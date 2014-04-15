package net.powermatcher.core.agent.concentrator.service;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface PeakShavingNotificationService {
	/**
	 * The peak shaving allocation has been updated, as a result of updated
	 * bids and/or a price update.
	 */
	public void updatedAllocation();

}
