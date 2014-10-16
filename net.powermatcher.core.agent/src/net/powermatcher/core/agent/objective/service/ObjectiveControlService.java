package net.powermatcher.core.agent.objective.service;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface ObjectiveControlService {

	/**
	 * Gets the aggregated bid (BidInfo) value.
	 * 
	 * @return The aggregated bid. Null when no aggregated bid is available
	 *         (yet).
	 */
	public BidInfo getAggregatedBid();

	/**
	 * Gets the last price info (PriceInfo) value.
	 * 
	 * @return The last price info (<code>PriceInfo</code>) value.
	 */
	public PriceInfo getLastPriceInfo();

	/**
	 * Set the bid that the objective agent will submit to the cluster.
	 * As long as no objective bid is set, the objective agent will
	 * send out the configured default bid.
	 *  
	 * @param objectiveBid The objective bid to submit to the cluster, or null to submit
	 * the configured default bid.
	 */
	public void setObjectiveBid(BidInfo objectiveBid);

	/**
	 * Get the bid that the objective agent is currently submitting to the cluster.
	 * @return The objective bid to submit to the cluster.
	 */
	public BidInfo getObjectiveBid();

	
}
