package net.powermatcher.api.data;

import java.util.Arrays;

/**
 * This immutable data object represents a {@link Bid} with a <code>double</code> demand array to represent the bid
 * curve. This is used because it is easy to perform calculations with.
 *
 * @author FAN
 * @version 2.0
 */
public class Bid {

    /**
     * The {@link MarketBasis} of the cluster.
     */
    protected final MarketBasis marketBasis;

    /**
     * The array of <code>double</code> values that make up the bid curve.
     */
    private final double[] demandArray;

    /**
     * This method checks to see it the given demand array is descending.
     *
     * @param demandArray
     *            The demand Array that has to be checked.
     * @throws IllegalArgumentException
     *             is the demand is not ascending.
     */
    static void checkDescending(double[] demandArray) {
        double last = Double.POSITIVE_INFINITY;
        for (double demand : demandArray) {
            if (demand > last) {
                throw new IllegalArgumentException("The demand can not be ascending");
            }
            last = demand;
        }
    }

    /**
     * The smallest difference between demands, for them to be called different.
     */
    protected static final double SMALLEST_DEMAND = 1e-6;

    /**
     * Tests if the difference between the 2 demand values is less that {@link #SMALLEST_DEMAND}.
     *
     * @param demand1
     *            The first demand
     * @param demand2
     *            The second demand
     * @return true when the difference between the 2 demand values is less than {@link #SMALLEST_DEMAND}
     */
    protected static boolean demandIsEqual(double demand1, double demand2) {
        return Math.abs(demand1 - demand2) < SMALLEST_DEMAND;
    }

    /**
     * Creates a new flat {@link Bid} with a given demand.
     *
     * @param marketBasis
     *            The {@link MarketBasis} on which the {@link Bid} should be based
     * @param demand
     *            The constant demand value
     * @return A new {@link Bid} that represents a flat bid
     */
    public static Bid flatDemand(MarketBasis marketBasis, double demand) {
        return new PointBidBuilder(marketBasis).add(marketBasis.getMinimumPrice(), demand).build();
    }

    /**
     * Construct a new {@link Bid} using the Builder pattern with the {@link ArrayBidBuilder}.
     *
     * @param marketBasis
     *            {@link MarketBasis} to be used for the Bid.
     * @return An instance of the {@link ArrayBidBuilder}
     */
    public static ArrayBidBuilder createUsingArray(MarketBasis marketBasis) {
        return new ArrayBidBuilder(marketBasis);
    }

    /**
     * Construct a new {@link Bid} using the Builder pattern with the {@link PointBidBuilder}.
     *
     * @param marketBasis
     *            {@link MarketBasis} to be used for the Bid.
     * @return An instance of the {@link PointBidBuilder}
     */
    public static PointBidBuilder create(MarketBasis marketBasis) {
        return new PointBidBuilder(marketBasis);
    }

    /**
     * A constructor to create an instance of Bid.
     *
     * @param marketBasis
     *            the {@link MarketBasis} of the cluster
     * @param demandArray
     *            the demandArray that belongs to this bid.
     */
    public Bid(MarketBasis marketBasis, double... demandArray) {
        if (marketBasis == null) {
            throw new IllegalArgumentException("marketBasis is not allowed to be null");
        }
        this.marketBasis = marketBasis;
        if (demandArray.length != marketBasis.getPriceSteps()) {
            throw new IllegalArgumentException("Length of the demandArray is not equal to the number of price steps");
        }
        checkDescending(demandArray);
        this.demandArray = Arrays.copyOf(demandArray, demandArray.length);
    }

    /**
     * Adds this {@link Bid} with another and creates a new {@link Bid}.
     *
     * @param other
     *            The {@link Bid} this should be added to
     * @return A new {@link Bid} object
     */
    public Bid aggregate(Bid other) {
        if (!other.marketBasis.equals(marketBasis)) {
            throw new IllegalArgumentException("The marketbasis of the supplied bid does not equal this market basis");
        }

        double[] aggregatedDemand = other.getDemand();
        for (int i = 0; i < aggregatedDemand.length; i++) {
            aggregatedDemand[i] += demandArray[i];
        }
        return new Bid(marketBasis, aggregatedDemand);
    }

