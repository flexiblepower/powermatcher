package net.powermatcher.extensions.connectivity.websockets.data;

public class PriceModel {
	/**
	 * Define the cluster id field
	 */
	private String clusterId;
	
    /**
     * Define the market basis (MarketBasis) field.
     */
    private MarketBasisModel marketBasis;

    /**
     * Define the current price (double) field.
     */
    private double priceValue;

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public MarketBasisModel getMarketBasis() {
		return marketBasis;
	}

	public void setMarketBasis(MarketBasisModel marketBasis) {
		this.marketBasis = marketBasis;
	}

	public double getPriceValue() {
		return priceValue;
	}

	public void setPriceValue(double priceValue) {
		this.priceValue = priceValue;
	}
}