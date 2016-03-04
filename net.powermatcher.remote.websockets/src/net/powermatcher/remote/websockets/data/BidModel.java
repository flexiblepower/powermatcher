package net.powermatcher.remote.websockets.data;

import net.powermatcher.api.data.Bid;

/**
 * Bid model class to transfer {@link Bid} data over the wire.
 *
 * @author FAN
 * @version 2.1
 */
public class BidModel {
    /**
     * The marketBasis for bids and prices.
     */
    private MarketBasisModel marketBasis;

    /**
     * Demand array for each price step in the market basis.
     */
    private double[] demand;

    /**
     * Holds the bidNumber.
     */
    private int bidNumber;

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
     * @return the current value of demand.
     */
    public double[] getDemand() {
        return demand;
    }

    public void setDemand(double[] demand) {
        this.demand = demand;
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
}
