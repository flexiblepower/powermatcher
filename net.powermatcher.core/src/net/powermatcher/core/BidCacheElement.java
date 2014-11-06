package net.powermatcher.core;

import net.powermatcher.api.data.Bid;

/**
 * <p>
 * A BidCacheElement instance consists of a bid object associated with a time
 * stamp to record the age of the bid object.
 * </p>
 * 
 * @author FAN
 * @version 1.0
 */
public class BidCacheElement {
	/**
	 * Define the bid (Bid) field.
	 */
	Bid bid;
	/**
	 * Define the time stamp (long) field.
	 */
	long timestamp;

	/**
	 * Constructs an instance of this class from the specified bid and time
	 * stamp parameters.
	 * 
	 * @param bid
	 *            The bid (<code>Bid</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>long</code>) parameter.
	 */
	public BidCacheElement(final Bid bid, final long timestamp) {
		this.bid = bid;
		this.timestamp = timestamp;
	}

	/**
	 * Gets the bid value.
	 * 
	 * @return The bid (<code>Bid</code>) value.
	 * @see #setBid(Bid)
	 */
	public Bid getBid() {
		return this.bid;
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
	 * Sets the bid value.
	 * 
	 * @param bid
	 *            The bid (<code>Bid</code>) parameter.
	 * @see #getBid()
	 */
	public void setBid(final Bid bid) {
		this.bid = bid;
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
