package net.powermatcher.websockets.data;

public class PriceModel {
    /**
     * Define the market basis (MarketBasis) field.
     */
    private MarketBasisModel marketBasis;

    /**
     * Define the current price (double) field.
     */
    private double currentPrice;

	public MarketBasisModel getMarketBasis() {
		return marketBasis;
	}

	public void setMarketBasis(MarketBasisModel marketBasis) {
		this.marketBasis = marketBasis;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}
}