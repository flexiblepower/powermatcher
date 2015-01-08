package net.powermatcher.extensions.connectivity.websockets.data;

import net.powermatcher.api.data.PricePoint;

/**
 * PricePoint model class to transfer {@link PricePoint} data over the wire.
 * 
 * @author FAN
 * @version 2.0
 */
public class PricePointModel {
	/**
	 * Define the price (double) field.
	 */
	private double price;
	/**
	 * Define the demand (double) field.
	 */
	private double demand;

	/**
	 * @return the current value of price.
	 */
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * @return the current value of demand.
	 */
	public double getDemand() {
		return demand;
	}

	public void setDemand(double demand) {
		this.demand = demand;
	}
}
