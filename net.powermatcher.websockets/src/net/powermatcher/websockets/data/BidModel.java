package net.powermatcher.websockets.data;

import net.powermatcher.api.data.PricePoint;

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

	public void convertPricePoints(PricePoint[] pricePoints) {
		this.pricePoints = new PricePointModel[pricePoints.length];
		for (int i = 0; i < pricePoints.length; i++) {
			this.pricePoints[i] = new PricePointModel(); 
			this.pricePoints[i].setDemand(pricePoints[i].getDemand());
			this.pricePoints[i].setNormalizedPrice(pricePoints[i].getNormalizedPrice());
		}
	}

	public int getBidNumber() {
		return bidNumber;
	}

	public void setBidNumber(int bidNumber) {
		this.bidNumber = bidNumber;
	}
}
