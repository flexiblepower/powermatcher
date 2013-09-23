package net.powermatcher.fpai.agent.buffer;

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
import net.powermatcher.fpai.agent.buffer.BufferAgent.Config;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.BufferControlSpace;
import org.flexiblepower.rai.ControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyProfile.Element;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerValue;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Config.class)
public class BufferAgent extends FPAIAgent implements
                                          AgentConnectorService,
                                          LoggingConnectorService,
                                          TimeConnectorService,
                                          SchedulerConnectorService {
    public BufferAgent() {
        super();
    }

    public BufferAgent(ConfigurationService configuration) {
        super(configuration);
    }

    /** This value influences the bid strategy */
    private final double bidBandWidth = 0.25; // TODO analyse this value

    /** The last allocation given */
    private Allocation lastAllocation;

    /** The time until the device must run (or null if not applicable) */
    private Date mustRunUntil = null;

    /** The time until the device must not run (or null if not applicable) */
    private Date mustNotRunUntil = null;

    /**
     * Main function that creates the bid based on the ControlSpace.
     */
    @Override
    protected BidInfo createBid(ControlSpace controlSpace, MarketBasis marketBasis) {
        // if there's no control space, issue a flat bid with power value 0
        if (controlSpace == null) {
            return createFlatBid(marketBasis, new PowerValue(0, PowerUnit.WATT));
        }

        BufferControlSpace bufferControlSpace = (BufferControlSpace) controlSpace;

        // if the buffer can't turn on, return a flat bid with power value 0
        if (isInMustNotRunState() || (isOffByAllocation() && !canTurnOn(bufferControlSpace))) {
            return createFlatBid(marketBasis, new PowerValue(0, PowerUnit.WATT));
        }

        // perform the basic bidding strategy
        BidInfo bid = bidStrategy(marketBasis, bufferControlSpace);

        // check if there is a minimum charge speed and apply it
        double minimumChargeSpeedWatt = calculateMinimumChargeSpeed(bufferControlSpace);
        if (minimumChargeSpeedWatt > 0) {
            bid = BidUtils.setMinimumDemand(bid, bufferControlSpace.getChargeSpeed(), minimumChargeSpeedWatt);
        }

        // return the bid;
        return bid;
    }

    private double calculateMinimumChargeSpeed(BufferControlSpace bufferControlSpace) {
        // calculate the minimum charge speed if there is a target
        double minChargeSpeedTargetWatt = 0;
        if (hasTarget(bufferControlSpace)) {
            minChargeSpeedTargetWatt = minimumChargeSpeedForTarget(bufferControlSpace).getValueAs(PowerUnit.WATT);
        }

        // calculate the charge speed for must-run situations
        double minChargeSpeedMustRunWatt = 0;
        if (isInMustRunState() || (isOnByAllocation() && !canTurnOff(bufferControlSpace))) {
            // Must run situation
            minChargeSpeedMustRunWatt = bufferControlSpace.getChargeSpeed().getMinimum().getValueAs(PowerUnit.WATT);
        }

        return Math.max(minChargeSpeedTargetWatt, minChargeSpeedMustRunWatt);
    }

    /**
     * Check if the device can turn off now in order to prevent drainage to SoC below 0
     */
    private boolean canTurnOff(BufferControlSpace bufferControlSpace) {
        // device IS off
        if (isOffByAllocation()) {
            return false;
        } else {
            double selfDischargeWatt = bufferControlSpace.getSelfDischarge().getValueAs(PowerUnit.WATT);
            double minOffHours = bufferControlSpace.getMinOffPeriod().getValueAs(TimeUnit.HOURS);
            double minChargeEnergyWH = selfDischargeWatt * minOffHours;
            double totalCapacityWH = bufferControlSpace.getTotalCapacity().getValueAs(EnergyUnit.WATTHOUR);
            double minSOC = minChargeEnergyWH / totalCapacityWH;
            return bufferControlSpace.getStateOfCharge() >= minSOC;
        }
    }

    /**
     * Check if the device can turn on now in order to prevent over-charging to SoC above 1
     */
    private boolean canTurnOn(BufferControlSpace bufferControlSpace) {
        // device IS on
        if (isOnByAllocation()) {
            return false;
        } else {
            double minDemandWatt = BidUtils.lowestPowerValue(bufferControlSpace.getChargeSpeed())
                                           .getValueAs(PowerUnit.WATT);
            double netDemandWatt = minDemandWatt - bufferControlSpace.getSelfDischarge().getValueAs(PowerUnit.WATT);
            double minOnHours = bufferControlSpace.getMinOnPeriod().getValueAs(TimeUnit.HOURS);
            double minChargeEnergyWH = minOnHours * netDemandWatt;
            double totalCapacityWH = bufferControlSpace.getTotalCapacity().getValueAs(EnergyUnit.WATTHOUR);
            double maxSOC = 1 - (minChargeEnergyWH / totalCapacityWH);
            return bufferControlSpace.getStateOfCharge() < maxSOC;
        }
    }

    private boolean isOnByAllocation() {
        return getCurrentlyAllocatedPower() != 0;
    }

    private boolean isOffByAllocation() {
        return getCurrentlyAllocatedPower() == 0;
    }

    /**
     * The default bid strategy. TODO give brief explanation of strategy.
     * 
     * @param marketBasis
     *            The market basis to use for creating the bid.
     * @param bufferControlSpace
     *            The control space which expresses the flexibility.
     * @return The bid based on the given flexibility.
     */
    private BidInfo bidStrategy(MarketBasis marketBasis, BufferControlSpace bufferControlSpace) {

        int turnOffindex;

        // Test to add target-behaviour to bid
        if (hasTarget(bufferControlSpace)) {

            turnOffindex = (int) ((1 - bufferControlSpace.getStateOfCharge()) * marketBasis.getPriceSteps());
            turnOffindex = turnOffindex / 2;

        } else {
            turnOffindex = (int) ((1 - bufferControlSpace.getStateOfCharge()) * marketBasis.getPriceSteps());
        }

        int bidBandStartIndex = Math.max(0, (int) (turnOffindex - marketBasis.getPriceSteps() * (bidBandWidth * 0.5)));
        int bidBandEndIndex = Math.min(marketBasis.getPriceSteps() - 1,
                                       turnOffindex + (turnOffindex - bidBandStartIndex));

        double maxDemand = BidUtils.highestPowerValue(bufferControlSpace.getChargeSpeed()).getValueAs(PowerUnit.WATT);
        double minDemand = BidUtils.lowestPowerValue(bufferControlSpace.getChargeSpeed()).getValueAs(PowerUnit.WATT);
        PricePoint[] pricePoints = new PricePoint[] { new PricePoint(0, maxDemand),
                                                     new PricePoint(bidBandStartIndex, maxDemand),
                                                     new PricePoint(bidBandEndIndex, minDemand),
                                                     new PricePoint(bidBandEndIndex, 0) };
        BidInfo bid = new BidInfo(marketBasis, pricePoints);
        return BidUtils.roundBidToPowerConstraintList(bid, bufferControlSpace.getChargeSpeed(), true);
    }

    /**
     * Determines the charge speed to meet the state of charge target. TODO consider if this behavior isn't already
     * covered by the default bidding strategy.
     * 
     * @return 0 if there is time left for 'idling' or max power if in must-run state.
     */
    private PowerValue minimumChargeSpeedForTarget(BufferControlSpace controlSpace) {
        // calculate the amount of energy required to achieve the target state of charge
        double deltaSoC = controlSpace.getTargetStateOfCharge() - controlSpace.getStateOfCharge();
        double deltaEnergy = deltaSoC * controlSpace.getTotalCapacity().getValueAs(EnergyUnit.KILO_WATTHOUR);

        // calculate the time required to charge to the target state of charge
        double discharge = controlSpace.getSelfDischarge().getValueAs(PowerUnit.KILO_WATT);
        double maxPower = controlSpace.getChargeSpeed().getMaximum().getValueAs(PowerUnit.KILO_WATT);
        double minDeltaTime = deltaEnergy / (maxPower - discharge);

        // calculate the time frame available
        double maxDeltaTime = new Duration(new Date(getTimeSource().currentTimeMillis()), controlSpace.getTargetTime()).getValueAs(TimeUnit.HOURS);

        if (minDeltaTime < maxDeltaTime) {
            // if there is time to idle, return 0 as minimum
            return new PowerValue(0, PowerUnit.WATT);
        } else {
            // otherwise, the buffer is in a must-run situation
            return controlSpace.getChargeSpeed().getMaximum();
        }
    }

    /**
     * Create a flat/must run bid the provided demand
     */
    private BidInfo createFlatBid(MarketBasis marketBasis, PowerValue demand) {
        return new BidInfo(marketBasis, new PricePoint(0, demand.getValueAs(PowerUnit.WATT)));
    }

    /**
     * Check if the control space has a target
     */
    private static boolean hasTarget(BufferControlSpace bufferControlSpace) {
        return !(bufferControlSpace.getTargetStateOfCharge() == null || bufferControlSpace.getTargetTime() == null);
    }

    /**
     * Create an Allocation. This is done by looking at the latest bid.
     */
    @Override
    protected Allocation createAllocation(BidInfo lastBid, PriceInfo newPriceInfo, ControlSpace controlSpace) {
        if (controlSpace == null) {
            return null;
        }

        // calculate the target power given the last bid (if any in that case power is 0)
        double targetPower = lastBid == null ? 0 : lastBid.getDemand(newPriceInfo.getCurrentPrice());

        // calculate the currently applicable target power given the last allocation
        double currentTargetPower = getCurrentlyAllocatedPower();

        BufferControlSpace bufferControlSpace = (BufferControlSpace) controlSpace;
        long now = getTimeSource().currentTimeMillis();

        // ignore deviations from the current target below the threshold (as ratio of the max charge speed, e.g. 1)
        // but only if we have an allocation for the current point in time
        if (getCurrentlyAllocatedPowerOrNull() != null) {
            double updateThreadholdRatio = getProperty("allocation.update.threshold", 0.001d);
            double threshold = bufferControlSpace.getChargeSpeed().getMaximum().getValueAs(PowerUnit.WATT) * updateThreadholdRatio;
            if (Math.abs(currentTargetPower - targetPower) < threshold) {
                return null;
            }
        }

        // if we're turning on or off, calculate the time at which we can switch again
        if (currentTargetPower == 0 && targetPower != 0) {
            logDebug("Turning device ON");
            mustRunUntil = new Date(now + bufferControlSpace.getMinOnPeriod().getMilliseconds());
        } else if (currentTargetPower != 0 && targetPower == 0) {
            logDebug("Turning device OFF");
            mustNotRunUntil = new Date(now + bufferControlSpace.getMinOffPeriod().getMilliseconds());
        }

        // Construct allocation object
        Date allocationEnd = bufferControlSpace.getValidThru();
        Duration duration = new Duration(allocationEnd.getTime() - now, TimeUnit.MILLISECONDS);
        EnergyValue targetEnergyVolume = new EnergyValue(targetPower * (duration.getValueAs(TimeUnit.SECONDS)),
                                                         EnergyUnit.JOULE);

        // return the allocation and remember it
        Date startTime = new Date(now);
        EnergyProfile energyProfile = new EnergyProfile(duration, targetEnergyVolume);
        return lastAllocation = new Allocation(bufferControlSpace, startTime, energyProfile);
    }

    /** @return true if the resource is in a must-run state */
    private boolean isInMustRunState() {
        if (mustRunUntil == null) {
            return false;
        } else if (getTimeSource().currentTimeMillis() > mustRunUntil.getTime()) {
            mustRunUntil = null;
            return false;
        } else {
            return true;
        }
    }

    /** @return true if the resource is in a must-not-run state */
    private boolean isInMustNotRunState() {
        if (mustNotRunUntil == null) {
            return false;
        } else if (getTimeSource().currentTimeMillis() > mustNotRunUntil.getTime()) {
            mustNotRunUntil = null;
            return false;
        } else {
            return true;
        }
    }

    /**
     * @return The currently applicable target power given the last allocation
     */
    private double getCurrentlyAllocatedPower() {
        // if we haven't allocated a profile yet, but (for code stability ... other code depends on this behavior ...
        // maybe fix at some point in time), return 0 as the default
        if (lastAllocation == null) {
            return 0;
        }

        Double currentlyAllocatedPower = getCurrentlyAllocatedPowerOrNull();

        // there is no actual allocation, but (for code stability ... other code depends on this behavior ... maybe fix
        // at some point in time), so indicate 0 as default allocated power
        if (currentlyAllocatedPower == null) {
            return 0;
        }

        return currentlyAllocatedPower;
    }

    /**
     * @return The currently applicable target power given the last allocation, or null if there is no currently
     *         applicable allocation
     */
    private Double getCurrentlyAllocatedPowerOrNull() {
        if (lastAllocation == null) {
            return null;
        }

        // determine position in current allocation
        Date now = new Date(getTimeSource().currentTimeMillis());
        Duration offsetInAllocation = new Duration(lastAllocation.getStartTime(), now);
        // pick the currently applicable element from the allocation
        Element currentAllocElement = lastAllocation.getEnergyProfile().getElementForOffset(offsetInAllocation);

        // allocation not yet active (starts in the future) or already completed (ends in the past)
        if (currentAllocElement == null) {
            return null;
        }

        // return the power we have allocated for the current point in time
        return currentAllocElement.getAveragePower().getValueAs(PowerUnit.WATT);
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

        @Meta.AD(required = false,
                 deflt = "0.001",
                 description = "The threshold for updating the allocation (expressed as the minimum change in requested power level as factor of the maximum chargespeed, e.g. 0.001 for one per mille of the maximum charge speed)")
        public double
                allocation_update_threshold();
    }

}
