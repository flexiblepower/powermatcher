package net.powermatcher.extensions.connectivity.websockets.data;

public class PriceUpdateModel {
	
    /**
     * Define the market basis (MarketBasis) field.
     */
    private MarketBasisModel marketBasis;

    /**
     * Define the bidNumber field.
     */
    private int bidNumber;
    
    /**
     * Define the current price (double) field.
     */
    private double priceValue;

	public MarketBasisModel getMarketBasis() {
		return marketBasis;
	}

	public void setMarketBasis(MarketBasisModel marketBasis) {
		this.marketBasis = marketBasis;
	}

	public int getBidNumber() {
		return bidNumber;
	}

	public void setBidNumber(int bidNumber) {
		this.bidNumber = bidNumber;
	}
	
	public double getPriceValue() {
		return priceValue;
	}

	public void setPriceValue(double priceValue) {
		this.priceValue = priceValue;
	}
}