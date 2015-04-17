package net.powermatcher.core.bidcache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.powermatcher.api.Agent;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;

/**
 * The {@link BidCache} is an object that makes it easy to store bids received from agents and aggregate them into an
 * {@link AggregatedBid}. This also stores all the generated {@link AggregatedBid}s such that they can easily be
 * recalled later.
 *
 * This class is thread-safe.
 */
public class BidCache {
    private final MarketBasis marketBasis;

    private final Map<String, BidUpdate> agentBids;

    private volatile boolean bidChanged;
    private transient AggregatedBid lastBid;

    /**
     * Creates a new {@link BidCache} based on the {@link MarketBasis}.
     *
     * @param marketBasis
     *            The {@link MarketBasis} that is used to match the bids.
     */
    public BidCache(MarketBasis marketBasis) {
        if (marketBasis == null) {
            throw new NullPointerException("marketBasis");
        }
        this.marketBasis = marketBasis;
        agentBids = new ConcurrentHashMap<String, BidUpdate>();
        bidChanged = true;
    }

    /**
     * Updates the bid for a specific agent identifier. When a previous bid for the agent was available, the bid is
     * overridden.
     *
     * @param agentId
     *            The unique identifier of the agent. See {@link Agent#getAgentId()}.
     * @param bid
     *            The {@link BidUpdate} that the agent has sent and has to be cache here. When the bid is
     *            <code>null</code>, the reference will be removed completely (effectively the same as calling
     *            {@link #removeBidOfAgent(String)}).
     * @throws IllegalArgumentException
     *             When the marketBasis of the bid does not match the marketBasis on which this {@link BidCache} is
     *             based.
     */
    public void updateAgentBid(String agentId, BidUpdate bid) {
        if (bid == null) {
            agentBids.remove(agentId);
        } else if (!bid.getBid().getMarketBasis().equals(marketBasis)) {
            throw new IllegalArgumentException("The marketBasis of the bid does not match the marketBasis of this BidCache");
        } else {
            bidChanged = true;
            agentBids.put(agentId, bid);
        }
    }

    /**
     * Removed the bid of the agent from this cache. When it was already not available, this method does nothing.
     *
     * @param agentId
     *            The unique identifier of the agent. See {@link Agent#getAgentId()}.
     */
    public void removeBidOfAgent(String agentId) {
        bidChanged = true;
        agentBids.remove(agentId);
    }

    /**
     * Creates a new {@link AggregatedBid} based on the current cache bids. This adds up all the bids and saves the
     * references to the agent bids.
     *
     * @return The new {@link AggregatedBid}.
     */
    public AggregatedBid aggregate() {
        if (!bidChanged && lastBid != null) {
            return lastBid;
        }

        AggregatedBid.Builder builder = new AggregatedBid.Builder(marketBasis);
        for (Entry<String, BidUpdate> entry : agentBids.entrySet()) {
            builder.addAgentBid(entry.getKey(), entry.getValue());
        }

        synchronized (this) {
            lastBid = builder.build();
            bidChanged = false;

            return lastBid;
        }
    }
}
