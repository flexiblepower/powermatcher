package net.powermatcher.extensions.connectivity.websockets.data;

import net.powermatcher.api.data.MarketBasis;

/**
 * MarketBasis model class to transfer {@link MarketBasis} data over the wire.
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

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getPriceSteps() {
        return priceSteps;
    }

    public void setPriceSteps(int priceSteps) {
        this.priceSteps = priceSteps;
    }

    public double getMinimumPrice() {
        return minimumPrice;
    }

    public void setMinimumPrice(double minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    public double getMaximumPrice() {
        return maximumPrice;
    }

    public void setMaximumPrice(double maximumPrice) {
        this.maximumPrice = maximumPrice;
    }
}
