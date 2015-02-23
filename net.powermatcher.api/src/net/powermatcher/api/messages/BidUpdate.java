package net.powermatcher.api.messages;

import net.powermatcher.api.data.Bid;

/**
 *
 * This immutable data object is a message that sends a {@link Bid} with a corresponding bidNumber.
 *
 * @author FAN
 * @version 2.0
 */
public class BidUpdate {
    private final Bid bid;
    private final int bidNumber;

    /**
     * A constructor used to create a new {@link BidUpdate} instance.
     *
     * @param bid
     *            the {@link Bid} of this BidUpdate
     * @param bidNumber
     *            the bidNumber
     */
    public BidUpdate(Bid bid, int bidNumber) {
        if (bid == null) {
            throw new NullPointerException("bid");
        }
        this.bid = bid;
        this.bidNumber = bidNumber;
    }

    /**
     * @return The {@link Bid} that is sent
     */
    public Bid getBid() {
        return bid;
    }

    /**
     * @return the current value of bidNumber.
     */
    public int getBidNumber() {
        return bidNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        } else {
            BidUpdate other = (BidUpdate) obj;
            return bidNumber == other.bidNumber && bid.equals(other.bid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * bid.hashCode() + bidNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "BidUpdate [" + bid + ", bidNr=" + bidNumber + "]";
    }
}
