package net.powermatcher.remote.websockets.data;

import net.powermatcher.api.messages.PriceUpdate;

/**
 * PriceUpdate model class to transfer {@link PriceUpdate} data over the wire.
 * 
 * @author FAN
 * @version 2.0
 */
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

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasisModel getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasisModel marketBasis) {
        this.marketBasis = marketBasis;
    }

    /**
     * @return the current value of bidNumber.
     */
    public int getBidNumber() {
        return bidNumber;
    }

    public void setBidNumber(int bidNumber) {
        this.bidNumber = bidNumber;
    }

    /**
     * @return the current value of priceValue.
     */
    public double getPriceValue() {
        return priceValue;
    }

    public void setPriceValue(double priceValue) {
        this.priceValue = priceValue;
    }
}
