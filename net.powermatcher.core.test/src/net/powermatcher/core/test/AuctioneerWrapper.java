package net.powermatcher.core.test;

import net.powermatcher.core.agent.auctioneer.Auctioneer;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;

public class AuctioneerWrapper extends Auctioneer {

	@Override
	public PriceInfo getLastPublishedPriceInfo() {
		return super.getLastPublishedPriceInfo();
	}
	
	@Override
	public BidInfo getAggregatedBid() {
		return super.getAggregatedBid();
	}
	
	@Override
	public void publishPriceInfo(PriceInfo newPriceInfo) {
		super.publishPriceInfo(newPriceInfo);
	}
}