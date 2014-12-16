package net.powermatcher.core;

import java.util.HashMap;
import java.util.Map;

public class BidCacheSnapshot {

    private Map<String, Integer> bidNumbers;


    public BidCacheSnapshot() {
        this.bidNumbers = new HashMap<>();
    }

    public Map<String, Integer> getBidNumbers() {
        return bidNumbers;
    }

    public void setBidNumbers(Map<String, Integer> bidNumbers) {
        this.bidNumbers = bidNumbers;
    }

}
