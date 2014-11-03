package net.powermatcher.core;

import net.powermatcher.api.data.Bid;

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
	 * Define the bid info (Bid) field.
	 */
	Bid bid;
	/**
	 * Define the time stamp (long) field.
	 */
	long timestamp;

	/**
	 * Constructs an instance of this class from the specified bid info and time
	 * stamp parameters.
	 * 
	 * @param bid
	 *            The bid info (<code>Bid</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>long</code>) parameter.
	 */
	public BidCacheElement(final Bid bid, final long timestamp) {
		super();
		this.bid = bid;
		this.timestamp = timestamp;
	}

	/**
	 * Gets the bid info value.
	 * 
	 * @return The bid info (<code>Bid</code>) value.
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
	 * Sets the bid info value.
	 * 
	 * @param bid
	 *            The bid info (<code>Bid</code>) parameter.
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
