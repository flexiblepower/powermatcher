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
    // /**
    // * In the calculatePricePoints, 2 floating points will be compared. The significance is set here.
    // */
    // private static final int PRECISION = 5; // TODO is this relevant?

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

    public static ArrayBidBuilder create(MarketBasis marketBasis) {
        return new ArrayBidBuilder(marketBasis);
    }

    public static PointBidBuilder createWithPricePoints(MarketBasis marketBasis) {
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
     * A copy constructor to create a copy of the given Bid and its bidNumber.
     *
     * @param bid
     *            The {@link Bid} you want to copy.
     */
    public Bid(Bid bid) {
        if (bid.marketBasis == null) {
            throw new IllegalArgumentException("marketBasis is not allowed to be null");
        }
        marketBasis = bid.marketBasis;
        demandArray = Arrays.copyOf(bid.demandArray, bid.demandArray.length);
    }

    // /**
    // * A constructor used to create an instance of this class, based on a {@link PointBid}.
    // *
    // * @param base
    // * The {@link PointBid} this Bid will be based on.
    // */
    // Bid(PointBid base) {
    // marketBasis = base.getMarketBasis();
    //
    // int priceSteps = marketBasis.getPriceSteps();
    // demandArray = new double[priceSteps];
    //
    // for (int ix = 0; ix < priceSteps; ix++) {
    // demandArray[ix] = base.getDemandAt(new PriceStep(marketBasis, ix));
    // }
    //
    // pointBid = base;
    // }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
                                        : new PriceStep(marketBasis, leftIx).toPrice().getPriceValue();
        double rightPrice = leftIx == demandArray.length - 1 ? marketBasis.getMaximumPrice()
                                                             : new PriceStep(marketBasis, rightIx).toPrice()
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
     * {@inheritDoc}
     */
    public double getMaximumDemand() {
        return demandArray[0];
    }

    /**
     * {@inheritDoc}
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
     * Calculates the demand at the intersection in the Bid curve with the priceStep in a demand array.
     *
     * Implementation note: you should always override either this method or the {@link #getDemandAt(PriceStep)} method.
     * The default implementation is to call the other.
     *
     * @param price
     *            the {@link Price} you want to know the demand of.
     * @return the calculated demand
     */
    public double getDemandAt(Price price) {
        return getDemandAt(price.toPriceStep());
    }

    /**
     * Calculates the demand at the intersection with the Bid curve at the given {@link PriceStep}.
     *
     * Implementation note: you should always override either this method or the {@link #getDemandAt(Price)} method. The
     * default implementation is to call the other.
     *
     * @param priceStep
     *            the {@link PriceStep} you want to know the demand of.
     * @return the calculated demand
     */
    public double getDemandAt(final PriceStep priceStep) {
        if (!priceStep.getMarketBasis().equals(marketBasis)) {
            throw new IllegalArgumentException("The marketbasis of the pricestep does not equal this market basis");
        }
        return demandArray[priceStep.getPriceStep()];
    }

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    // /**
    // * @return a {@link PricePoint} <code>array</code> representation of the demand array.
    // */
    // PricePoint[] calculatePricePoints() {
    // int priceSteps = marketBasis.getPriceSteps();
    // List<PricePoint> points = new ArrayList<PricePoint>(priceSteps);
    // /*
    // * Flag to indicate if the last price point is the start of a flat segment.
    // */
    // boolean flatStart = false;
    // /*
    // * Flag to indicate if the last price point is the end point of a flat segment.
    // */
    // boolean flatEnd = false;
    // int i = 0;
    // while (i < priceSteps - 1) {
    // /*
    // * Search for the last price step in the flat segment, if any. At the end of this loop delta is the
    // * difference for the next demand after the flat segment.
    // */
    // PricePoint flatEndPoint = null;
    // double delta = 0.0d;
    // while (i < priceSteps - 1) {
    // delta = demandArray[i] - demandArray[i + 1];
    // if (delta != 0) {
    // break;
    // }
    // i += 1;
    // flatEnd = true;
    // }
    // /*
    // * Add a point at i for the following two cases: 1) If not at the end of the demand array, add the next
    // * point for i and remember that this point is the end of a flat segment. 2) Add a final point if past the
    // * end of the demand array, but the previous point was not the beginning of a flat segment.
    // */
    // if (i < priceSteps - 1 || !flatStart) {
    // flatEndPoint = newPoint(i);
    // points.add(flatEndPoint);
    // flatStart = false;
    // }
    // i += 1;
    //
    // // TODO Refactor this code to not nest more than 3
    // // if/for/while/switch/try statements.
    // /*
    // * If not at the end of the demand array, check if the following segment is a step or an inclining segment.
    // */
    // if (i < priceSteps - 1) {
    // if (Math.abs((demandArray[i] - demandArray[i + 1]) - delta) < Math.pow(10, PRECISION * -1)) {
    // /*
    // * Here i is in a constantly inclining or declining segment. Search for the last price step in the
    // * segment.
    // */
    // while (i < priceSteps - 1
    // && Math.abs((demandArray[i] - demandArray[i + 1]) - delta) < Math.pow(10,
    // PRECISION * -1)) {
    // i += 1;
    // }
    // /*
    // * If not at the end of the demand array, add the end point for the segment.
    // */
    // if (i < priceSteps - 1) {
    // points.add(newPoint(i));
    // i += 1;
    // }
    // } else if (flatEnd && flatEndPoint != null) {
    // /*
    // * If i is not in a constantly inclining or declining segment, and the previous segment was flat,
    // * then move the end point of the flat segment one price step forward to convert it to a straight
    // * step.
    // *
    // * This is to preserve the semantics of the straight step when converting between point and vector
    // * representation and back.
    // */
    // flatEndPoint = newPoint(i);
    // }
    // }
    //
    // flatStart = endSegment(priceSteps, points, flatStart, i);
    // }
    //
    // return points.toArray(new PricePoint[points.size()]);
    // }
    //
    // /**
    // * Adds a point at location for the following two cases: 1) Add a point for the start of the next flat segment and
    // * loop. 2) Add a final point if the last step of the demand array is the end of an inclining or declining
    // segment.
    // *
    // * @param priceSteps
    // * @param points
    // * @param flatStart
    // * @param location
    // * @return boolean indicating if the nextPoint is the start of a new flat segment
    // */
    // private boolean endSegment(int priceSteps, List<PricePoint> points, boolean flatStart, int location) {
    // if (location == priceSteps - 1
    // || (location < priceSteps - 1 && demandArray[location] - demandArray[location + 1] == 0)) {
    // points.add(newPoint(location));
    // return true;
    // }
    // return false;
    // }
    //
    // /**
    // * Created a {@link PricePoint} based on a given priceStep value.
    // *
    // * @param priceStep
    // * the pricestep used to calculate the {@link PriceStep} and determin the demand that belongs to that
    // * value
    // * @return a {@link PricePoint}, based on on the priceStep paramater.
    // */
    // private PricePoint newPoint(int priceStep) {
    // return new PricePoint(new PriceStep(marketBasis, priceStep).toPrice(), demandArray[priceStep]);
    // }

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
