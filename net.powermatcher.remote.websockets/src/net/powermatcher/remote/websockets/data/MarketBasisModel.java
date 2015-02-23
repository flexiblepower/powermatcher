package net.powermatcher.remote.websockets.data;

import net.powermatcher.api.data.MarketBasis;

/**
 * MarketBasis model class to transfer {@link MarketBasis} data over the wire.
 * 
 * @author FAN
 * @version 2.0
 */
public class MarketBasisModel {
    /**
     * Define the commodity (String) field.
     */
    private String commodity;

    /**
     * Define the currency (String) field.
     */
    private String currency;

    /**
     * Define the price steps (int) field.
     */
    private int priceSteps;

    /**
     * Define the minimum price (double) field.
     */
    private double minimumPrice;

    /**
     * Define the maximum price (double) field.
     */
    private double maximumPrice;

    /**
     * @return the current value of commodity.
     */
    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    /**
     * @return the current value of currency.
     */
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * @return the current value of priceSteps.
     */
    public int getPriceSteps() {
        return priceSteps;
    }

    public void setPriceSteps(int priceSteps) {
        this.priceSteps = priceSteps;
    }

    /**
     * @return the current value of minimumPrice.
     */
    public double getMinimumPrice() {
        return minimumPrice;
    }

    public void setMinimumPrice(double minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    /**
     * @return the current value of maximumPrice.
     */
    public double getMaximumPrice() {
        return maximumPrice;
    }

    public void setMaximumPrice(double maximumPrice) {
        this.maximumPrice = maximumPrice;
    }
}
