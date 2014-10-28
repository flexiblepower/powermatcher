package net.powermatcher.core.agent.framework.log;


/**
 * 
 * The LogListenerService defines the interface of a log listener component.
 * <p>
 * The interface defines the log listener services available for handling
 * log messages for bid info and price info updates.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface LogListenerService {
	/**
	 * Handle bid info logging event.
	 * 
	 * @param bidLogInfo
	 *            Bid log info to handle.
	 */
	public void handleBidLogInfo(final BidLogInfo bidLogInfo);

	/**
	 * Handle price info logging event.
	 * 
	 * @param priceLogInfo
	 *            Price log info to handle.
	 */
	public void handlePriceLogInfo(final PriceLogInfo priceLogInfo);

}
