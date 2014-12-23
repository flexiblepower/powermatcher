package net.powermatcher.extensions.connectivity.websockets.data;

import net.powermatcher.api.data.Bid;

/**
 * Bid model class to transfer {@link Bid} data over the wire.
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
     * Price points in a bid curve .
     */
    private PricePointModel[] pricePoints;

    /**
     * Holds the bidNumber.
     */
    private int bidNumber;

    public MarketBasisModel getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasisModel marketBasis) {
        this.marketBasis = marketBasis;
    }

    public double[] getDemand() {
        return demand;
    }

    public void setDemand(double[] demand) {
        this.demand = demand;
    }

    public PricePointModel[] getPricePoints() {
        return pricePoints;
    }

    public void setPricePoints(PricePointModel[] pricePoints) {
        this.pricePoints = pricePoints;
    }

    public int getBidNumber() {
        return bidNumber;
    }

    public void setBidNumber(int bidNumber) {
        this.bidNumber = bidNumber;
    }
}
