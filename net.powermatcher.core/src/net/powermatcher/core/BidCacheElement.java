package net.powermatcher.core;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PricePoint;

/**
 * <p>
 * A {@link BidCacheElement} instance consists of a {@link Bid} object associated with a time stamp to record the age of
 * the {@link Bid} object.
 * </p>
 * 
 * @author FAN
 * @version 1.0
 */
public class BidCacheElement {
    /**
     * A {@link Bid} curve in either {@link PricePoint} or by an demand array representation.
     */
    ArrayBid arrayBid;

    /**
     * Time stamp for age of {@link Bid}.
     */
    long timestamp;

    /**
     * Constructs an instance of this class from the specified {@link Bid} and time stamp parameters.
     * 
     * @param arrayBid
     *            The bid (<code>Bid</code>) parameter.
     * @param timestamp
     *            The time stamp (<code>long</code>) parameter.
     */
    public BidCacheElement(final Bid bid, final long timestamp) {
        
        if(bid instanceof PointBid){
            this.arrayBid = bid.toArrayBid();
        }else{
            this.arrayBid = (ArrayBid)bid;
        }
        
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the bid value.
     * 
     * @return The bid (<code>Bid</code>) value.
     * @see #setBid(Bid)
     */
    public ArrayBid getBid() {
        return this.arrayBid;
    }

    /**
     * Gets the time stamp value.
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
        
        if(bid instanceof PointBid){
            this.arrayBid = bid.toArrayBid();
        }else{
            this.arrayBid = (ArrayBid)bid;
        }
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
