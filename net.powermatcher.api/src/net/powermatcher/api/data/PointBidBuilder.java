package net.powermatcher.api.data;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A builder class to create an {@link PointBid} instance.
 *
 * @author FAN
 * @version 2.0
 */
public final class PointBidBuilder {

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

        for (int ix = 0; ix < priceSteps; ix++) {
            demandArray[ix] = getDemandAt(new PriceStep(marketBasis, ix).toPrice());
        }
        return new Bid(marketBasis, demandArray);
    }

    private double getDemandAt(Price price) {
        PricePoint[] pricePoints = this.pricePoints.toArray(new PricePoint[this.pricePoints.size()]);
        Arrays.sort(pricePoints);
        double minimumDemand = pricePoints[0].getDemand();
        double maximumDemand = pricePoints[pricePoints.length - 1].getDemand();

        if (pricePoints.length == 1) {
            // Flat bid, send any demand (they are all the same)
            return maximumDemand;
        } else if (price.compareTo(pricePoints[0].getPrice()) < 0) {
            // If the price is lower than the lowest price, return the maximum
            // demand
            return maximumDemand;
        } else if (price.equals(pricePoints[0].getPrice())) {
            // If the first matcher, it could be that the second is at the same price. If that is the case, use the
            // second, otherwise the first.
            PricePoint secondPricePoint = pricePoints[1];
            if (price.equals(secondPricePoint.getPrice())) {
                return secondPricePoint.getDemand();
            } else {
                return maximumDemand;
            }
        } else if (price.compareTo(pricePoints[pricePoints.length - 1].getPrice()) >= 0) {
            // If the price is higher than the highest price, return the minimum
            // demand
            return minimumDemand;
        } else {
            // We have a normal case that is somewhere in between the lower and higher demands

            // First determine which 2 pricepoints it is in between
            int lowIx = 0, highIx = pricePoints.length;
            while (highIx - lowIx > 1) {
                int middleIx = (lowIx + highIx) / 2;
                PricePoint middle = pricePoints[middleIx];

                int cmp = middle.getPrice().compareTo(price);
                if (cmp < 0) {
                    lowIx = middleIx;
                } else if (cmp > 0) {
                    highIx = middleIx;
                } else {
                    // Found at least 1 point that is equal in price.
                    // This is the special case with an open and closed node. Always the lower demand should be chosen.
                    PricePoint nextPoint = pricePoints[middleIx + 1];
                    if (price.equals(nextPoint.getPrice())) {
                        return nextPoint.getDemand();
                    } else {
                        middle.getDemand();
                    }
                }
            }
            PricePoint lower = pricePoints[lowIx];
            PricePoint higher = pricePoints[highIx];

            // Now calculate the demand between the 2 points
            // First the factor (between 0 and 1) of where the price is on the line
            double factor = (price.getPriceValue() - lower.getPrice().getPriceValue())
                            / (higher.getPrice().getPriceValue() - lower.getPrice().getPriceValue());
            // Now calculate the demand
            return (1 - factor) * lower.getDemand() + factor * higher.getDemand();
        }
    }
}
