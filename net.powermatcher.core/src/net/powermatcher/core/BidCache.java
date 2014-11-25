package net.powermatcher.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

/**
 * The {@link BidCache} maintains an aggregated bid, where bids can be added and removed explicitly, or removed
 * implicitly after a {@link Bid} has expired. The {@link Bid} cache is fully thread-safe and the calculation of the
 * aggregated bid is optimized.
 * 
 * @see BidCacheElement
 * @see Bid
 * @see MarketBasis
 * 
 * @author FAN
 * @version 1.0
 */
public class BidCache {
    /**
     * Define the default bid expiration time (long) field.
     */
    public static final long DEFAULT_BID_EXPIRATION_TIME = 300;
    /**
     * Define the time source (TimeService) that is used for obtaining real or simulated time.
     */
    private TimeService timeService;
    /**
     * Define the bid cache (Map<String,HanBidCacheElement>) field.
     */
    private Map<String, BidCacheElement> bidCache;
    /**
     * Define the expiration time (long) field.
     */
    private long expirationTimeMillis;
    /**
     * Define the last reset time (long) field.
     */
    private long lastResetTime;
    /**
     * Define the aggregated bid (Bid) field.
     */
    private Bid aggregatedBid;
    
    private int snapshotCounter;
    
    private Map<Integer, BidCacheSnapshot> bidCacheHistory = new HashMap<Integer, BidCacheSnapshot>();
    
    /**
     * Default constructor
     * 
     * @param expirationTime
     *            The expiration time (<code>int</code>) parameter.
     * @see #BidCache()
     */
    public BidCache(final TimeService timeService, final int expirationTime) {
        this.lastResetTime = 0;
        this.expirationTimeMillis = expirationTime * 1000L;
        this.bidCache = new HashMap<String, BidCacheElement>();
        this.timeService = timeService;
    }
    
    public int getSnapshotCounter() {
		return this.snapshotCounter;
	}
    
    private void incrSnapshotCounter() {
    	this.snapshotCounter += 1;
	}

	public void setSnapshotCounter(int snapshotCounter) {
		this.snapshotCounter = snapshotCounter;
	}
	
	/**
     * Update bid with the specified agent ID and new bid parameters and return the Bid result.
     * 
     * @param agentId
     *            The agent ID (<code>String</code>) parameter.
     * @param newBid
     *            The new bid (<code>Bid</code>) parameter.
     * @return Returns the old bid (<code>Bid</code>), or null if the agent is new.
     * @see #getAggregatedBid(MarketBasis)
     * @see #getLastBid(String)
     */
    public synchronized Bid updateBid(final String agentId, final Bid newBid) {
        assert newBid != null;
        TimeService timeSource = this.timeService;
        long currentTime = (timeSource == null) ? 0 : timeSource.currentTimeMillis();
        BidCacheElement element = new BidCacheElement(newBid, currentTime);
        BidCacheElement oldElement = this.bidCache.put(agentId, element);
        Bid oldBid = null;
        if (this.aggregatedBid != null) {
            if (oldElement != null) {
                oldBid = oldElement.getBid();
                this.aggregatedBid = this.aggregatedBid.subtract(oldBid);
            }
            this.aggregatedBid = this.aggregatedBid.aggregate(newBid);
        }
        return oldBid;
    }

    /**
     * Remove agent with the specified agent ID parameter and return the Bid result.
     * 
     * @param agentId
     *            The agent ID (<code>String</code>) parameter.
     * @return Results of the remove agent (<code>Bid</code>) value.
     */
    public synchronized Bid removeAgent(final String agentId) {
        BidCacheElement oldElement = this.bidCache.remove(agentId);
        Bid lastBid = null;
        if (this.aggregatedBid != null && oldElement != null) {
            lastBid = oldElement.getBid();
            this.aggregatedBid = this.aggregatedBid.subtract(lastBid);
        }
        return lastBid;
    }

