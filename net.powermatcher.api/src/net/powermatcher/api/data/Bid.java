package net.powermatcher.api.data;

/**
 * This immutable abstract data object defines the basis for a Bid in the Powermatcher cluster. The bid curve can be
 * represented in several ways, so subclasses will have their own implementation.
 *
 * @author FAN
 * @version 2.0
 */
public abstract class Bid {
    /**
     * The smallest difference between demands, for them to be called different.
     */
    protected static final double SMALLEST_DEMAND = 1e-6;

    protected static boolean demandIsEquals(double demand1, double demand2) {
        return Math.abs(demand1 - demand2) < SMALLEST_DEMAND;
    }

    public static Bid flatDemand(MarketBasis marketBasis, int bidNumber, double demand) {
        return new PointBid.Builder(marketBasis).bidNumber(bidNumber).add(0, demand).build();
    }

    /**
     * The {@link MarketBasis} of the cluster.
     */
    protected final MarketBasis marketBasis;

    /**
     * The number or id of this Bid instance.
     */
    protected final int bidNumber;

    /**
     * A constructor used to create an instance of this class.
     *
     * @param marketBasis
     *            the {@link MarketBasis} of the cluster.
     * @param bidNumber
     *            the number of this Bid instance.
     */
    protected Bid(MarketBasis marketBasis, int bidNumber) {
        if (marketBasis == null) {
            throw new IllegalArgumentException("marketBasis is not allowed to be null");
        }
        this.marketBasis = marketBasis;
        this.bidNumber = bidNumber;
    }

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    /**
     * @return the current value of bidNumber
     */
    public int getBidNumber() {
        return bidNumber;
    }

    /**
     * Aggregates this {@link Bid} instance with another Bid. Bid are aggregated by adding their bid curves.
     *
     * @param other
     *            The {@link Bid} whose bid curve has to be added to the bid curve of this instance.
     * @return A new aggregated Bid.
     */
    public abstract Bid aggregate(final Bid other);

    /**
     * Calculates the {@link Price} at the intersection with the bid curve at the given demand.
     *
     * @param targetDemand
     *            the part of the bid curve you want to get the {@link Price} of.
     * @return the calculated{@link Price}
     */
    public abstract Price calculateIntersection(double targetDemand);

    /**
     * @return the highest demand in the demand curve.
     */
    public abstract double getMaximumDemand();

    /**
     * @return the lowest demand in the demand curve.
     */
    public abstract double getMinimumDemand();

    /**
     * @return The {@link ArrayBid} implementation of this Bid.
     */
    public abstract ArrayBid toArrayBid();

    /**
     * @return The {@link PointBid} implementation of this Bid.
     */
    public abstract PointBid toPointBid();

    /**
     * Calculates the demand at the intersection with the bid curve at the given {@link PriceStep}.
     *
     * Implementation note: you should always override either this method or the {@link #getDemandAt(Price)} method. The
     * default implementation is to call the other.
     *
     * @param priceStep
     *            the {@link PriceStep} you want to know the demand of.
     * @return the calculated demand
     */
    public double getDemandAt(PriceStep priceStep) {
        return getDemandAt(priceStep.toPrice());
    }

    /**
     * Calculates the demand at the intersection in the bid curve with the priceStep in a demand array.
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
}
