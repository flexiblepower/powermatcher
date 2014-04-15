package net.powermatcher.core.agent.objective.service;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface ObjectiveNotificationService {

	/**
	 * The auctioneer's aggregated bid, as seen by the objective agent, has been updated.
	 * This includes the bid that is injected into the cluster by the objective agent.
	 * 
	 * @param newBidInfo
	 *            The auctioneer's aggregated bid.
	 */
	public void handleAggregatedBidUpdate(BidInfo newBidInfo);

	/**
	 * Update price info with the specified new price info parameter.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 */
	public void handlePriceUpdate(final PriceInfo newPriceInfo);

}