    /**
     * Calculates the {@link Price} for which the demand is closest to targetDemand (expressed in Watts).
     *
     * @param targetDemand
     *            The demand expressed in watts
     * @return The {@link Price} for which the demand in this {@link Bid} is closests to the targetDemand
     */
    public Price calculateIntersection(double targetDemand) {
        int leftIx = 0, rightIx = demandArray.length - 1;

        // First test for a few special cases
        if (targetDemand > demandArray[leftIx]) {
            // If the target is higher than the maximum of the bid, return the minimum price
            return new Price(marketBasis, marketBasis.getMinimumPrice());
        } else if (targetDemand < demandArray[rightIx]) {
            // If the target is lower than the minimum of the bid, return the maximum price
            return new Price(marketBasis, marketBasis.getMaximumPrice());
        } else if (demandIsEqual(targetDemand, demandArray[leftIx])) {
            rightIx = leftIx;
        } else if (demandIsEqual(targetDemand, demandArray[rightIx])) {
            leftIx = rightIx;
        } else { // demand is between the limits of this bid, which can not be flat at this point
            // Go on while there is at least 1 point between the left and right index
            while (rightIx - leftIx > 1) {
                // Determine the middle between the 2 boundaries
                int middleIx = (leftIx + rightIx) / 2;
                double middleDemand = demandArray[middleIx];

                if (demandIsEqual(targetDemand, middleDemand)) {
                    // A point with the target demand is found, select this point
                    leftIx = middleIx;
                    rightIx = middleIx;
                } else if (middleDemand > targetDemand) {
                    // If the middle demand is bigger than the target demand, we set the left to the middle
                    leftIx = middleIx;
                } else { // middleDemand < targetDemand
                    // If the middle demand is smaller than the target demand, we set the right to the middle
                    rightIx = middleIx;
                }
            }
        }

        // If the left or right point matches the targetDemand, expand the range
        while (leftIx > 0 && demandIsEqual(targetDemand, demandArray[leftIx - 1])) {
            leftIx--;
        }
        while (rightIx < demandArray.length - 1 && demandIsEqual(targetDemand, demandArray[rightIx + 1])) {
            rightIx++;
        }

        return interpolate(leftIx, rightIx, targetDemand);
    }

    private Price interpolate(int leftIx, int rightIx, double targetDemand) {
        double leftPrice = rightIx == 0 ? marketBasis.getMinimumPrice()
                                        : Price.fromPriceIndex(marketBasis, leftIx).getPriceValue();
        double rightPrice = leftIx == demandArray.length - 1 ? marketBasis.getMaximumPrice()
                                                             : Price.fromPriceIndex(marketBasis, rightIx)
                                                                    .getPriceValue();

        double leftDemand = demandArray[leftIx];
        double rightDemand = demandArray[rightIx];

        double demandFactor = demandIsEqual(leftDemand, rightDemand) ? 0.5
                                                                     : (leftDemand - targetDemand)
                                                                       / (leftDemand - rightDemand);
        double price = leftPrice + (rightPrice - leftPrice) * demandFactor;

        return new Price(marketBasis, price);
    }

    /**
     * @return the maximum demand (expressed in watts) in this bid
     */
    public double getMaximumDemand() {
        return demandArray[0];
    }

    /**
     * @return the minimum demand (expressed in watts) in this bid
     */
    public double getMinimumDemand() {
        return demandArray[demandArray.length - 1];
    }

    /**
     * @return a copy of the demand array.
     */
    public double[] getDemand() {
        return Arrays.copyOf(demandArray, demandArray.length);
    }

    /**
     * Calculates the demand at the intersection in the Bid curve with the Price in a demand array.
     *
     * @param price
     *            the {@link Price} you want to know the demand of.
     * @return the calculated demand
     */
    public double getDemandAt(Price price) {
        if (!price.getMarketBasis().equals(marketBasis)) {
            throw new IllegalArgumentException("The marketbasis of the pricestep does not equal this market basis");
        }
        return demandArray[price.getPriceIndex()];
    }

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    /**
     * Subtract the other bid curve from this bid curve. Subtract is the inverse of aggregate. The other bid does not
     * have to be based on the same market basis.
     *
     * @param other
     *            The other (<code>Bid</code>) parameter.
     * @return A copy of this bid with the other bid subtracted from it.
     */
    public Bid subtract(final Bid other) {
        double[] otherDemand = other.getDemand();
        double[] newDemand = getDemand();
        for (int i = 0; i < newDemand.length; i++) {
            newDemand[i] -= otherDemand[i];
        }
        return new Bid(marketBasis, newDemand);
    }

    /**
     * Transpose the bid curve by adding an offset to the demand.
     *
     * @param offset
     *            The offset (<code>double</code>) parameter.
     * @return The {@link Bid} that has been shifted.
     */
    public Bid transpose(final double offset) {
        double[] newDemand = getDemand();
        for (int i = 0; i < newDemand.length; i++) {
            newDemand[i] += offset;
        }
        return new Bid(marketBasis, newDemand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 2011 * Arrays.hashCode(demandArray) + marketBasis.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof Bid)) {
            return false;
        } else {
            Bid other = (Bid) obj;
            return marketBasis.equals(other.marketBasis)
                   && Arrays.equals(other.getDemand(), getDemand());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("Bid [");

        for (double demand : demandArray) {
            b.append(MarketBasis.DEMAND_FORMAT.format(demand)).append(',');
        }
        b.setLength(b.length() - 1);
        b.append(']');
        return b.toString();
    }
}
