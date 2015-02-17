package net.powermatcher.core.concentrator;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.core.bidcache.AggregatedBid;

public class SentBidInformation {
    private final AggregatedBid originalBid;
    private final BidUpdate sentBidUpdate;

    public SentBidInformation(AggregatedBid originalBid, BidUpdate sentBidUpdate) {
        if (originalBid == null || sentBidUpdate == null) {
            throw new NullPointerException();
        }
        this.originalBid = originalBid;
        this.sentBidUpdate = sentBidUpdate;
    }

    public AggregatedBid getOriginalBid() {
        return originalBid;
    }

    public BidUpdate getSentBidUpdate() {
        return sentBidUpdate;
    }

    public Bid getSentBid() {
        return sentBidUpdate.getBid();
    }

    public int getBidNumber() {
        return sentBidUpdate.getBidNumber();
    }

    @Override
    public int hashCode() {
        return 31 * originalBid.hashCode() + 63 * sentBidUpdate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            SentBidInformation other = (SentBidInformation) obj;
            return originalBid.equals(other.originalBid) && sentBidUpdate.equals(other.sentBidUpdate);
        }
    }

    @Override
    public String toString() {
        return "SentBidInformation [originalBid=" + originalBid + ", sentBidUpdate=" + sentBidUpdate + "]";
    }
}
