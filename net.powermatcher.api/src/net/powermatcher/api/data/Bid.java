package net.powermatcher.api.data;

public abstract class Bid {
	public static Bid flatDemand(MarketBasis marketBasis, int bidNumber, double demand) {
		return new PointBid.Builder(marketBasis).bidNumber(bidNumber).add(0, demand).build();
	}
	
	protected final MarketBasis marketBasis;
	
	protected final int bidNumber;
	
	protected Bid(MarketBasis marketBasis, int bidNumber) {
		if(marketBasis == null) {
			throw new NullPointerException("marketBasis");
		}
		this.marketBasis = marketBasis;
		this.bidNumber = bidNumber;
	}
	
	public MarketBasis getMarketBasis() {
		return marketBasis;
	}

	public int getBidNumber() {
		return bidNumber;
	}
	
	public abstract Bid aggregate(final Bid other);
	
	public abstract PriceUpdate calculateIntersection(double targetDemand);
	
	public abstract double getMaximumDemand();
	
	public abstract double getMinimumDemand();

	public abstract ArrayBid toArrayBid();
	
	public abstract PointBid toPointBid();

	public double getDemandAt(PriceStep priceStep) {
		return getDemandAt(priceStep.toPrice());
	}

	public double getDemandAt(Price price) {
		return getDemandAt(price.toPriceStep());
	}
}
