package net.powermatcher.fpai.agent;

import static javax.measure.unit.SI.WATT;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Power;
import javax.measure.unit.SI;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;

import org.flexiblepower.rai.values.Constraint;
import org.flexiblepower.rai.values.ConstraintList;
import org.flexiblepower.rai.values.ConstraintList.Builder;

public class BidUtil {

    private BidUtil() {
    }

    /**
     * Create a flat bid with a demand of 0 Watt
     */
    public static BidInfo zeroBid(MarketBasis marketBasis) {
        return createFlatBid(marketBasis, Measure.valueOf(0, SI.WATT));
    }

    /**
     * Create a flat/must run bid the provided demand
     */
    public static BidInfo createFlatBid(MarketBasis marketBasis, Measurable<Power> demand) {
        return new BidInfo(marketBasis, new PricePoint(0, demand.doubleValue(WATT)));
    }

    public static BidInfo roundBidToPowerConstraintList(BidInfo bid, ConstraintList<Power> pcl, boolean includeZero) {
        if (includeZero) {
            // Make a clone of the PowerConstraintList that includes a demand of 0 Watt
            Builder<Power> builder = ConstraintList.create(WATT);
            for (Constraint<Power> c : pcl) {
                builder.addRange(c.getLowerBound(), c.getUpperBound());
            }
            builder.addSingle(Measure.valueOf(0, WATT));
            pcl = builder.build();
        }
        double[] demand = bid.getDemand();
        for (int i = 0; i < demand.length; i++) {
            demand[i] = roundToPowerConstraintList(pcl, Measure.valueOf(demand[i], WATT)).doubleValue(WATT);
        }
        return new BidInfo(bid.getMarketBasis(), demand);
    }

    public static Measurable<Power>
            roundToPowerConstraintList(ConstraintList<Power> pcl, Measurable<Power> wantedPower) {
        Measurable<Power> resultValue = null;
        double result = Double.NaN;
        double wantedPowerWatt = wantedPower.doubleValue(WATT);

        for (Constraint<Power> pc : pcl) {
            Measurable<Power> powerValue = pc.getClosestValue(wantedPower);
            double powerWatt = powerValue.doubleValue(WATT);

            if (resultValue == null || (Math.abs(powerWatt - wantedPowerWatt) < Math.abs(result - wantedPowerWatt))) {
                resultValue = powerValue;
                result = powerWatt;
            }
        }

        return resultValue;
    }

    /**
     * Transform a bid so that it has at least a demand of minDemandWatt for every price.
     * 
     * @param bid
     *            The bid to transform
     * @param minDemandWatt
     *            The minimum demand which is allowed (will be adjusted to the given power constraint list)
     * @return The transformed bid wherein any demand in the original bid is adjusted so that it is not below the given
     *         minimum demand
     */
    public static BidInfo setMinimumDemand(BidInfo bid, Measurable<Power> minDemand) {
        double[] demand = bid.getDemand();
        double minDemandWatt = minDemand.doubleValue(WATT);
        for (int i = 0; i < demand.length; i++) {
            demand[i] = Math.max(demand[i], minDemandWatt);
        }

        return new BidInfo(bid.getMarketBasis(), demand);
    }

    /**
     * Transform a bid so that it has at most a demand of maxDemandWatt for every price.
     * 
     * @param bid
     *            The bid to transform
     * @param maxDemandWatt
     *            The maximum demand which is allowed (will be adjusted to the given power constraint list)
     * @return The transformed bid wherein any demand in the original bid is adjusted so that it is not above the given
     *         maximum demand (as rounded to the nearest allowed power value in the given constraint list).
     */
    public static BidInfo setMaximumDemand(BidInfo bid, Measurable<Power> maxDemand) {
        double[] demand = bid.getDemand();
        double maxDemandWatt = maxDemand.doubleValue(WATT);
        for (int i = 0; i < demand.length; i++) {
            demand[i] = Math.min(demand[i], maxDemandWatt);
        }

        return new BidInfo(bid.getMarketBasis(), demand);
    }

}
