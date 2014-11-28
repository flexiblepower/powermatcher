package net.powermatcher.extensions.connectivity.websockets.data;

public class PricePointModel {
    /**
     * Define the normalized price (int) field.
     */
    private int normalizedPrice;
    /**
     * Define the demand (double) field.
     */
    private double demand;
    
	public int getNormalizedPrice() {
		return normalizedPrice;
	
	}
	public void setNormalizedPrice(int normalizedPrice) {
		this.normalizedPrice = normalizedPrice;
	}
	
	public double getDemand() {
		return demand;
	}
	
	public void setDemand(double demand) {
		this.demand = demand;
	}
}