package net.powermatcher.core.test;

import net.powermatcher.core.agent.concentrator.Concentrator;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.ConfigurationService;

public class ConcentratorWrapper extends Concentrator {

	public PriceInfo lastReceivedPriceInfo;
	public BidInfo lastPublishedBidUpdate;
	public BidInfo lastReceivedBidUpdate;
	
	public ConcentratorWrapper(final ConfigurationService configuration) {
		super(configuration);
	}

	@Override
	public PriceInfo getLastPublishedPriceInfo() {
		return super.getLastPublishedPriceInfo();
	}
	
	@Override
	public PriceInfo getLastPriceInfo() {
		return super.getLastPriceInfo();
	}
	
	@Override
	public BidInfo getAggregatedBid() {
		return super.getAggregatedBid();
	}
	
	@Override
	public void updatePriceInfo(PriceInfo newPriceInfo) {
		this.lastReceivedPriceInfo = newPriceInfo;
		super.updatePriceInfo(newPriceInfo);
	}
	
	@Override
	public void updateBidInfo(String agentId, BidInfo newBidInfo) {
		this.lastReceivedBidUpdate = newBidInfo;
		super.updateBidInfo(agentId, newBidInfo);
	}
	
	@Override
	protected BidInfo publishBidUpdate(BidInfo newBidInfo) {
		this.lastPublishedBidUpdate = newBidInfo;
		return super.publishBidUpdate(newBidInfo);
	}
	
	
	
}
