package net.powermatcher.api.data;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Price implements Comparable<Price> {
	private final MarketBasis marketBasis;
	private final double price;

	public Price(MarketBasis marketBasis, double price) {
		if(marketBasis == null) {
			throw new NullPointerException("marketBasis");
		} else if(Double.isNaN(price)) {
			throw new IllegalArgumentException("Price NaN is not valid");
		} else if(price < marketBasis.getMinimumPrice() || price > marketBasis.getMaximumPrice()) {
			throw new IllegalArgumentException("Price " + price + " is out of bounds [" 
												+ marketBasis.getMinimumPrice() + ", " + marketBasis.getMaximumPrice() +"]");
		}
		this.marketBasis = marketBasis;
		this.price = price;
	}

	public MarketBasis getMarketBasis() {
		return marketBasis;
	}
	
	public double getPrice() {
		return price;
	}
	
	public PriceStep toPriceStep() {
		double priceStep = (price - marketBasis.getMinimumPrice()) / marketBasis.getPriceIncrement();
        return new PriceStep(marketBasis, Math.round((float) priceStep));
	}

	@Override
	public int hashCode() {
		return 83257 * marketBasis.hashCode() + 50723 * Double.hashCode(price); 
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		} else {
			Price other = (Price) obj;
			return marketBasis.equals(other.marketBasis) && price == other.price;
		}
	}
	
    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    @Override
	public String toString() {
		return PRICE_FORMAT.format(price);
	}
    
    @Override
    public int compareTo(Price o) {
    	if(!marketBasis.equals(o.marketBasis)) {
    		throw new IllegalArgumentException("Non-equal market basis");
    	} else if(price < o.price) {
    		return -1;
    	} else if(price > o.price) {
    		return 1;
    	} else {
    		return 0;
    	}
    }
}
