package net.powermatcher.core;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.Agent;
import net.powermatcher.api.data.Bid;

/**
 * A {@link BidCacheSnapshot} contains a map of id's of {@link Bid}s and the id of the {@link Agent} it belongs to.
 * 
 * @author FAN
 * @version 2.0
 */
public class BidCacheSnapshot {

    /**
     * A map of Agentid and the Id's of the {@link Bid} that they sent.
     */
    private Map<String, Integer> bidNumbers;

    /**
     * A constructor to create a BidCacheSnapshot.
     */
    public BidCacheSnapshot() {
        this.bidNumbers = new HashMap<>();
    }

    /**
     * @return the current value of bidNumbers.
     */
    public Map<String, Integer> getBidNumbers() {
        return bidNumbers;
    }

    /**
     * @param the
     *            new map to replace bidNumbers.
     */
    public void setBidNumbers(Map<String, Integer> bidNumbers) {
        this.bidNumbers = bidNumbers;
    }
}
