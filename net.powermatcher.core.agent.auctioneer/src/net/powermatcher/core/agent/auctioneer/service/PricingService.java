package net.powermatcher.core.agent.auctioneer.service;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface PricingService {

	/**
	 * The aggregated bid from the auctioneer's children has been updated and a
	 * new price should be calculated calculated.
	 * 
	 * @param newBidInfo
	 *            The aggregated bid from the auctioneer's children.
	 * @return The new price that the auctioneer will publish.
	 */
	public PriceInfo determinePrice(BidInfo newBidInfo);

}
