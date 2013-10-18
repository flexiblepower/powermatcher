package net.powermatcher.fpai.agent.buffer;

import static javax.measure.unit.SI.JOULE;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.WATT;

import java.util.Date;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
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
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.fpai.agent.buffer.BufferAgent.Config;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.BufferControlSpace;
import org.flexiblepower.rai.values.Constraint;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyProfile.Element;
import org.flexiblepower.time.TimeUtil;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Config.class)
public class BufferAgent extends FPAIAgent<BufferControlSpace> implements
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
    private final double bidBandWidth = 0.25; // TODO analyze this value

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
    protected BidInfo createBid(BufferControlSpace controlSpace, MarketBasis marketBasis) {
        // if the buffer can't turn on, return a flat bid with power value 0
        if (isInMustNotRunState() || (isOffByAllocation() && !canTurnOn(controlSpace))) {
            return BidUtil.zeroBid(marketBasis);
        }

        // perform the basic bidding strategy (either with the SoC delta from the target as 'driver' or the SoC itself)
        BidInfo bid;
        if (hasTarget(controlSpace)) {
            bid = createBidByTargetSoCDelta(marketBasis, controlSpace);
        } else {
            bid = createBidBySoC(marketBasis, controlSpace);
        }

        // check if there is a minimum charge speed and apply it
        Measurable<Power> minimumChargeSpeed = calculateMinimumChargeSpeed(controlSpace);
        if (minimumChargeSpeed.doubleValue(WATT) > 0) {
            bid = BidUtil.setMinimumDemand(bid, minimumChargeSpeed);
        }

        // return the bid;
        return bid;
    }

    /**
     * The default bid strategy.
     * 
     * @param marketBasis
     *            The market basis to use for creating the bid.
     * @param controlSpace
     *            The control space which expresses the flexibility.
     * @return The bid based on the given flexibility.
     */
    private BidInfo createBidBySoC(MarketBasis marketBasis, BufferControlSpace controlSpace) {
        // select the price (index) at which the buffer will be charged at minimum power (or is off if possible)
        // this price is inversely proportional to the state of charge, i.e. the higher the SoC the lower the price must
        // be for the buffer to charge
        int minDemandPrice = (int) ((1 - controlSpace.getStateOfCharge()) * marketBasis.getPriceSteps());

        // The bid is shaped as a slope over a range in the total price range of the market
        int maxDemandPrice = (int) (minDemandPrice - (marketBasis.getPriceSteps() * bidBandWidth));

        // create the bid respecting the bounds of the market basis and control space
        return createSlopedBid(marketBasis, controlSpace, minDemandPrice, maxDemandPrice);
    }

    /**
     * The default bid strategy.
     * 
     * @param marketBasis
     *            The market basis to use for creating the bid.
     * @param controlSpace
     *            The control space which expresses the flexibility.
     * @return The bid based on the given flexibility.
     */
    private BidInfo createBidByTargetSoCDelta(MarketBasis marketBasis, BufferControlSpace controlSpace) {
        // calculate the amount of energy required to achieve the target state of charge
        double deltaSoC = controlSpace.getTargetStateOfCharge() - controlSpace.getStateOfCharge();

        // use normal bidding strategy if target already achieved
        if (deltaSoC < 0) {
            return createBidBySoC(marketBasis, controlSpace);
        }

        // calculate the amount to charge
        double deltaEnergyJoule = deltaSoC * controlSpace.getTotalCapacity().doubleValue(JOULE);

        // calculate the time required to charge to the target state of charge at maximum power
        double dischargeWatt = controlSpace.getSelfDischarge().doubleValue(WATT);
        double maxChargePowerWatt = controlSpace.getChargeSpeed().getMaximum().doubleValue(WATT);
        double fastestChargeTimeSecond = deltaEnergyJoule / (maxChargePowerWatt - dischargeWatt);

        // calculate the time frame available
        double timeToDeadlineMS = getTimeSource().currentTimeMillis() - controlSpace.getTargetTime().getTime();

        // if past the deadline ...
        if (timeToDeadlineMS <= 0) {
            if (deltaSoC > 0) {
                // and target SoC not yet achieved, charge as fast as possible (minimize 'damage')
                return BidUtil.createFlatBid(marketBasis, controlSpace.getChargeSpeed().getMaximum());
            } else {
                // otherwise use normal bidding strategy
                return createBidBySoC(marketBasis, controlSpace);
            }
        }

        // the price for which the maximum demand is desirable is inversely proportional to the ratio between the
        // minimum time required to cover the delta SoC (at max power) and the time until the target deadline. I.e. the
        // closer to a must-run situation, the higher the accepted price.
        int maxDemandPrice = (int) (((fastestChargeTimeSecond * 1000) / timeToDeadlineMS) * marketBasis.getPriceSteps());
        // the bid is slope shaped with a configured width, here (with a target), if the price is higher than
        // maxDemandPrice, it may still be 'acceptable', but at a lower charging power
        // i.e. the width of the slope influences the eagerness to charge the buffer, the wider the slope, the more
        // eager the strategy
        int minDemandPrice = (int) (maxDemandPrice + marketBasis.getPriceSteps() * bidBandWidth);

        // create the bid respecting the bounds of the market basis and control space
        return createSlopedBid(marketBasis, controlSpace, minDemandPrice, maxDemandPrice);
    }

    /**
     * Create a sloped bid determined by the price indices for which the maximum / minimum demand should be expressed in
     * the bid - respecting the bounds of the market basis and control space.
     * 
     * @param marketBasis
     *            The market basis by which the price indices will be bounded.
     * @param bufferControlSpace
     *            The buffer control space which
     * @param minDemandPriceIdx
     *            The price for which minimum demand is desirable.
     * @param maxDemandPriceIdx
     *            The price for which maximum demand is desirable.
     * @return The sloped bid based on the given prices and the price bounds in the market basis and the power
     *         constraints in the control space.
     */
    private BidInfo createSlopedBid(MarketBasis marketBasis,
                                    BufferControlSpace bufferControlSpace,
                                    int minDemandPriceIdx,
                                    int maxDemandPriceIdx) {
        // if the maximum demand is at or beyond the price range, we need to generate a must run bid
        if (maxDemandPriceIdx >= marketBasis.getPriceSteps()) {
            return BidUtil.createFlatBid(marketBasis, bufferControlSpace.getChargeSpeed().getMaximum());
        }

        // the min and max prices are bound by the price basis
        maxDemandPriceIdx = Math.max(0, maxDemandPriceIdx);
        minDemandPriceIdx = Math.min(marketBasis.getPriceSteps(), minDemandPriceIdx);

        // get the max and min demand possible
        double maxDemand = bufferControlSpace.getChargeSpeed().getMaximum().doubleValue(WATT);
        double minDemand = bufferControlSpace.getChargeSpeed().getMinimum().doubleValue(WATT);

        // construct the bid via price points
        PricePoint[] pricePoints = new PricePoint[] { new PricePoint(0, maxDemand),
                                                     new PricePoint(maxDemandPriceIdx, maxDemand),
                                                     new PricePoint(minDemandPriceIdx, minDemand),
                                                     new PricePoint(minDemandPriceIdx, 0) };
        BidInfo bid = new BidInfo(marketBasis, pricePoints);

        // constrain the bid to the possibilities of the buffer to be charged with
        return BidUtil.roundBidToPowerConstraintList(bid, bufferControlSpace.getChargeSpeed(), true);
    }

    /**
     * Calculate the minimum charge speed for must-run situations
     */
    private Measurable<Power> calculateMinimumChargeSpeed(BufferControlSpace bufferControlSpace) {
        if (isInMustRunState() || (isOnByAllocation() && !canTurnOff(bufferControlSpace))) {
            // determined by the lowest power which is > 0
            for (Constraint<Power> c : bufferControlSpace.getChargeSpeed()) {
                Measurable<Power> lowerBound = c.getLowerBound();
                if (lowerBound.doubleValue(WATT) > 0) {
                    return lowerBound;
                }
            }
        }

        // There is currently no minimum charge speed
        return Measure.valueOf(0, WATT);
    }

    /**
     * Check if the control space has a target
     */
    private static boolean hasTarget(BufferControlSpace bufferControlSpace) {
        return !(bufferControlSpace.getTargetStateOfCharge() == null || bufferControlSpace.getTargetTime() == null);
    }

    /**
     * Check if the device can turn off now in order to prevent drainage to SoC below 0
     */
    private boolean canTurnOff(BufferControlSpace bufferControlSpace) {
        // device IS off
        if (isOffByAllocation()) {
            return false;
        } else {
            double selfDischargeWatt = bufferControlSpace.getSelfDischarge().doubleValue(WATT);
            double minOffSecond = bufferControlSpace.getMinOffPeriod().doubleValue(SECOND);
            double minChargeEnergyJoule = selfDischargeWatt * minOffSecond;
            double totalCapacityJoule = bufferControlSpace.getTotalCapacity().doubleValue(JOULE);
            double minSOC = minChargeEnergyJoule / totalCapacityJoule;
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
            double minDemandWatt = bufferControlSpace.getChargeSpeed().getMinimum().doubleValue(WATT);
            double netDemandWatt = minDemandWatt - bufferControlSpace.getSelfDischarge().doubleValue(WATT);
            double minOnSecond = bufferControlSpace.getMinOnPeriod().doubleValue(SECOND);
            double minChargeEnergyJoule = minOnSecond * netDemandWatt;
            double totalCapacityJoule = bufferControlSpace.getTotalCapacity().doubleValue(JOULE);
            double maxSOC = 1 - (minChargeEnergyJoule / totalCapacityJoule);
            return bufferControlSpace.getStateOfCharge() < maxSOC;
        }
    }

    /**
     * Create an Allocation. This is done by looking at the latest bid.
     */
    @Override
    protected Allocation createAllocation(BidInfo lastBid, PriceInfo newPriceInfo, BufferControlSpace controlSpace) {
        if (controlSpace == null) {
            return null;
        }

        // calculate the target power given the last bid (if any in that case power is 0)
        double targetPower = lastBid == null ? 0 : lastBid.getDemand(newPriceInfo.getCurrentPrice());

        // calculate the currently applicable target power given the last allocation
        double currentTargetPower = getCurrentlyAllocatedPower();

        Date now = new Date(getTimeSource().currentTimeMillis());

        // ignore deviations from the current target below the threshold (as ratio of the max charge speed, e.g. 1�)
        // but only if we have an allocation for the current point in time
        if (getCurrentlyAllocatedPowerOrNull() != null) {
            double updateThreadholdRatio = getProperty("allocation.update.threshold", 0.001d);
            double threshold = controlSpace.getChargeSpeed().getMaximum().doubleValue(WATT) * updateThreadholdRatio;
            if (Math.abs(currentTargetPower - targetPower) < threshold) {
                return null;
            }
        }

        // if we're turning on or off, calculate the time at which we can switch again
        if (currentTargetPower == 0 && targetPower != 0) {
            logDebug("Turning device ON for at least " + controlSpace.getMinOnPeriod());
            mustRunUntil = TimeUtil.add(now, controlSpace.getMinOnPeriod());
        } else if (currentTargetPower != 0 && targetPower == 0) {
            logDebug("Turning device OFF for at least " + controlSpace.getMinOffPeriod());
            mustNotRunUntil = TimeUtil.add(now, controlSpace.getMinOffPeriod());
        }

        // Construct allocation object
        Date allocationEnd = controlSpace.getValidThru();
        Measurable<Duration> duration = Measure.valueOf(allocationEnd.getTime(), MILLI(SECOND));
        Measurable<Energy> targetEnergyVolume = Measure.valueOf(targetPower * (duration.doubleValue(SECOND)), JOULE);

        // return the allocation and remember it
        EnergyProfile energyProfile = EnergyProfile.create().add(duration, targetEnergyVolume).build();
        return lastAllocation = new Allocation(controlSpace, now, energyProfile);
    }

    private boolean isOnByAllocation() {
        return getCurrentlyAllocatedPower() != 0;
    }

    private boolean isOffByAllocation() {
        return getCurrentlyAllocatedPower() == 0;
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
        Measurable<Duration> offsetInAllocation = TimeUtil.difference(lastAllocation.getStartTime(), now);
        // pick the currently applicable element from the allocation
        Element currentAllocElement = lastAllocation.getEnergyProfile().getElementForOffset(offsetInAllocation);

        // allocation not yet active (starts in the future) or already completed (ends in the past)
        if (currentAllocElement == null) {
            return null;
        }

        // return the power we have allocated for the current point in time
        return currentAllocElement.getAveragePower().doubleValue(WATT);
    }

    protected static boolean isConsumingBuffer(BufferControlSpace controlSpace) {
        for (Constraint<Power> c : controlSpace.getChargeSpeed()) {
            double upperWatt = c.getUpperBound().doubleValue(WATT);
            if (upperWatt != 0) {
                return upperWatt > 0;
            }
        }
        // should not get here
        return true;
    }

    protected static boolean isProducingBuffer(BufferControlSpace controlSpace) {
        return !isConsumingBuffer(controlSpace);
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
