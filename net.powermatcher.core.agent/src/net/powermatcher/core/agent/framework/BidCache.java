package net.powermatcher.core.agent.framework;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.powermatcher.core.agent.framework.config.MatcherAgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.scheduler.service.TimeServicable;

/**
 * The bid cache maintains an aggregated bid, where bids can be added and
 * removed explicitly, or removed implicitly after a bid has expired. The bid
 * cache is fully thread-safe and the calculation of the aggregated bid is
 * optimized.
 * 
 * 
 * @see BidCacheElement
 * @see BidInfo
 * @see MarketBasis
 * 
 * @author IBM
 * @version 0.9.0
 */
public class BidCache {
	/**
	 * Define the default bid expiration time (long) field.
	 */
	public final static long DEFAULT_BID_EXPIRATION_TIME = 300;
	/**
	 * Define the time source (TimeService) that is used for obtaining real or
	 * simulated time.
	 */
	private TimeServicable timeSource;
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
	 * Define the aggregated bid (BidInfo) field.
	 */
	private BidInfo aggregatedBid;

	/**
	 * Default constructor
	 * 
	 * @see #BidCache(int)
	 */
	public BidCache() {
		this(MatcherAgentConfiguration.BID_EXPIRATION_TIME_DEFAULT);
	}

	/**
	 * Default constructor
	 * 
	 * @param expirationTime
	 *            The expiration time (<code>int</code>) parameter.
	 * @see #BidCache()
	 */
	public BidCache(final int expirationTime) {
		this.lastResetTime = 0;
		this.expirationTimeMillis = expirationTime * 1000l;
		this.bidCache = new HashMap<String, BidCacheElement>();
	}

	/**
	 * Bind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>) to bind.
	 * @see #unbind(TimeServicable)
	 */
	public void bind(final TimeServicable timeSource) {
		this.timeSource = timeSource;
	}

	/**
	 * Cleanup expired bids and arithmetic drift resulting from the
	 * optimization.
	 * 
	 * @return Returns the agent ids that have been removed from the cache.
	 */
	public synchronized Set<String> cleanup() {
		Set<String> removedAgents = new HashSet<String>();
		TimeServicable timeSource = this.timeSource;
		if (timeSource == null) {
			/*
			 * If a time source has not been set yet, always discard the
			 * aggregated bid.
			 */
			this.aggregatedBid = null;
		} else {
			/*
			 * Otherwise, clean up expired bids and discard the aggregated bid
			 * when it is too old.
			 */
			long currentTime = timeSource.currentTimeMillis();
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
	 * Return the aggregated bid for the non-expired bids that are currently in
	 * the cache. Returns a bid info that is a copy and therefore thread safe.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @return The aggregated bid for the bids in the cache.
	 */
	public synchronized BidInfo getAggregatedBid(final MarketBasis marketBasis) {
		if (marketBasis != null) {
			if (this.aggregatedBid == null || !this.aggregatedBid.getMarketBasis().equals(marketBasis)) {
				BidInfo aggregatedBid = new BidInfo(marketBasis);
				Set<String> idSet = this.bidCache.keySet();
				for (String agentId : idSet) {
					BidInfo bid = getLastBid(agentId);
					aggregatedBid = aggregatedBid.aggregate(bid);
				}
				this.aggregatedBid = aggregatedBid;
			}
			return this.aggregatedBid;
		}
		return null;
	}

	/**
	 * Get last bid with the specified agent ID parameter and return the BidInfo
	 * result.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @return Results of the get last bid (<code>BidInfo</code>) value.
	 */
	public synchronized BidInfo getLastBid(final String agentId) {
		BidCacheElement element = this.bidCache.get(agentId);
		BidInfo lastBid = null;
		if (element != null) {
			lastBid = element.getBidInfo();
		}
		return lastBid;
	}

	/**
	 * Remove agent with the specified agent ID parameter and return the BidInfo
	 * result.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @return Results of the remove agent (<code>BidInfo</code>) value.
	 */
	public synchronized BidInfo removeAgent(final String agentId) {
		BidCacheElement oldElement = this.bidCache.remove(agentId);
		BidInfo lastBid = null;
		if (this.aggregatedBid != null) {
			if (oldElement != null) {
				lastBid = oldElement.getBidInfo();
				this.aggregatedBid = this.aggregatedBid.subtract(lastBid);
			}
		}
		return lastBid;
	}

	/**
	 * Unbind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>) to unbind.
	 * 
	 * @see #bind(TimeServicable)
	 */
	public void unbind(final TimeServicable timeSource) {
		this.timeSource = null;
	}

	/**
	 * Update bid with the specified agent ID and new bid parameters and return
	 * the BidInfo result.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 * @return Returns the old bid (<code>BidInfo</code>), or null if the agent
	 *         is new.
	 * @see #getAggregatedBid(MarketBasis)
	 * @see #getLastBid(String)
	 */
	public synchronized BidInfo updateBid(final String agentId, final BidInfo newBidInfo) {
		assert newBidInfo != null;
		TimeServicable timeSource = this.timeSource;
		long currentTime = (timeSource == null) ? 0 : timeSource.currentTimeMillis();
		BidCacheElement element = new BidCacheElement(newBidInfo, currentTime);
		BidCacheElement oldElement = this.bidCache.put(agentId, element);
		BidInfo oldBid = null;
		if (this.aggregatedBid != null) {
			if (oldElement != null) {
				oldBid = oldElement.getBidInfo();
				this.aggregatedBid = this.aggregatedBid.subtract(oldBid);
			}
			this.aggregatedBid = this.aggregatedBid.aggregate(newBidInfo);
		}
		return oldBid;
	}

}
