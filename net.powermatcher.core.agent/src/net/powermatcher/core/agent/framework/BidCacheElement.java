package net.powermatcher.core.agent.framework;


import net.powermatcher.core.agent.framework.data.BidInfo;

/**
 * A BidCacheElement instance consists of a bid info object
 * associated with a time stamp to record the age of the bid info
 * object.
 * 
 * @author IBM
 * @version 0.9.0
 */
public class BidCacheElement {
	/**
	 * Define the bid info (BidInfo) field.
	 */
	BidInfo bidInfo;
	/**
	 * Define the time stamp (long) field.
	 */
	long timestamp;

	/**
	 * Constructs an instance of this class from the specified bid info and time
	 * stamp parameters.
	 * 
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>long</code>) parameter.
	 */
	public BidCacheElement(final BidInfo bidInfo, final long timestamp) {
		super();
		this.bidInfo = bidInfo;
		this.timestamp = timestamp;
	}

	/**
	 * Gets the bid info value.
	 * 
	 * @return The bid info (<code>BidInfo</code>) value.
	 * @see #setBidInfo(BidInfo)
	 */
	public BidInfo getBidInfo() {
		return this.bidInfo;
	}

	/**
	 * Gets the time stamp (long) value.
	 * 
	 * @return The time stamp (<code>long</code>) value.
	 * @see #setTimestamp(long)
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * Sets the bid info value.
	 * 
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @see #getBidInfo()
	 */
	public void setBidInfo(final BidInfo bidInfo) {
		this.bidInfo = bidInfo;
	}

	/**
	 * Sets the time stamp value.
	 * 
	 * @param timestamp
	 *            The time stamp (<code>long</code>) parameter.
	 * @see #getTimestamp()
	 */
	public void setTimestamp(final long timestamp) {
		this.timestamp = timestamp;
	}

}
