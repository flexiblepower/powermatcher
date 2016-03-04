package net.powermatcher.api.data;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A builder class to create an {@link Bid} instance.
 *
 * @author FAN
 * @version 2.1
 */
public final class PointBidBuilder {

    /**
     * Epsilon for correcting rounding errors
     */
    private static final double EPSILON = 0.0000000001;

    /**
     * The {@link MarketBasis} of the cluster.
     */
    private final MarketBasis marketBasis;

    /**
     * The set of {@link PointBid} values that make up the bid curve.
     */
    private final SortedSet<PricePoint> pricePoints;

    /**
     * Constructor to create an instance of this class.
     *
     * @param marketBasis
     *            the {@link MarketBasis} of the cluster.
     */
    public PointBidBuilder(final MarketBasis marketBasis) {
        if (marketBasis == null) {
            throw new IllegalArgumentException("marketBasis is not allowed to be null");
        }
        this.marketBasis = marketBasis;
        pricePoints = new TreeSet<PricePoint>();
    }

    /**
     * Adds the supplied pricePoint the PricePoint array.
     *
     * @param pricePoint
     *            The point to add
     * @return this instance of the Builder with the array
     */
    public PointBidBuilder add(PricePoint pricePoint) {
        pricePoints.add(pricePoint);
        return this;
    }

    /**
     * Adds the supplied pricePoints the PricePoint array.
     *
     * @param pricePoints
     *            Array of PricePoints to add
     *
     * @return this instance of the Builder with the array
     */
    public PointBidBuilder addAll(PricePoint[] pricePoints) {
        for (PricePoint pp : pricePoints) {
            this.pricePoints.add(pp);
        }
        return this;
    }

    /**
     * Creates a PricePoint with the supplied price and demand. Adds the point to the PricePoint array.
     *
     * @param price
     *            The price of the point that should be added
     * @param demand
     *            The demand value of the point that should be added
     * @return this instance of the Builder with the array
     */
    public PointBidBuilder add(double price, double demand) {
        return add(new PricePoint(marketBasis, price, demand));
    }

    /**
     * Uses the supplied parameters to create a new PointBid.
     *
     * @return The created {@link PointBid}
     * @throws IllegalArgumentException
     *             when the marketBasis is null
     */
    public Bid build() {
        int priceSteps = marketBasis.getPriceSteps();
        double[] demandArray = new double[priceSteps];

        PricePoint[] sortedPricePoints = pricePoints.toArray(new PricePoint[pricePoints.size()]);
        // Sort from low price to high price
        Arrays.sort(sortedPricePoints);

        double last = Double.POSITIVE_INFINITY;
        for (int ix = 0; ix < priceSteps; ix++) {
            demandArray[ix] = getDemandAt(Price.fromPriceIndex(marketBasis, ix), sortedPricePoints);
            // Ensure we still have a (not strictly) descending array when rounding issues occur
            if (demandArray[ix] > last && demandArray[ix] - EPSILON < last) {
                // Second value is higher, but not significantly. Fix this by using the last value.
                demandArray[ix] = last;
            }
            last = demandArray[ix];
        }
        return new Bid(marketBasis, demandArray);
    }

    private double getDemandAt(Price price, PricePoint[] sortedPricePoints) {
        double demandMinimumPrice = sortedPricePoints[0].getDemand();
        double demandMaximumPrice = sortedPricePoints[sortedPricePoints.length - 1].getDemand();

        if (demandMinimumPrice == demandMaximumPrice) {
            // Flat bid, send any demand (they are all the same)
            return demandMaximumPrice;
        } else if (price.compareTo(sortedPricePoints[0].getPrice()) < 0) {
            // If the price is lower than the lowest price, return the maximum
            // demand
            return demandMinimumPrice;
        } else if (price.compareTo(sortedPricePoints[sortedPricePoints.length - 1].getPrice()) >= 0) {
            // If the price is higher than the highest price, return the minimum demand
            return demandMaximumPrice;
        } else {
            // We have a normal case that is somewhere in between the lower and higher demands

            // First determine which 2 pricepoints it is in between
            int lowIx = 0, highIx = sortedPricePoints.length;
            while (highIx - lowIx > 1) {
                int middleIx = (lowIx + highIx) / 2;
                PricePoint middle = sortedPricePoints[middleIx];

                int cmp = middle.getPrice().compareTo(price);
                if (cmp < 0) {
                    lowIx = middleIx;
                } else if (cmp > 0) {
                    highIx = middleIx;
                } else {
                    // Found at least 1 point that is equal in price.
                    // This is the special case with an open and closed node. Always the lower demand should be chosen.
                    // We are going to look for the last node with price 'price'
                    int currentIx = middleIx;
                    while (currentIx + 1 < sortedPricePoints.length
                           && sortedPricePoints[currentIx + 1].getPrice().equals(price)) {
                        currentIx++;
                    }
                    return sortedPricePoints[currentIx].getDemand();
                }
            }
            PricePoint lower = sortedPricePoints[lowIx];
            PricePoint higher = sortedPricePoints[highIx];

            // Now calculate the demand between the 2 points
            // First the factor (between 0 and 1) of where the price is on the line
            double factor = (price.getPriceValue() - lower.getPrice().getPriceValue())
                            / (higher.getPrice().getPriceValue() - lower.getPrice().getPriceValue());
            // Now calculate the demand
            return (1 - factor) * lower.getDemand() + factor * higher.getDemand();
        }
    }
}
