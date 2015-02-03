package net.powermatcher.api.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Checksum;

/**
 * This immutable data object represents a {@link Bid} with a <code>double</code> demand array to represent the bid
 * curve. This is used because it is easy to perform calculations with.
 *
 * @author FAN
 * @version 2.0
 */
public class ArrayBid
    extends Bid {

    /**
     * In the calculatePricePoints, 2 floating points will be compared. The significance is set here.
     */
    private static int PRECISION = 5;

    /**
     * The array of <code>double</code> values that make up the bid curve.
     */
    private final double[] demandArray;

    /**
     * The {@link PointBid} representation of this ArrayBid.
     */
    private transient PointBid pointBid;

    /**
     * A builder class to create an {@link ArrayBid} instance.
     *
     * @author FAN
     * @version 2.0
     */
    public static final class Builder {

        /**
         * The {@link MarketBasis} of the cluster.
         */
        private final MarketBasis marketBasis;

        /**
         * The number or id of this Bid instance.
         */
        private int bidNumber;

        /**
         * The index of the next item the demand array.
         */
        private int nextIndex;

        /**
         * The demandArray that will be filled in this builder.
         */
        private double[] builderDemand;

        /**
         * Constructor to create an instance of this class.
         *
         * @param marketBasis
         *            the {@link MarketBasis} of the cluster.
         */
        public Builder(MarketBasis marketBasis) {
            this.marketBasis = marketBasis;
            bidNumber = 0;
            nextIndex = 0;
            builderDemand = new double[marketBasis.getPriceSteps()];
        }

        /**
         * Sets the bidNumber with the specified bidNumber
         *
         * @param bidNumber
         * @return this instance of the Builder with the set bidNumber
         */
        public Builder setBidNumber(int bidNumber) {
            this.bidNumber = bidNumber;
            return this;
        }

        /**
         * Adds a demand <code>double</code> to builderDemand.
         *
         * @param demand
         *            The demand to be added to the demand array. Must not be higher than the previous added demand.
         * @return this instance of the Builder, with the added demand
         * @throws IllegalArgumentException
         *             when the demand to be added is higher than the demand already in the array
         * @throws ArrayIndexOutOfBoundsException
         *             when the demand array is already full
         */
        public Builder setDemand(double demand) {
            checkIndex(nextIndex);
            if (nextIndex > 0 && demand > builderDemand[nextIndex - 1]) {
                throw new IllegalArgumentException("The demand can not be ascending");
            }
            builderDemand[nextIndex++] = demand;
            return this;
        }

        /**
         * {@link Checksum} to see if the given index is not bigger than the length of builderDemand.
         *
         * @param ix
         *            the index that has to be checked
         * @throws ArrayIndexOutOfBoundsException
         *             if the given index is bigger than the length of builderDemand.
         */
        private void checkIndex(int ix) {
            if (ix >= builderDemand.length) {
                throw new ArrayIndexOutOfBoundsException("Demand array has already been filled to maximum");
            }
        }

        /**
         * Sets the demandArray with the supplied demand array. The supplied array must not be ascending. The length of
         * the array should be the same size as the number of price steps in the marketBasis.
         *
         * @param demand
         *            The new demand array
         * @return this instance of the Builder, with the added array
         * @throws IllegalArgumentException
         *             if the size of the array differs from the number of priceSteps in the MarketBasis or if the
         *             demand array is ascending
         */
        public Builder setDemandArray(double[] demand) {
            if (demand.length != marketBasis.getPriceSteps()) {
                throw new IllegalArgumentException(
                                                   "supplied array is not same size as number of priceSteps in MarketBasis");
            }
            checkDescending(demand);
            builderDemand = Arrays.copyOf(demand, demand.length);
            nextIndex = builderDemand.length;
            return this;
        }

        /**
         * Makes sure the whole array is filled, then creates the ArrayBid with the Builder's internal values
         *
         * @return The created ArrayBid
         * @throws IllegalArgumentException
         *             When the length of the demandArray is not equal to the number of price steps
         */
        public ArrayBid build() {
            fillArrayToPriceStep(builderDemand.length);
            return new ArrayBid(marketBasis, bidNumber, builderDemand);
        }

        /**
         * fills the demand array from the nextIndex until the designated priceStep with the last set demand Will do
         * nothing on an already filled array.
         *
         * @param priceStep
         *            The priceStep to fill to.
         * @return this instance of the Builder, with the filled demand array
         * @throws IllegalStateException
         *             when nextIndex is zero (no demand has been set, yet)
         * @throws IllegalArgumentException
         *             when the supplied priceStep is higher than the number of price steps in the marketBasis
         */
        public Builder fillArrayToPriceStep(int priceStep) {
            if (nextIndex == 0) {
                throw new IllegalStateException("Demand array contains no demand that can be extended");
            }
            if (priceStep > marketBasis.getPriceSteps()) {
                throw new IllegalArgumentException("The supplied priceStep is out of bounds");
            }
            double demand = builderDemand[nextIndex - 1];
            while (nextIndex < priceStep) {
                builderDemand[nextIndex++] = demand;
            }
            return this;
        }
    }

    /**
     * This method checks to see it the given demand array is descending.
     *
     * @param demandArray
     *            The demand Array that has to be checked.
     * @throws IllegalArgumentException
     *             is the demand is not ascending.
     */
    private static void checkDescending(double[] demandArray) {
        double last = Double.POSITIVE_INFINITY;
        for (double demand : demandArray) {
            if (demand > last) {
                throw new IllegalArgumentException("The demand can not be ascending");
            }
            last = demand;
        }
    }

    /**
     * A constructor to create an instance of ArrayBid.
     *
     * @param marketBasis
     *            the {@link MarketBasis} of the cluster
     * @param bidNumber
     *            the number of this ArrayBid instance
     * @param demandArray
     *            the demandArray that belongs to this bid.
     */
    public ArrayBid(MarketBasis marketBasis, int bidNumber, double[] demandArray) {
        super(marketBasis, bidNumber);
        if (demandArray.length != marketBasis.getPriceSteps()) {
            throw new IllegalArgumentException("Length of the demandArray is not equal to the number of price steps");
        }
        checkDescending(demandArray);
        this.demandArray = Arrays.copyOf(demandArray, demandArray.length);
    }

    /**
     * A copy constructor to create a copy of the given ArrayBid and its bidNumber.
     *
     * @param bid
     *            The {@link ArrayBid} you want to copy.
     * @param the
     *            bidnumber you want the new ArrayBid to have.
     */
    public ArrayBid(ArrayBid bid, int bidNumber) {
        super(bid.marketBasis, bidNumber);
        demandArray = Arrays.copyOf(bid.demandArray, bid.demandArray.length);
    }

    /**
     * A copy constructor to create a copy of the given {@link ArrayBid}.
     *
     * @param bid
     *            The {@link ArrayBid} you want to copy.
     */
    public ArrayBid(ArrayBid bid) {
        super(bid.marketBasis, bid.bidNumber);
        demandArray = Arrays.copyOf(bid.demandArray, bid.demandArray.length);
    }

    /**
     * A constructor used to create an instance of this class, based on a {@link PointBid}.
     *
     * @param base
     *            The {@link PointBid} this ArrayBid will be based on.
     */
    ArrayBid(PointBid base) {
        super(base.getMarketBasis(), base.getBidNumber());
        demandArray = base.calculateDemandArray();
        pointBid = base;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayBid aggregate(Bid other) {
        if (!other.marketBasis.equals(marketBasis)) {
            throw new IllegalArgumentException("The marketbasis of the supplied bid does not equal this market basis");
        }
        ArrayBid otherBid = other.toArrayBid();

        double[] aggregatedDemand = otherBid.getDemand();
        for (int i = 0; i < aggregatedDemand.length; i++) {
            aggregatedDemand[i] += demandArray[i];
        }
        return new ArrayBid(marketBasis, 0, aggregatedDemand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Price calculateIntersection(final double targetDemand) {
        int leftBound = 0;
        int rightBound = demandArray.length - 1;
        int middle = rightBound / 2;
        while (leftBound < middle) {
            if (demandArray[middle] > targetDemand) {
                leftBound = middle;
            } else {
                rightBound = middle;
            }
            middle = (leftBound + rightBound) / 2;
        }

        rightBound = determineDropoff(targetDemand, rightBound);

        int priceStep;
        if (leftBound > 0 && rightBound < demandArray.length) {
            priceStep = interpolate(targetDemand, leftBound, rightBound);
        } else {
            priceStep = determineCurve(targetDemand, leftBound, rightBound);
        }

        return new PriceStep(marketBasis, priceStep).toPrice();
    }

    /**
     * Find the point where the demand falls below the target demand.
     *
     * @param targetDemand
     *            The demand you want to know the dropoff value of.
     * @param rightBound
     *            the method will check for a dropoff up untill this index.
     * @return the point where the demand drops below the given target demand.
     */
    private int determineDropoff(final double targetDemand, int rightBound) {
        while (rightBound < demandArray.length && demandArray[rightBound] >= targetDemand) {
            rightBound += 1;
        }
        return rightBound;
    }

    /**
     * The index of the point which is just above the intersection is now stored in the variable 'middle'. This means
     * that middle + 1 is the index of the point that lies just under the intersection. That means that the exact
     * intersection is between middle and middle+1, hence a weighted interpolation is needed.
     *
     * @param targetDemand
     * @param leftBound
     * @param rightBound
     * @return The interpolated priceStep
     */
    private int interpolate(final double targetDemand, int leftBound, int rightBound) {
        int priceStep;
        double interpolation = ((demandArray[leftBound] - targetDemand) / (demandArray[leftBound] - demandArray[rightBound]))
                               * (rightBound - leftBound);
        priceStep = leftBound + (int) Math.ceil(interpolation);
        return priceStep;
    }

    /**
     * Pricing for boundary cases: 1) If the curve is a flat line, if the demand is positive the price will become the
     * maximum price, or if the demand is zero or negative, the price will become 0. 2) If the curve is a single step,
     * the price of the stepping point (the left or the right boundary).
     *
     * @param targetDemand
     * @param leftBound
     * @param rightBound
     * @return
     */
    private int determineCurve(final double targetDemand, int leftBound, int rightBound) {
        int priceStep;
        if (leftBound == 0 && rightBound == demandArray.length) {
            /* Curve is flat line or single step at leftBound */
            if (demandArray[leftBound] != 0) {
                /*
                 * Curve is flat line for non-zero demand or single step at leftBound
                 */
                if (demandArray[leftBound + 1] == 0) {
                    /* Curve is a single step at leftBound */
                    priceStep = leftBound + 1;
                } else {
                    /* Curve is flat line for non-zero demand */
                    priceStep = rightBound - 1;
                }
            } else {
                /* Curve is flat line for zero demand 0 */
                priceStep = new Price(marketBasis, 0).toPriceStep().getPriceStep();
            }
        } else {
            /* Curve is a single step at leftBound */
            priceStep = demandArray[leftBound] <= targetDemand ? leftBound : leftBound + 1;
        }
        return priceStep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaximumDemand() {
        return demandArray[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinimumDemand() {
        return demandArray[demandArray.length - 1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayBid toArrayBid() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PointBid toPointBid() {
        if (pointBid == null) {
            pointBid = new PointBid(this);
        }
        return pointBid;
    }

    /**
     * @return a copy of the demand array.
     */
    public double[] getDemand() {
        return Arrays.copyOf(demandArray, demandArray.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getDemandAt(final PriceStep priceStep) {
        if (!priceStep.getMarketBasis().equals(marketBasis)) {
            throw new IllegalArgumentException("The marketbasis of the pricestep does not equal this market basis");
        }
        int boundPriceStep = marketBasis.boundPriceStep(priceStep);
        return demandArray[boundPriceStep];
    }

    /**
     * @return a {@link PricePoint} <code>array</code> representation of the demand array.
     */
    PricePoint[] calculatePricePoints() {
        int priceSteps = marketBasis.getPriceSteps();
        List<PricePoint> points = new ArrayList<PricePoint>(priceSteps);
        /*
         * Flag to indicate if the last price point is the start of a flat segment.
         */
        boolean flatStart = false;
        /*
         * Flag to indicate if the last price point is the end point of a flat segment.
         */
        boolean flatEnd = false;
        int i = 0;
        while (i < priceSteps - 1) {
            /*
             * Search for the last price step in the flat segment, if any. At the end of this loop delta is the
             * difference for the next demand after the flat segment.
             */
            PricePoint flatEndPoint = null;
            double delta = 0.0d;
            while (i < priceSteps - 1 && (delta = demandArray[i] - demandArray[i + 1]) == 0) {
                i += 1;
                flatEnd = true;
            }
            /*
             * Add a point at i for the following two cases: 1) If not at the end of the demand array, add the next
             * point for i and remember that this point is the end of a flat segment. 2) Add a final point if past the
             * end of the demand array, but the previous point was not the beginning of a flat segment.
             */
            if (i < priceSteps - 1 || !flatStart) {
                flatEndPoint = newPoint(i);
                points.add(flatEndPoint);
                flatStart = false;
            }
            i += 1;

            // TODO Refactor this code to not nest more than 3
            // if/for/while/switch/try statements.
            /*
             * If not at the end of the demand array, check if the following segment is a step or an inclining segment.
             */
            if (i < priceSteps - 1) {
                if (Math.abs((demandArray[i] - demandArray[i + 1]) - delta) < Math.pow(10, PRECISION * -1)) {
                    /*
                     * Here i is in a constantly inclining or declining segment. Search for the last price step in the
                     * segment.
                     */
                    while (i < priceSteps - 1
                           && Math.abs((demandArray[i] - demandArray[i + 1]) - delta) < Math.pow(10,
                                                                                                 PRECISION * -1)) {
                        i += 1;
                    }
                    /*
                     * If not at the end of the demand array, add the end point for the segment.
                     */
                    if (i < priceSteps - 1) {
                        points.add(newPoint(i));
                        i += 1;
                    }
                } else if (flatEnd && flatEndPoint != null) {
                    /*
                     * If i is not in a constantly inclining or declining segment, and the previous segment was flat,
                     * then move the end point of the flat segment one price step forward to convert it to a straight
                     * step.
                     *
                     * This is to preserve the semantics of the straight step when converting between point and vector
                     * representation and back.
                     */
                    flatEndPoint = newPoint(i);
                }
            }

            flatStart = endSegment(priceSteps, points, flatStart, i);
        }

        return points.toArray(new PricePoint[points.size()]);
    }

    /**
     * Adds a point at location for the following two cases: 1) Add a point for the start of the next flat segment and
     * loop. 2) Add a final point if the last step of the demand array is the end of an inclining or declining segment.
     *
     * @param priceSteps
     * @param points
     * @param flatStart
     * @param location
     * @return boolean indicating if the nextPoint is the start of a new flat segment
     */
    private boolean endSegment(int priceSteps, List<PricePoint> points, boolean flatStart, int location) {
        if (location == priceSteps - 1
            || (location < priceSteps - 1 && demandArray[location] - demandArray[location + 1] == 0)) {
            points.add(newPoint(location));
            return true;
        }
        return false;
    }

    /**
     * Created a {@link PricePoint} based on a given priceStep value.
     *
     * @param priceStep
     *            the pricestep used to calculate the {@link PriceStep} and determin the demand that belongs to that
     *            value
     * @return a {@link PricePoint}, based on on the priceStep paramater.
     */
    private PricePoint newPoint(int priceStep) {
        return new PricePoint(new PriceStep(marketBasis, priceStep).toPrice(), demandArray[priceStep]);
    }

    /**
     * Subtract the other bid curve from this bid curve. Subtract is the inverse of aggregate. The other bid does not
     * have to be based on the same market basis.
     *
     * @param other
     *            The other (<code>Bid</code>) parameter.
     * @return A copy of this bid with the other bid subtracted from it.
     */
    public ArrayBid subtract(final ArrayBid other) {
        double[] otherDemand = other.getDemand();
        double[] newDemand = getDemand();
        for (int i = 0; i < newDemand.length; i++) {
            newDemand[i] -= otherDemand[i];
        }
        return new ArrayBid(marketBasis, bidNumber, newDemand);
    }

    /**
     * Transpose the bid curve by adding an offset to the demand.
     *
     * @param offset
     *            The offset (<code>double</code>) parameter.
     */
    public ArrayBid transpose(final double offset) {
        double[] newDemand = getDemand();
        for (int i = 0; i < newDemand.length; i++) {
            newDemand[i] += offset;
        }
        return new ArrayBid(marketBasis, bidNumber, newDemand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 2011 * Arrays.hashCode(demandArray) + 3557 * bidNumber + marketBasis.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof ArrayBid)) {
            return false;
        } else {
            ArrayBid other = (ArrayBid) obj;
            return other.bidNumber == bidNumber && marketBasis.equals(other.marketBasis)
                   && Arrays.equals(other.getDemand(), getDemand());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("ArrayBid{bidNumber=").append(bidNumber);

        double[] demand = getDemand();
        if (demand != null) {
            b.append(", demand[]{");
            for (int i = 0; i < demand.length; i++) {
                if (i > 0) {
                    b.append(',');
                }
                b.append(MarketBasis.DEMAND_FORMAT.format(demand[i]));
            }
            b.append("}, ");
        }
        b.append(marketBasis);
        b.append('}');
        return b.toString();
    }
}
