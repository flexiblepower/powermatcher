package net.powermatcher.api.data;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This immutable data object represents a price in the powermatcher cluster.
 * 
 * @author FAN
 * @version 2.0
 */
public class Price implements Comparable<Price> {
    /**
     * The {@link MarketBasis} of this cluster.
     */
    private final MarketBasis marketBasis;

    /**
     * the <code>double</code> value of this Price instance.
     */
    private final double priceValue;

    /**
     * A constructor to create an instance of Price.
     * 
     * @param marketBasis
     *            the {@link MarketBasis} of this Price.
     * @param price
     *            the value of this Price.
     */
    public Price(MarketBasis marketBasis, double price) {
        if (marketBasis == null) {
            throw new IllegalArgumentException("marketBasis not allowed to be null");
        } else if (Double.isNaN(price)) {
            throw new IllegalArgumentException("Price NaN is not valid");
        } else if (price < marketBasis.getMinimumPrice() || price > marketBasis.getMaximumPrice()) {
            throw new IllegalArgumentException("Price " + price + " is out of bounds [" + marketBasis.getMinimumPrice()
                    + ", " + marketBasis.getMaximumPrice() + "]");
        }
        this.marketBasis = marketBasis;
        this.priceValue = price;
    }

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    /**
     * @return the current value of priceValue.
     */
    public double getPriceValue() {
        return priceValue;
    }

    /**
     * @return The {@link PriceStep} representation of this Price.
     */
    public PriceStep toPriceStep() {
        double priceStep = (priceValue - marketBasis.getMinimumPrice()) / marketBasis.getPriceIncrement();
        return new PriceStep(marketBasis, Math.round((float) priceStep));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 83257 * marketBasis.hashCode() + 50723 * Double.valueOf(priceValue).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            Price other = (Price) obj;
            return marketBasis.equals(other.marketBasis) && priceValue == other.priceValue;
        }
    }

    /**
     * The {@link DecimalFormat} used to format the <code>String</code> representation of this instance's priceValue.
     */
    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.##",
            DecimalFormatSymbols.getInstance(Locale.ROOT));

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Price{priceValue=" + PRICE_FORMAT.format(priceValue) + "}";
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     * 
     * This method compares the priceValues of both instances. The one with the highest value is the greatest.
     * 
     * @param that
     *            The {@link Price} instance you want to compare with this one.
     * 
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object.
     * @throws IllegalArgumentException
     *             is the {@link MarketBasis} is <code>null</code>.
     */
    @Override
    public int compareTo(Price that) {
        if (!marketBasis.equals(that.marketBasis)) {
            throw new IllegalArgumentException("Non-equal market basis");
        } else if (priceValue < that.priceValue) {
            return -1;
        } else if (priceValue > that.priceValue) {
            return 1;
        } else {
            return 0;
        }
    }
}
