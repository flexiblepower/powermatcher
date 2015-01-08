package net.powermatcher.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

/**
 * The {@link BidCache} maintains an aggregated bid, where bids can be added and
 * removed explicitly, or removed implicitly after a {@link Bid} has expired.
 * The {@link Bid} cache is fully thread-safe and the calculation of the
 * aggregated bid is optimized.
 * 
 * @see BidCacheElement
 * @see Bid
 * @see MarketBasis
 * 
 * @author FAN
 * @version 2.0
 */
public class BidCache {
	public static final long DEFAULT_BID_EXPIRATION_TIME = 300;

	/**
	 * TimeService that is used for obtaining real or simulated time.
	 */
	private TimeService timeService;
	/**
	 * A Map containing the AgentId the most recent {@link BidCacheElement}
	 */
	private Map<String, BidCacheElement> bidCache;

	/**
	 * Time, in milliseconds, that is used to determin if a
	 * {@link BidCacheElement} is still valid.
	 */
	private long expirationTimeMillis;

	/**
	 * Used to keep track of when the {@link BidCache} was last resetted.
	 */
	private long lastResetTime;

	/**
	 * The aggregated bid of all Bids in all {@link BidCacheElement}s, contained
	 * in this BidCache.
	 */
	private ArrayBid aggregatedBid;

	/**
	 * The threadSafe counter to generate new id's for {@link ArrayBid}s
	 */
	private final AtomicInteger snapshotGenerator;

	/**
	 * A map containing all snapshot id's and their corresponding
	 * {@link BidCacheSnapshot}s
	 */
	private Map<Integer, BidCacheSnapshot> bidCacheHistory = new HashMap<Integer, BidCacheSnapshot>();

	/**
	 * @param timeService
	 *            The timeservice used to obtain the current time. For
	 *            comparison with the expirationTime.
	 * @param expirationTime
	 *            The expiration time (<code>int</code>) parameter.
	 */
	public BidCache(final TimeService timeService, final int expirationTime) {
		this.lastResetTime = 0;
		this.expirationTimeMillis = expirationTime * 1000L;
		this.bidCache = new HashMap<String, BidCacheElement>();
		this.timeService = timeService;
		this.snapshotGenerator = new AtomicInteger();
	}

	/**
	 * Update bid with the specified agent ID and new bid parameters and return
	 * any old Bid as an ArrayBid.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param newBid
	 *            The new bid (<code>Bid</code>) parameter.
	 * @return Returns the old bid (<code>ArrayBid</code>), or null if the agent
	 *         is new.
	 * @see #getAggregatedBid(MarketBasis)
	 * @see #getLastBid(String)
	 */
	public synchronized ArrayBid updateBid(final String agentId,
			final Bid newBid) {
		if (newBid == null) {
			throw new IllegalArgumentException();
		}
		TimeService timeSource = this.timeService;
		long currentTime = (timeSource == null) ? 0 : timeSource
				.currentTimeMillis();
		BidCacheElement element = new BidCacheElement(newBid, currentTime);
		BidCacheElement oldElement = this.bidCache.put(agentId, element);
		ArrayBid oldBid = null;
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
	 * Remove agent with the specified agent ID parameter and return the Bid
	 * result.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @return Results of the remove agent (<code>Bid</code>) value.
	 */
	public synchronized ArrayBid removeAgent(final String agentId) {
		BidCacheElement oldElement = this.bidCache.remove(agentId);
		ArrayBid lastBid = null;
		if (this.aggregatedBid != null && oldElement != null) {
			lastBid = oldElement.getBid();
			this.aggregatedBid = this.aggregatedBid.subtract(lastBid);
		}
		return lastBid;
	}

	/**
	 * Cleanup expired bids and arithmetic drift resulting from the
	 * optimization.
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
			/* Only remove bids if the age is known */
			if (timeStamp != 0
					&& currentTime - timeStamp >= this.expirationTimeMillis) {
				removedAgents.add(agentId);
				iterator.remove();
				agentsRemoved = true;
			}
		}
		if (agentsRemoved
				|| currentTime - this.lastResetTime >= this.expirationTimeMillis) {
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
	 * Return the aggregated bid for the non-expired bids that are currently in
	 * the cache. Returns a bid that is a copy and therefore thread safe.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @return The aggregated bid for the bids in the cache.
	 */
	public synchronized ArrayBid getAggregatedBid(
			final MarketBasis marketBasis, boolean isConcentrator) {
		if (marketBasis != null) {
			if (this.aggregatedBid == null
					|| !this.aggregatedBid.getMarketBasis().equals(marketBasis)) {

				ArrayBid newAggregatedBid = new ArrayBid.Builder(marketBasis)
						.setDemand(0).build();
				Set<String> idSet = this.bidCache.keySet();
				for (String agentId : idSet) {
					ArrayBid bid = getLastBid(agentId);
					newAggregatedBid = newAggregatedBid.aggregate(bid);
				}
				this.aggregatedBid = newAggregatedBid;
			}

			if (isConcentrator == true) {

				BidCacheSnapshot bidCacheSnapshot = new BidCacheSnapshot();

				// Make a blueprint of the bidCache storing agentID - bidNumber
				// pairs
				Set<String> idSet = this.bidCache.keySet();
				for (String agentId : idSet) {
					ArrayBid bid = getLastBid(agentId);
					bidCacheSnapshot.getBidNumbers().put(agentId,
							bid.getBidNumber());
				}

				// Update the aggregatedBid with the new bidNumber.
				int snapshotId = snapshotGenerator.incrementAndGet();
				ArrayBid newBidNr = new ArrayBid(this.aggregatedBid, snapshotId);
				this.aggregatedBid = newBidNr;

				bidCacheHistory.put(snapshotId, bidCacheSnapshot);
			}

			return this.aggregatedBid;
		}
		return null;
	}

	/**
	 * Returns the {@link BidCacheSnapshot} corresponding to the given bid
	 * number.
	 * 
	 * @param bidNumber
	 *            the bidnumber you want to get the BidCacheSnapshot of.
	 * @return the {@link BidCacheSnapshot} instance
	 */
	public synchronized BidCacheSnapshot getMatchingSnapshot(int bidNumber) {
		BidCacheSnapshot matchedSnapshot = this.bidCacheHistory
				.remove(bidNumber);
		if (bidNumber > 1 && matchedSnapshot != null
				&& this.bidCacheHistory.get(bidNumber - 1) != null) {
			this.bidCacheHistory.remove(bidNumber - 1);
		}
		return matchedSnapshot;
	}

	/**
	 * Get last bid with the specified agent ID parameter and return the Bid
	 * result.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @return Results of the get last bid (<code>Bid</code>) value.
	 */
	public synchronized ArrayBid getLastBid(final String agentId) {
		BidCacheElement element = this.bidCache.get(agentId);
		ArrayBid lastBid = null;
		if (element != null) {
			lastBid = element.getBid();
		}
		return lastBid;
	}
}
