package net.powermatcher.core.agent.auctioneer.service;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface PricingControlService {

	/**
	 * Request the auctioneer to update its price. If a PricingService is
	 * available, the auctioneer will use it to calculate the new price. If no
	 * PricingService is available, the auctioneer will use its default
	 * algorithm.
	 * 
	 * @see PricingService#determinePrice(net.powermatcher.core.agent.framework.data.BidInfo)
	 */
	public void updatePrice();

}
