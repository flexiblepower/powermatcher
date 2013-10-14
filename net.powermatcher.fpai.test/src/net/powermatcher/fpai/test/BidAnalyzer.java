package net.powermatcher.fpai.test;

import static javax.measure.unit.SI.WATT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import javax.measure.Measurable;
import javax.measure.quantity.Power;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;

public class BidAnalyzer {
    public static void assertFlatBid(BidInfo bid) {
        assertNotNull(bid);
        assertTrue(demandIsFlat(bid.getDemand()));
    }

    public static void assertFlatBidWithValue(BidInfo bid, Measurable<Power> p) {
        assertNotNull("Expected a bid, but was null", bid);
        double[] demand = bid.getDemand();
        assertTrue("Expected a flat bid with value " + p + ", but it is not flat", demandIsFlat(demand));
        assertTrue("Expected a flat bid with value " + p + ", but found value of " + demand[0] + "W",
                   demand[0] == p.doubleValue(WATT));
    }

    public static void assertNonFlatBid(BidInfo bid) {
        assertNotNull(bid);
        assertFalse("Expected a non-flat bid, but it was flat with " + bid.getDemand()[0] + "W",
                    demandIsFlat(bid.getDemand()));
    }

    /** Asserts that the given bid has a single step. */
    public static void assertStepBid(BidInfo bid) {
        assertNotNull(bid);
        assertTrue(isStepBid(bid, null, null, null));
    }

    public static void assertDemandAtMost(BidInfo bid, Measurable<Power> demand) {
        assertNotNull(bid);
        double demandWatt = demand.doubleValue(WATT);
        for (double v : bid.getDemand()) {
            assertTrue(demandWatt >= v);
        }
    }

    public static void assertDemandAtLeast(BidInfo bid, Measurable<Power> demand) {
        assertNotNull(bid);
        double demandWatt = demand.doubleValue(WATT);
        for (double v : bid.getDemand()) {
            assertTrue("Demand in bid may not be lower than " + demandWatt + " for any price, (was " + v + ")",
                       demandWatt <= v);
        }
    }

    /**
     * Asserts that the given bid has a single step with power1 as value before and power2 as value after the step, and
     * the step at the given price.
     * 
     * @param bid
     *            The bid to assert that it has a step.
     * @param power1
     *            The power before the step.
     * @param power2
     *            The power after the step.
     * @param price
     *            The price at which the bid has a step.
     */
    public static void assertStepBid(BidInfo bid, Measurable<Power> power1, Measurable<Power> power2, Double price) {
        assertNotNull(bid);
        assertTrue(isStepBid(bid, power1, power2, price));
    }

    private static boolean isStepBid(BidInfo bid, Measurable<Power> power1, Measurable<Power> power2, Double price) {
        assertNotNull(bid);

        double[] demand = bid.getDemand();

        // check the first power value
        if (power1 != null && power1.doubleValue(WATT) != demand[0]) {
            return false;
        }

        int priceIndex = 1;

        // detect the first change in the demand array
        for (; priceIndex < demand.length; priceIndex++) {
            if (demand[priceIndex] != demand[priceIndex - 1]) {
                break;
            }
        }

        // if at the end of the demand array, it's a flat bid (without a step)
        if (priceIndex >= demand.length - 1) {
            return false;
        }

        // if the price is set, check it, or not if it isn't
        if (price != null) {
            return bid.getMarketBasis().toPrice(priceIndex) == price;
        }

        priceIndex += 1;

        // check the second power value
        if (power2 != null && power2.doubleValue(WATT) != demand[priceIndex]) {
            return false;
        }

        // check that after the first difference, the remainer is flat
        for (; priceIndex < demand.length; priceIndex++) {
            if (demand[priceIndex] != demand[priceIndex - 1]) {
                return false;
            }
        }

        return true;
    }

    public static double getStepPrice(BidInfo bid) {
        assertStepBid(bid);

        PricePoint[] pricePoints = bid.getPricePoints();
        if (pricePoints != null) {
            return bid.getMarketBasis().toPrice(pricePoints[0].getNormalizedPrice());
        }

        int priceIndex = 1;
        double[] demand = bid.getDemand();

        for (; priceIndex < demand.length; priceIndex++) {
            if (demand[priceIndex] != demand[priceIndex - 1]) {
                break;
            }
        }

        return bid.getMarketBasis().toPrice(priceIndex);
    }

    /**
     * Checks if the bid is not entirely null
     * 
     * @param bid
     */
    public static void assertDemandBid(BidInfo bid) {
        assertNotNull(bid);

        double[] demand = bid.getDemand();
        assertTrue(demand.length >= 1);
        boolean notzero = false;
        for (double v : demand) {
            if (v > 0) {
                notzero = true;
                break;
            }
        }
        assertTrue(notzero);
    }

    private static boolean demandIsFlat(double[] demand) {
        return demandIsFlat(demand, 0, demand.length);
    }

    private static boolean demandIsFlat(double[] demand, int fromIndex, int toIndex) {
        assertNotNull(demand);

        if (toIndex - fromIndex <= 0) {
            return false;
        } else if (toIndex - fromIndex == 1) {
            return true;
        }

        for (int i = 1; i < demand.length; i++) {
            if (demand[i] != demand[0]) {
                return false;
            }
        }
        return true;
    }

    public static void assertBidsEqual(BidInfo bid1, BidInfo bid2) {
        assertTrue(bid1 != null && bid2 != null);
        double[] demand1 = bid1.getDemand();
        double[] demand2 = bid2.getDemand();
        for (int i = 0; i < demand1.length; i++) {
            assertEquals(demand1[i], demand2[i], 0.0001);
        }
    }

}
