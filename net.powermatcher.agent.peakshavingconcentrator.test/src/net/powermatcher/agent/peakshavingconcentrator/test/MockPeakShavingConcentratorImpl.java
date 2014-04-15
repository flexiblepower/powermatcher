package net.powermatcher.agent.peakshavingconcentrator.test;

import net.powermatcher.agent.peakshavingconcentrator.PeakShavingConcentrator;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;


public class MockPeakShavingConcentratorImpl extends PeakShavingConcentrator {
	private PriceInfo propagatedPrice;
	private BidInfo propagatedBid;

	@Override
	protected BidInfo publishBidUpdate(final BidInfo newBidInfo) {
		super.publishBidUpdate(newBidInfo);
		this.propagatedBid = newBidInfo;
		return newBidInfo;
	}

	@Override
	public synchronized void publishPriceInfo(final PriceInfo newPriceInfo) {
		super.publishPriceInfo(newPriceInfo);
		this.propagatedPrice = newPriceInfo;
	}

	public BidInfo getPropagatedBid() {
		return this.propagatedBid;
	}

	public PriceInfo getPropagatedPrice() {
		return this.propagatedPrice;
	}

}