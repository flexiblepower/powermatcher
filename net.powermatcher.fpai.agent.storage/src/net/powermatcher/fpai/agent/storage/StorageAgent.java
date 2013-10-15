package net.powermatcher.fpai.agent.storage;

import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.WATT;

import java.util.Date;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Power;

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
import net.powermatcher.fpai.agent.BidUtil;
import net.powermatcher.fpai.agent.ConstraintListUtil;
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.fpai.agent.storage.StorageAgent.Config;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.values.ConstraintList;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.time.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Config.class)
public class StorageAgent extends FPAIAgent<StorageControlSpace> implements
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
    private double currentChargeSpeedWatt;

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
    public synchronized Allocation createAllocation(BidInfo bid, PriceInfo price, StorageControlSpace controlSpace) {
        double targetChargeSpeedWatt = bid.getDemand(price.getCurrentPrice());

        if (currentChargeSpeedWatt == 0 && targetChargeSpeedWatt != 0) {
            // Turn ON
            logDebug("Turning device ON");
            underMinTurnOnUntil = new Date(getTimeSource().currentTimeMillis() + controlSpace.getMinOnPeriod()
                                                                                             .longValue(MILLI(SECOND)));
        } else if (currentChargeSpeedWatt != 0 && targetChargeSpeedWatt == 0) {
            // Turn OFF
            logDebug("Turning device OFF");
            underMinTurnOffUntil = new Date(getTimeSource().currentTimeMillis() + controlSpace.getMinOffPeriod()
                                                                                              .longValue(MILLI(SECOND)));
        }

        currentChargeSpeedWatt = targetChargeSpeedWatt;

        Measurable<Duration> duration = TimeUtil.difference(new Date(getTimeSource().currentTimeMillis()),
                                                            controlSpace.getValidThru());

        double energyAmount = currentChargeSpeedWatt * (duration.doubleValue(SECOND)); // in joules
        EnergyProfile energyProfile = EnergyProfile.create()
                                                   .add(duration, Measure.valueOf(energyAmount, JOULE))
                                                   .build();
        return new Allocation(currentControlSpace, new Date(getTimeSource().currentTimeMillis()), energyProfile);
    }

    @Override
    public synchronized BidInfo createBid(StorageControlSpace controlSpace, MarketBasis marketBasis) {
        assert controlSpace != null;
        assert marketBasis != null;

        logger.debug("Processing storage control space {}", controlSpace);

        currentControlSpace = controlSpace;

        // If must run, do not change anything, so return a constant bid of the current charge speed
        if (isUnderMinTurnOff() || isUnderMinTurnOn()) {
            return new BidInfo(marketBasis, new PricePoint(0, currentChargeSpeedWatt));
        }

        Measurable<Power> maxChargePower = currentControlSpace.getChargeSpeed().getMaximum();
        Measurable<Power> maxDischargePower = currentControlSpace.getDischargeSpeed().getMaximum();

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
            demand[i] = ConstraintListUtil.getClosestPower(currentControlSpace.getChargeSpeed(),
                                                           Measure.valueOf((maxChargePower.doubleValue(WATT) * (1.0 - (i / (double) lowerLimit))),
                                                                           WATT))
                                          .doubleValue(WATT);
        }

        for (; i <= upperLimit; i++) {
            demand[i] = 0;
        }

        for (; i < demand.length; i++) {
            // TODO should it be lowerLimit in this formula?
            demand[i] = -ConstraintListUtil.getClosestPower(currentControlSpace.getDischargeSpeed(),
                                                            Measure.valueOf((maxDischargePower.doubleValue(WATT) * ((i - demand.length - 1) / (double) lowerLimit)),
                                                                            WATT))
                                           .doubleValue(WATT);
        }

        BidInfo bid = new BidInfo(marketBasis, demand);

        ConstraintList<Power> combinedPCL = ConstraintListUtil.combinePowerConstraintLists(currentControlSpace.getChargeSpeed(),
                                                                                           currentControlSpace.getDischargeSpeed());
        if (currentChargeSpeedWatt != 0 && !canTurnOffNow(currentControlSpace)) {
            // We can't turn off now, so must run situation
            if (currentChargeSpeedWatt > 0) { // We're charging
                bid = BidUtil.setMinimumDemand(bid, Measure.valueOf(currentChargeSpeedWatt, WATT));
            } else if (currentChargeSpeedWatt < 0) { // We're discharging
                bid = BidUtil.setMaximumDemand(bid, Measure.valueOf(currentChargeSpeedWatt, WATT));
            }
        } else if (currentChargeSpeedWatt <= 0 && !canStartCharging(currentControlSpace)) {
            // Can't start charging now
            bid = BidUtil.setMaximumDemand(bid, Measure.valueOf(0, WATT));
        } else if (currentChargeSpeedWatt >= 0 && !canStartDischarging(currentControlSpace)) {
            // Can't start discharging now
            bid = BidUtil.setMinimumDemand(bid, Measure.valueOf(0, WATT));
        } else if (hasTarget(currentControlSpace)) {
            logDebug("Agent is working towards a target");
            Measurable<Power> requiredPower = requiredDemandForTarget(currentControlSpace);
            if (requiredPower.doubleValue(WATT) > 0) {
                requiredPower = ConstraintListUtil.ceilToPowerConstraintList(combinedPCL, requiredPower);
                if (requiredPower == null) {
                    requiredPower = combinedPCL.getMaximum(); // best we can do
                }
                bid = BidUtil.setMinimumDemand(bid, requiredPower);
            } else if (requiredPower.doubleValue(WATT) < 0) {
                requiredPower = ConstraintListUtil.floorToPowerConstraintList(combinedPCL, requiredPower);
                if (requiredPower == null) {
                    requiredPower = combinedPCL.getMinimum(); // best we can do
                }
                bid = BidUtil.setMaximumDemand(bid, requiredPower);
            }
        }

        logger.debug("Constructed bid curve: {}", bid);
        return bid;
    }

    private static boolean hasTarget(StorageControlSpace storageControlSpace) {
        return !(storageControlSpace.getTargetStateOfCharge() == null || storageControlSpace.getTargetTime() == null);
    }

    private Measurable<Power> requiredDemandForTarget(StorageControlSpace controlSpace) {
        double deltaSOC = controlSpace.getTargetStateOfCharge() - controlSpace.getStateOfCharge();
        double requiredEnergyJoule = controlSpace.getTotalCapacity().doubleValue(JOULE) * deltaSOC;
        Measurable<Duration> timeToDeadline = TimeUtil.difference(new Date(getTimeSource().currentTimeMillis()),
                                                                  controlSpace.getTargetTime());
        double minimumChargeSpeedWatt = requiredEnergyJoule / timeToDeadline.doubleValue(SECOND);
        double correctedMinimumChargeSpeedWatt = minimumChargeSpeedWatt + controlSpace.getSelfDischarge()
                                                                                      .doubleValue(WATT);
        return Measure.valueOf(correctedMinimumChargeSpeedWatt, WATT);
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
        if (currentChargeSpeedWatt == 0) {
            // device IS off
            return false;
        } else {
            double selfDischargeWatt = storageControlSpace.getSelfDischarge().doubleValue(WATT);
            double minOffSeconds = storageControlSpace.getMinOffPeriod().doubleValue(SECOND);
            double minChargeEnergyJoule = selfDischargeWatt * minOffSeconds;
            double totalCapacityJoule = storageControlSpace.getTotalCapacity().doubleValue(JOULE);
            double minSOC = minChargeEnergyJoule / totalCapacityJoule;
            return storageControlSpace.getStateOfCharge() >= minSOC;
        }
    }

    /**
     * Check if the device can turn on now in order to prevent over charging
     */
    private boolean canStartCharging(StorageControlSpace controlSpace) {
        if (currentChargeSpeedWatt > 0) {
            // device IS charging
            return false;
        } else {
            double demandWatt = controlSpace.getChargeSpeed().getMaximum().doubleValue(WATT);
            double netDemandWatt = demandWatt - controlSpace.getSelfDischarge().doubleValue(WATT);
            double minOnSeconds = controlSpace.getMinOnPeriod().doubleValue(SECOND);
            double minChargeEnergyJoule = minOnSeconds * netDemandWatt;
            double totalCapacityJoule = controlSpace.getTotalCapacity().doubleValue(JOULE);
            double maxSOC = 1 - (minChargeEnergyJoule / totalCapacityJoule);
            return controlSpace.getStateOfCharge() < maxSOC;
        }
    }

    /**
     * Check if the device can turn on now in order to prevent drainage
     */
    private boolean canStartDischarging(StorageControlSpace storageControlSpace) {
        if (currentChargeSpeedWatt < 0) {
            // device IS discharging
            return false;
        } else {
            double dischargeSpeedWatt = storageControlSpace.getDischargeSpeed().getMaximum().doubleValue(WATT);
            double netDischargeSpeedWatt = dischargeSpeedWatt + storageControlSpace.getSelfDischarge()
                                                                                   .doubleValue(WATT);
            double minOnSeconds = storageControlSpace.getMinOnPeriod().doubleValue(SECOND);
            double minChargeEnergyJoule = minOnSeconds * netDischargeSpeedWatt;
            double totalCapacityJoule = storageControlSpace.getTotalCapacity().doubleValue(JOULE);
            double minSOC = minChargeEnergyJoule / totalCapacityJoule;
            return storageControlSpace.getStateOfCharge() >= minSOC;
        }
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