    /**
     * Cleanup expired bids and arithmetic drift resulting from the optimization.
     * 
     * @return Returns the agent ids that have been removed from the cache.
     */
    public synchronized Set<String> cleanup() {
        Set<String> removedAgents = new HashSet<String>();

        long currentTime = timeService.currentTimeMillis();
        boolean agentsRemoved = false;
        Set<String> keys = this.bidCache.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String agentId = iterator.next();
            BidCacheElement element = this.bidCache.get(agentId);
            long timeStamp = element.getTimestamp();
            /* Only remove bids if the age is know */
            if (timeStamp != 0 && currentTime - timeStamp >= this.expirationTimeMillis) {
                removedAgents.add(agentId);
                iterator.remove();
                agentsRemoved = true;
            }
        }
        if (agentsRemoved || currentTime - this.lastResetTime >= this.expirationTimeMillis) {
            this.aggregatedBid = null;
            this.lastResetTime = currentTime;
        }

        return removedAgents;
    }

    /**
     * Gets the agent ID set (Set<String>) value.
     * 
     * @return The agent ID set (<code>Set<String></code>) value.
     */
    public synchronized Set<String> getAgentIdSet() {
        return new HashSet<String>(this.bidCache.keySet());
    }

    /**
     * Return the aggregated bid for the non-expired bids that are currently in the cache. Returns a bid that is a copy
     * and therefore thread safe.
     * 
     * @param marketBasis
     *            The market basis (<code>MarketBasis</code>) parameter.
     * @return The aggregated bid for the bids in the cache.
     */
    public synchronized Bid getAggregatedBid(final MarketBasis marketBasis) {
        if (marketBasis != null) {
        	
        	BidCacheSnapshot bidCacheSnapshot = new BidCacheSnapshot();
        	
            if (this.aggregatedBid == null || !this.aggregatedBid.getMarketBasis().equals(marketBasis)) {
            	 	
                Bid newAggregatedBid = new Bid(marketBasis);
                Set<String> idSet = this.bidCache.keySet();
                for (String agentId : idSet) {
                    Bid bid = getLastBid(agentId);                   
                    newAggregatedBid = newAggregatedBid.aggregate(bid);
                }
                this.aggregatedBid = newAggregatedBid;
            }
            
            //Make a blueprint of the bidCache storing agentID - bidNumber pairs
            Set<String> idSet = this.bidCache.keySet();
            for (String agentId : idSet) {
                Bid bid = getLastBid(agentId);                   
                bidCacheSnapshot.getBidNumbers().put(agentId, bid.getBidNumber());  
            }

            //Increment the counter to create a unique bidNumber for the aggregatedBid. 
            //Save it with the BidCacheSnapshot (not used).
            //Update the aggregatedBid with the new Bidnumber.  
            incrSnapshotCounter();
            Bid newBidNr = new Bid(this.aggregatedBid, getSnapshotCounter());
            this.aggregatedBid = newBidNr;            
           
            bidCacheSnapshot.setCount(getSnapshotCounter());
            bidCacheSnapshot.setAggregatedBid(newBidNr);
            bidCacheHistory.put(getSnapshotCounter(), bidCacheSnapshot);
                        
            return this.aggregatedBid;
        }
        return null;
    }
    
    public synchronized BidCacheSnapshot getMatchingSnapshot(int bidNumber) {
    	return this.bidCacheHistory.remove(bidNumber);
    }

    /**
     * Get last bid with the specified agent ID parameter and return the Bid result.
     * 
     * @param agentId
     *            The agent ID (<code>String</code>) parameter.
     * @return Results of the get last bid (<code>Bid</code>) value.
     */
    public synchronized Bid getLastBid(final String agentId) {
        BidCacheElement element = this.bidCache.get(agentId);
        Bid lastBid = null;
        if (element != null) {
            lastBid = element.getBid();
        }
        return lastBid;
    }
}
