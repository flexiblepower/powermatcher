package net.powermatcher.core;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.Bid;

public class BidCacheSnapshot {

    private Bid aggregatedBid;

    private Map<String, Integer> bidNumbers;

    private int count;

    public BidCacheSnapshot() {
        this.bidNumbers = new HashMap<>();
    }

    public Bid getAggregatedBid() {
        return aggregatedBid;
    }

    public void setAggregatedBid(Bid aggregatedBid) {
        this.aggregatedBid = aggregatedBid;
    }

    public Map<String, Integer> getBidNumbers() {
        return bidNumbers;
    }

    public void setBidNumbers(Map<String, Integer> bidNumbers) {
        this.bidNumbers = bidNumbers;
    }

    public void setCount(int snapshotCounter) {
        this.count = snapshotCounter;
    }

    public int getCount() {
        return this.count;
    }
}
