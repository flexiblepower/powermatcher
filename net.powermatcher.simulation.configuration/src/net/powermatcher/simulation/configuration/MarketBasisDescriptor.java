package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "market_basis")
public class MarketBasisDescriptor {

	private String commodity;
	private String currency;

	private double maximumPrice;
	private double minimumPrice;

	private int priceSteps;

	@XmlElement(name = "commodity")
	public String getCommodity() {
		return commodity;
	}

	@XmlElement(name = "currency")
	public String getCurrency() {
		return currency;
	}

	@XmlElement(name = "maximum_price")
	public double getMaximumPrice() {
		return maximumPrice;
	}

	@XmlElement(name = "minimum_price")
	public double getMinimumPrice() {
		return minimumPrice;
	}

	@XmlElement(name = "price_steps")
	public int getPriceSteps() {
		return priceSteps;
	}

	public void setCommodity(String commodity) {
		this.commodity = commodity;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setMaximumPrice(double maximumPrice) {
		this.maximumPrice = maximumPrice;
	}

	public void setMinimumPrice(double minimumPrice) {
		this.minimumPrice = minimumPrice;
	}

	public void setPriceSteps(int priceSteps) {
		this.priceSteps = priceSteps;
	}

}
