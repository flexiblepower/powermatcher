package net.powermatcher.api.data;

/**
 * PricePoint represents a Price and the demand that goes with it.
 *
 * @author FAN
 * @version 2.0
 */
public class PricePoint
    implements Comparable<PricePoint> {
    /**
     * The {@link Price} value of this instance.
     */
    private final Price price;

    /**
     * The <code>double</code> value of the demand
     */
    private final double demand;

    /**
     * A constructor to create an instance of PricePoint.
     *
     * @param price
     *            the {@link Price} value
     * @param demand
     *            the <code>double</code> value of the demand
     */
    public PricePoint(Price price, double demand) {
        if (Double.isNaN(demand)) {
            throw new IllegalArgumentException("Can not create a PricePoint for a NaN demand");
        } else if (Double.isInfinite(demand)) {
            throw new IllegalArgumentException("Can not create a PricePoint for an infinite demand");
        }

        this.price = price;
        this.demand = demand;
    }

    /**
     * A copy constructor used to create a PricePoint with a new {@link MarketBasis}.
     *
     * @param marketBasis
     *            the new {@link MarketBasis}
     * @param price
     *            the {@link Price} value
     * @param demand
     *            the <code>double</code> value of the demand
     */
    public PricePoint(MarketBasis marketBasis, double price, double demand) {
        this(new Price(marketBasis, price), demand);
    }

    /**
     * @return the current value of price.
     */
    public Price getPrice() {
        return price;
    }

    /**
     * @return the current value of demand.
     */
    public double getDemand() {
        return demand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            PricePoint other = (PricePoint) obj;
            return other.price.equals(price) && other.demand == demand;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * price.hashCode() + Double.valueOf(demand).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{" + MarketBasis.PRICE_FORMAT.format(price.getPriceValue())
               + " -> "
               + MarketBasis.DEMAND_FORMAT.format(demand)
               + "}";
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * This method first compares the {@link Price} values of both {@link PricePoint}s. Then it compares the demand
     * values of both instances. The one with the highest value is the greatest.
     *
     * @param that
     *            The {@link PricePoint} instance you want to compare with this one.
     *
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     */
    @Override
    public int compareTo(PricePoint that) {
        int cmpPrice = price.compareTo(that.price);
        if (cmpPrice != 0) {
            return cmpPrice;
        } else if (demand < that.demand) {
            return 1;
        } else if (demand > that.demand) {
            return -1;
        } else {
            return 0;
        }
    }
}
