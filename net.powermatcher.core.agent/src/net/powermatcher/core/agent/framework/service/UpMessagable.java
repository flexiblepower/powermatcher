package net.powermatcher.core.agent.framework.service;


import net.powermatcher.core.agent.framework.data.BidInfo;

/**
 * @author IBM
 * @version 0.9.0
 * 
 * <p>
 * The MatcherService interface defines the input services provided by a matcher.
 * </p>
 * <p>
 * It provides only one service. Clients can send a bid info update that
 * will be processed by the matcher.
 * </p>
 */
public interface UpMessagable {
	/**
	 * Update bid info with the specified agent ID and new bid parameters.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	public void updateBidInfo(final String agentId, final BidInfo newBidInfo);

}
