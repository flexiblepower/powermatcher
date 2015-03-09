package net.powermatcher.peakshaving;

import java.util.Arrays;
import java.util.Map;

import javax.measure.Measurable;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceStep;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.core.concentrator.SentBidInformation;
import net.powermatcher.core.concentrator.TransformingConcentrator;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * <p>
 * This class represents a {@link PeakShavingConcentrator} component where several instances can be created.
 * </p>
 *
 * <p>
 * The {@link PeakShavingConcentrator} receives {@link Bid} from the agents and forwards this in an aggregate
 * {@link Bid} up in the hierarchy to a {@link PeakShavingConcentrator} or to the {@link Auctioneer}. It will receive
 * price updates from the {@link Auctioneer} and forward them to its connected agents. TODO: add PeakShavingConcentrator
 * comment.
 *
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = PeakShavingConcentrator.Config.class,
           immediate = true,
           provide = { ObservableAgent.class,
                      MatcherEndpoint.class,
                      AgentEndpoint.class,
                      TransformingConcentrator.class,
                      PeakShavingConcentrator.class })
public class PeakShavingConcentrator
    extends Concentrator
    implements TransformingConcentrator {

    @Meta.OCD
    public static interface Config
        extends Concentrator.Config {
        @Override
        @Meta.AD(deflt = "auctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "600", description = "Nr of seconds before a bid becomes invalidated")
        int bidTimeout();

        @Override
        @Meta.AD(deflt = "60", description = "Number of seconds between bid updates")
        long bidUpdateRate();

        @Override
        @Meta.AD(deflt = "peakshavingconcentrator")
        String agentId();

        @Meta.AD(deflt = "-50000", description = "The floor constraint in Watt", required = false)
        Double floor();

        @Meta.AD(deflt = "50000", description = "The ceiling constraint in Watt", required = false)
        Double ceiling();
    }

    /**
     * Minimum power level applied in 'peak shaving'
     */
    protected double floor = -Double.MAX_VALUE;

    /**
     * Maximum power level applied in 'peak shaving'
     */
    protected double ceiling = Double.MAX_VALUE;

    /**
     * Measured flow as reported via the peak shaving interface
     */
    protected volatile double measuredFlow = Double.NaN;

    /**
     * The allocated flow as calculated from the aggregated bidcurve
     */
    protected volatile double allocatedFlow = Double.NaN;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Override
    @Activate
    public void activate(final Map<String, ?> properties) {
        activate(Configurable.createConfigurable(Config.class, properties));
    }

    /**
     * Convenient activate method that takes a {@link Config} object. This also makes subclassing easier.
     *
     * @param config
     *            The {@link Config} object that configures this concentrator
     */
    public void activate(Config config) {
        if (config.floor() != null) {
            floor = config.floor();
        } else {
            floor = -Double.MAX_VALUE;
        }

        if (config.ceiling() != null) {
            ceiling = config.ceiling();
        } else {
            ceiling = Double.MAX_VALUE;
        }

        if (ceiling <= floor) {
            throw new IllegalArgumentException("The floor constraint shouldn't be higher than the ceiling constraint");
        }

        measuredFlow = Double.NaN;

        super.activate(config);
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Override
    @Deactivate
    public void deactivate() {
        super.deactivate();
    }

    @Override
    protected Bid transformBid(Bid aggregatedBid) {
        ArrayBid bid = aggregatedBid.toArrayBid();
        double uncontrolledFlow = getUncontrolledFlow();

        if (!Double.isNaN(uncontrolledFlow)) {
            bid = bid.transpose(uncontrolledFlow);
        }
        bid = clip(bid);
        if (!Double.isNaN(uncontrolledFlow)) {
            bid = bid.transpose(-uncontrolledFlow);
        }

        return bid;
    }

    @Override
    protected Price transformPrice(Price price, SentBidInformation info) {
        // Find the transformedBid that has the same bidnumber as the bid
        Bid originalBid = info.getOriginalBid();
        Bid transformedBid = info.getSentBid();

        PriceStep priceStep = price.toPriceStep();
        double transformedDemand = transformedBid.getDemandAt(priceStep);
        double realDemand = originalBid.getDemandAt(priceStep);

        if (transformedDemand < realDemand) {
            // Increase the price step until this is no longer true
            while (transformedDemand < realDemand && !priceStep.isMaximum()) {
                priceStep = priceStep.increment();
                realDemand = originalBid.getDemandAt(priceStep);
            }
        } else if (transformedDemand > realDemand) {
            // Decrease the price step until this is no longer true
            while (transformedDemand > realDemand && !priceStep.isMinimum()) {
                priceStep = priceStep.decrement();
                realDemand = originalBid.getDemandAt(priceStep);
            }
        }

        allocatedFlow = originalBid.getDemandAt(priceStep);

        return priceStep.toPrice();
    }

    /**
     * @return the difference between the measured flow and the allocation for the cluster. If either the measurement or
     *         the allocation is not available, this method will return Double.NaN.
     */
    protected double getUncontrolledFlow() {
        return measuredFlow - allocatedFlow;
    }

    /**
     * Clip a bid such that no power value in the bid exceeds the ceiling or the floor. Any value in the resulting bid
     * will have resulted from the given bid (i.e. no new power level values will have been introduced).
     *
     * @param bid
     *            The bid to clip.
     * @return The clipped bid.
     */
    private ArrayBid clip(final ArrayBid bid) {
        double[] demand = bid.getDemand();

        // find start of unclipped region
        int start = findFirstIndexOfUnclippedRegion(demand, ceiling);

        // if there is no unclipped region we use the last (lowest value)
        if (start == -1) {
            Arrays.fill(demand, demand[demand.length - 1]);
        } else {
            // replace part above ceiling with first point in unclipped region
            double firstUnclippedPoint = demand[start];
            for (int i = 0; i < start; i++) {
                demand[i] = firstUnclippedPoint;
            }

            int end = findLastIndexOfUnclippedRegion(demand, floor);

            if (end == -1) {
                // if there is no unclipped region we use the first (highest value)
                Arrays.fill(demand, demand[0]);
            } else {
                // replace part below floor with last point in unclipped region
                double lastUnclippedPoint = demand[end];
                for (int i = end + 1; i < demand.length; i++) {
                    demand[i] = lastUnclippedPoint;
                }
            }
        }

        return new ArrayBid(bid.getMarketBasis(), demand);
    }

    /**
     * Finds the first index in a demand function for which the allocation doesn't exceed the given ceiling. Starting at
     * the lowest price (index), the first index of the region which won't be clipped is the first value in the demand
     * function which is lower than or equal to the ceiling.
     *
     * @param demandFunction
     *            The demand function as an array of power flow values, where positive flow is demand.
     * @param floor
     *            The ceiling which defines the maximum value the unclipped region has.
     * @return The first index of the unclipped region or -1 if there is no region which isn't below the ceiling.
     */
    private int findFirstIndexOfUnclippedRegion(final double[] demandFunction,
                                                final double ceiling) {
        for (int i = 0; i < demandFunction.length; i++) {
            if (demandFunction[i] <= ceiling) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Finds the first index in a demand function for which the allocation doesn't exceed the given floor; the last
     * index of the region which won't be clipped is the last value in the demand function which is greater than or
     * equal to the ceiling.
     *
     * @param demandFunction
     *            The demand function as an array of power flow values, where positive flow is demand.
     * @param floor
     *            The floor which defines the minimum value the unclipped region has.
     * @return The last index of the unclipped region or -1 if there is no region which isn't above the floor.
     */
    private int findLastIndexOfUnclippedRegion(final double[] demandFunction,
                                               final double floor) {
        for (int i = demandFunction.length - 1; i >= 0; i--) {
            if (demandFunction[i] >= floor) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void setMeasuredFlow(Measurable<Power> measuredFlow) {
        this.measuredFlow = measuredFlow.doubleValue(SI.WATT);
    }
}
