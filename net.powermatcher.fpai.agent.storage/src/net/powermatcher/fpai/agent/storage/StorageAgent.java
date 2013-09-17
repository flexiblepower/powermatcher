package net.powermatcher.fpai.agent.storage;

import java.util.Date;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;
import net.powermatcher.core.scheduler.service.TimeConnectorService;
import net.powermatcher.fpai.agent.BidUtils;
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.fpai.agent.storage.StorageAgent.Config;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.ControlSpace;
import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerConstraint;
import org.flexiblepower.rai.values.PowerConstraintList;
import org.flexiblepower.rai.values.PowerValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Config.class)
public class StorageAgent extends FPAIAgent implements
                                           AgentConnectorService,
                                           LoggingConnectorService,
                                           TimeConnectorService,
                                           SchedulerConnectorService {

    private final static Logger logger = LoggerFactory.getLogger(StorageAgent.class);

    private StorageControlSpace currentControlSpace;

    /**
     * Current charge speed. Consumption/charging is indicated by a positive value, production/discharging is indicated
     * by a negative value
     */
    private double currentChargeSpeed;
    // private Date lastChange = new Date(0);

    /** If the device in the minTurnOn period the end time, otherwise null */
    private Date underMinTurnOnUntil = null;
    /** If the device in the minTurnOff period the end time, otherwise null */
    private Date underMinTurnOffUntil = null;

    public StorageAgent() {
        super();
    }

    public StorageAgent(ConfigurationService configuration) {
        super(configuration);
    }

    @Override
    public synchronized Allocation createAllocation(BidInfo bid, PriceInfo price, ControlSpace controlSpace) {
        double targetChargeSpeed = bid.getDemand(price.getCurrentPrice());

        if (currentChargeSpeed == 0 && targetChargeSpeed != 0) {
            // Turn ON
            logDebug("Turning device ON");
            underMinTurnOnUntil = new Date(getTimeSource().currentTimeMillis() + currentControlSpace.getMinOnPeriod()
                                                                                                    .getMilliseconds());
        } else if (currentChargeSpeed != 0 && targetChargeSpeed == 0) {
            // Turn OFF
            logDebug("Turning device OFF");
            underMinTurnOffUntil = new Date(getTimeSource().currentTimeMillis() + currentControlSpace.getMinOffPeriod()
                                                                                                     .getMilliseconds());
        }

        currentChargeSpeed = targetChargeSpeed;

        Duration duration = new Duration(controlSpace.getValidThru().getTime() - getTimeSource().currentTimeMillis(),
                                         TimeUnit.MILLISECONDS);

        double energyAmount = currentChargeSpeed * (duration.getValueAs(TimeUnit.SECONDS)); // in joules
        EnergyProfile energyProfile = new EnergyProfile(duration, EnergyUnit.JOULE, energyAmount);
        return new Allocation(currentControlSpace, new Date(getTimeSource().currentTimeMillis()), energyProfile);
    }

    @Override
    public synchronized BidInfo createBid(ControlSpace controlSpace, MarketBasis marketBasis) {
        assert controlSpace != null;
        assert marketBasis != null;

        logger.debug("Processing storage control space {}", controlSpace);

        currentControlSpace = (StorageControlSpace) controlSpace;

        // If must run, do not change anything, so return a constant bid of the current charge speed
        if (isUnderMinTurnOff() || isUnderMinTurnOn()) {
            return new BidInfo(marketBasis, new PricePoint(0, currentChargeSpeed));
        }

        double maxChargePower = getMaxPower(currentControlSpace.getChargeSpeed());
        double maxDischargePower = getMaxPower(currentControlSpace.getDischargeSpeed());

        double efficiency = currentControlSpace.getChargeEfficiency() * currentControlSpace.getDischargeEfficiency();
        efficiency = Math.min(1.0, efficiency);

        double diff = (.5 - (efficiency / 2)) / (1 + efficiency);
        int lowerLimit = Math.max(Math.min((int) Math.floor(((1 - currentControlSpace.getStateOfCharge()) - diff) * marketBasis.getPriceSteps()),
                                           marketBasis.getPriceSteps() - 1),
                                  0);
        int upperLimit = Math.max(Math.min((int) Math.floor(((1 - currentControlSpace.getStateOfCharge()) + diff) * marketBasis.getPriceSteps()),
                                           marketBasis.getPriceSteps() - 1),
                                  0);

        double[] demand = new double[marketBasis.getPriceSteps()];
        int i = 0;
        for (; i < lowerLimit; i++) {
            demand[i] = getClosestPower(currentControlSpace.getChargeSpeed(),
                                        maxChargePower * (1.0 - (i / (double) lowerLimit))).getValueAs(PowerUnit.WATT);
        }

        for (; i <= upperLimit; i++) {
            demand[i] = 0;
        }

        for (; i < demand.length; i++) {
            // TODO should it be lowerLimit in this formula?
            demand[i] = -getClosestPower(currentControlSpace.getDischargeSpeed(),
                                         maxDischargePower * ((i - demand.length - 1) / (double) lowerLimit)).getValueAs(PowerUnit.WATT);
        }

        BidInfo bid = new BidInfo(marketBasis, demand);

        PowerConstraintList combinedPCL = combinePowerConstraintLists(currentControlSpace.getChargeSpeed(),
                                                                      currentControlSpace.getDischargeSpeed());
        if (currentChargeSpeed != 0 && !canTurnOffNow(currentControlSpace)) {
            // We can't turn off now, so must run situation
            if (currentChargeSpeed > 0) { // We're charging
                bid = BidUtils.setMinimumDemand(bid, combinedPCL, currentChargeSpeed);
            } else if (currentChargeSpeed < 0) { // We're discharging
                bid = BidUtils.setMaximumDemand(bid, combinedPCL, currentChargeSpeed);
            }
        } else if (currentChargeSpeed <= 0 && !canStartCharging(currentControlSpace)) {
            // Can't start charging now
            bid = BidUtils.setMaximumDemand(bid, combinedPCL, 0);
        } else if (currentChargeSpeed >= 0 && !canStartDischarging(currentControlSpace)) {
            // Can't start discharging now
            bid = BidUtils.setMinimumDemand(bid, combinedPCL, 0);
        } else if (hasTarget(currentControlSpace)) {
            logDebug("Agent is working towards a target");
            double requiredWatt = requiredDemandForTarget(currentControlSpace).getValueAs(PowerUnit.WATT);
            if (requiredWatt > 0) {
                bid = BidUtils.setMinimumDemand(bid, combinedPCL, requiredWatt);
            } else if (requiredWatt < 0) {
                bid = BidUtils.setMaximumDemand(bid, combinedPCL, requiredWatt);
            }
        }

        logger.debug("Constructed bid curve: {}", bid);
        return bid;
    }

    private static boolean hasTarget(StorageControlSpace storageControlSpace) {
        return !(storageControlSpace.getTargetStateOfCharge() == null || storageControlSpace.getTargetTime() == null);
    }

    private PowerValue requiredDemandForTarget(StorageControlSpace controlSpace) {
        double deltaSOC = controlSpace.getTargetStateOfCharge() - controlSpace.getStateOfCharge();
        EnergyValue requiredEnergy = new EnergyValue(controlSpace.getTotalCapacity().getValueAs(EnergyUnit.WATTHOUR) * deltaSOC,
                                                     EnergyUnit.WATTHOUR);
        double hoursToDeadline = Math.max((controlSpace.getTargetTime().getTime() - getTimeSource().currentTimeMillis()) / 3600000.0,
                                          1 / 60.0);
        double minimumChareSpeedWatt = requiredEnergy.getValueAs(EnergyUnit.WATTHOUR) / hoursToDeadline;
        double correctedMinimumChareSpeedWatt = minimumChareSpeedWatt - controlSpace.getSelfDischarge()
                                                                                    .getValueAs(PowerUnit.WATT);
        return new PowerValue(correctedMinimumChareSpeedWatt, PowerUnit.WATT);
    }

    private double getMaxPower(PowerConstraintList pcl) {
        double maxPower = 0;

        for (PowerConstraint chargeSpeed : pcl) {
            double cs = chargeSpeed.getLowerBound().getValueAs(PowerUnit.WATT);
            if (cs > maxPower) {
                maxPower = cs;
            }

            cs = chargeSpeed.getUpperBound().getValueAs(PowerUnit.WATT);
            if (cs > maxPower) {
                maxPower = cs;
            }
        }

        return maxPower;
    }

    private PowerValue getClosestPower(PowerConstraintList pcl, double wantedPower) {
        PowerValue resultValue = null;
        double result = Double.NaN;

        for (PowerConstraint pc : pcl) {
            PowerValue powerValue = pc.getClosestValue(new PowerValue(wantedPower, PowerUnit.WATT));
            double power = powerValue.getValueAs(PowerUnit.WATT);

            if (resultValue == null || (Math.abs(power - wantedPower) < Math.abs(result - wantedPower))) {
                resultValue = powerValue;
                result = power;
            }
        }

        return resultValue;
    }

    private boolean isUnderMinTurnOn() {
        if (underMinTurnOnUntil == null) {
            return false;
        } else {
            if (getTimeSource().currentTimeMillis() > underMinTurnOnUntil.getTime()) {
                underMinTurnOnUntil = null;
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean isUnderMinTurnOff() {
        if (underMinTurnOffUntil == null) {
            return false;
        } else {
            if (getTimeSource().currentTimeMillis() > underMinTurnOffUntil.getTime()) {
                underMinTurnOffUntil = null;
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Check if the device can turn off now in order to prevent drainage
     */
    private boolean canTurnOffNow(StorageControlSpace storageControlSpace) {
        if (currentChargeSpeed == 0) {
            // device IS off
            return false;
        } else {
            double selfDischargeWatt = storageControlSpace.getSelfDischarge().getValueAs(PowerUnit.WATT);
            double minOffHours = storageControlSpace.getMinOffPeriod().getValueAs(TimeUnit.HOURS);
            double minChargeEnergyWH = selfDischargeWatt * minOffHours;
            double totalCapacityWH = storageControlSpace.getTotalCapacity().getValueAs(EnergyUnit.WATTHOUR);
            double minSOC = minChargeEnergyWH / totalCapacityWH;
            return storageControlSpace.getStateOfCharge() >= minSOC;
        }
    }

    /**
     * Check if the device can turn on now in order to prevent over charging
     */
    private boolean canStartCharging(StorageControlSpace storageControlSpace) { // TODO
        if (currentChargeSpeed > 0) {
            // device IS charging
            return false;
        } else {
            double demandWatt = BidUtils.highestPowerValue(storageControlSpace.getChargeSpeed())
                                        .getValueAs(PowerUnit.WATT);
            double netDemandWatt = demandWatt - storageControlSpace.getSelfDischarge().getValueAs(PowerUnit.WATT);
            double minOnHours = storageControlSpace.getMinOnPeriod().getValueAs(TimeUnit.HOURS);
            double minChargeEnergyWH = minOnHours * netDemandWatt;
            double totalCapacityWH = storageControlSpace.getTotalCapacity().getValueAs(EnergyUnit.WATTHOUR);
            double maxSOC = 1 - (minChargeEnergyWH / totalCapacityWH);
            return storageControlSpace.getStateOfCharge() < maxSOC;
        }
    }

    /**
     * Check if the device can turn on now in order to prevent drainage
     */
    private boolean canStartDischarging(StorageControlSpace storageControlSpace) { // TODO
        if (currentChargeSpeed < 0) {
            // device IS discharging
            return false;
        } else {
            double dischargeSpeedWatt = BidUtils.highestPowerValue(storageControlSpace.getDischargeSpeed())
                                                .getValueAs(PowerUnit.WATT);
            double netDischargeSpeedWatt = dischargeSpeedWatt + storageControlSpace.getSelfDischarge()
                                                                                   .getValueAs(PowerUnit.WATT);
            double minOnHours = storageControlSpace.getMinOnPeriod().getValueAs(TimeUnit.HOURS);
            double minChargeEnergyWH = minOnHours * netDischargeSpeedWatt;
            double totalCapacityWH = storageControlSpace.getTotalCapacity().getValueAs(EnergyUnit.WATTHOUR);
            double minSOC = minChargeEnergyWH / totalCapacityWH;
            return storageControlSpace.getStateOfCharge() > minSOC;
        }
    }

    private static PowerConstraint negate(PowerConstraint pc) {
        PowerValue l = pc.getLowerBound(), u = pc.getUpperBound();
        return new PowerConstraint(new PowerValue(-u.getValue(), u.getUnit()), new PowerValue(-l.getValue(),
                                                                                              l.getUnit()));
    }

    private static PowerConstraintList combinePowerConstraintLists(PowerConstraintList charge,
                                                                   PowerConstraintList discharge) {
        if (discharge == null) {
            return charge;
        }
        // Make a clone and merge powerConstraintLists
        PowerConstraint[] values = charge.toArray(new PowerConstraint[charge.size() + discharge.size()]);
        int i = 0;
        for (PowerConstraint pc : charge) {
            values[i] = pc;
            i++;
        }
        for (PowerConstraint pc : discharge) {
            values[i] = negate(pc);
            i++;
        }
        return new PowerConstraintList(values);
    }

    public static interface Config extends AgentConfiguration {
        @Override
        @Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT)
        public String cluster_id();

        @Override
        public String id();

        @Override
        @Meta.AD(required = false, deflt = IdentifiableObjectConfiguration.ENABLED_DEFAULT_STR)
        public boolean enabled();

        @Override
        @Meta.AD(required = false, deflt = UPDATE_INTERVAL_DEFAULT_STR)
        public int update_interval();

        @Override
        @Meta.AD(required = false,
                 deflt = AGENT_BID_LOG_LEVEL_DEFAULT,
                 optionValues = { NO_LOGGING, PARTIAL_LOGGING, FULL_LOGGING },
                 optionLabels = { NO_LOGGING_LABEL, PARTIAL_LOGGING_LABEL, FULL_LOGGING_LABEL })
        public String agent_bid_log_level();

        @Override
        @Meta.AD(required = false,
                 deflt = AGENT_PRICE_LOG_LEVEL_DEFAULT,
                 optionValues = { NO_LOGGING, FULL_LOGGING },
                 optionLabels = { NO_LOGGING_LABEL, FULL_LOGGING_LABEL })
        public String agent_price_log_level();
    }
}
