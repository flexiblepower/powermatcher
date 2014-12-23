package net.powermatcher.api.data;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Price implements Comparable<Price> {
    private final MarketBasis marketBasis;
    private final double priceValue;

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

    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    public double getPriceValue() {
        return priceValue;
    }

    public PriceStep toPriceStep() {
        double priceStep = (priceValue - marketBasis.getMinimumPrice()) / marketBasis.getPriceIncrement();
        return new PriceStep(marketBasis, Math.round((float) priceStep));
    }

    @Override
    public int hashCode() {
        return 83257 * marketBasis.hashCode() + 50723 * Double.valueOf(priceValue).hashCode();
    }

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

    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.##",
            DecimalFormatSymbols.getInstance(Locale.ROOT));

    @Override
    public String toString() {
        return "Price{priceValue=" + PRICE_FORMAT.format(priceValue) + "}";
    }

    @Override
    public int compareTo(Price o) {
        if (!marketBasis.equals(o.marketBasis)) {
            throw new IllegalArgumentException("Non-equal market basis");
        } else if (priceValue < o.priceValue) {
            return -1;
        } else if (priceValue > o.priceValue) {
            return 1;
        } else {
            return 0;
        }
    }
}
