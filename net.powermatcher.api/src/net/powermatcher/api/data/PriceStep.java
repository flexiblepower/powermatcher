package net.powermatcher.api.data;

public class PriceStep {
	private final MarketBasis marketBasis;
	private final int priceStep;

	public PriceStep(MarketBasis marketBasis, int priceStep) {
		if(marketBasis == null) {
			throw new NullPointerException("marketBasis can not be null");
		} else if(priceStep < 0 || priceStep >= marketBasis.getPriceSteps()) {
			throw new IllegalArgumentException("PriceStep " + priceStep + " is out of bounds [0, " + marketBasis.getPriceSteps() +")");
		}
		this.marketBasis = marketBasis;
		this.priceStep = priceStep;
	}
	
	public MarketBasis getMarketBasis() {
		return marketBasis;
	}
	
	public int getPriceStep() {
		return priceStep;
	}
	
	public Price toPrice() {
		return new Price(marketBasis, marketBasis.getMinimumPrice() + priceStep * marketBasis.getPriceIncrement());
	}

	@Override
	public int hashCode() {
		return 16333 * marketBasis.hashCode() + 3557 * priceStep;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		} else {
			PriceStep other = (PriceStep) obj;
			return marketBasis.equals(other.marketBasis) && priceStep == other.priceStep;
		}
	}
	
	@Override
	public String toString() {
		return "PriceStep " + Integer.toString(priceStep);
	}
}
