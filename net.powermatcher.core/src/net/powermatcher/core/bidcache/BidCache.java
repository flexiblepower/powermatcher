package net.powermatcher.core.bidcache;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.Agent;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BidCache} is an object that makes it easy to store bids received from agents and aggregate them into an
 * {@link AggregatedBid}. This also stores all the generated {@link AggregatedBid}s such that they can easily be
 * recalled later.
 *
 * This class is thread-safe.
 */
public class BidCache {
    private static final int MAX_BIDS = 900;

    private static final Logger LOGGER = LoggerFactory.getLogger(BidCache.class);

    private final MarketBasis marketBasis;

    private final AtomicInteger bidNumberGenerator;
    private final Map<String, Bid> agentBids;
    private volatile boolean bidChanged;

    private final Deque<AggregatedBid> generatedBids;

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
        bidNumberGenerator = new AtomicInteger(0);
        agentBids = new ConcurrentHashMap<String, Bid>();
        generatedBids = new LinkedList<AggregatedBid>();
        bidChanged = true;
    }

    /**
     * Updates the bid for a specific agent identifier. When a previous bid for the agent was available, the bid is
     * overridden.
     *
     * @param agentId
     *            The unique identifier of the agent. See {@link Agent#getAgentId()}.
     * @param bid
     *            The {@link Bid} that the agent has sent and has to be cache here. When the bid is <code>null</code>,
     *            the reference will be removed completely (effectively the same as calling
     *            {@link #removeBidOfAgent(String)}).
     * @throws IllegalArgumentException
     *             When the marketBasis of the bid does not match the marketBasis on which this {@link BidCache} is
     *             based.
     */
    public void updateAgentBid(String agentId, Bid bid) {
        if (bid == null) {
            agentBids.remove(agentId);
        } else if (!bid.getMarketBasis().equals(marketBasis)) {
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
        if (!bidChanged) {
            return generatedBids.peekLast();
        }

        AggregatedBid.Builder builder = new AggregatedBid.Builder(marketBasis);
        for (Entry<String, Bid> entry : agentBids.entrySet()) {
            builder.addAgentBid(entry.getKey(), entry.getValue());
        }
        builder.bidNumber(bidNumberGenerator.getAndIncrement());

        AggregatedBid aggregatedBid = builder.build();
        synchronized (generatedBids) {
            generatedBids.add(aggregatedBid);

            if (generatedBids.size() > MAX_BIDS) {
                LOGGER.warn("The number of generated bids is becoming very big, possible memory leak?");
                while (generatedBids.size() > MAX_BIDS) {
                    generatedBids.removeFirst();
                }
            }
        }
        bidChanged = false;

        return aggregatedBid;
    }

    /**
     * Retrieves a previously generated {@link AggregatedBid} for which the bidNumberReference matched the bidnumber of
     * the aggregatedBid. This will remove any bids that are generated before the wanted bid.
     *
     * @param bidNumberReference
     *            The reference that is used to find the {@link AggregatedBid}
     * @return The {@link AggregatedBid} for which holds that
     *         <code> bid.getAggregatedBid().getBidNumber() == bidNumberReference </code>
     * @throws IllegalArgumentException
     *             when there is no such {@link AggregatedBid} available.
     */
    public AggregatedBid retreiveAggregatedBid(int bidNumberReference) {
        synchronized (generatedBids) {
            boolean found = false;
            for (AggregatedBid bid : generatedBids) {
                if (bid.getAggregatedBid().getBidNumber() == bidNumberReference) {
                    found = true;
                }
            }

            if (!found) {
                throw new IllegalArgumentException("No bid with bidNumber " + bidNumberReference + " is available");
            }

            while (true) {
                AggregatedBid bid = generatedBids.peek();
                if (bid.getAggregatedBid().getBidNumber() == bidNumberReference) {
                    return bid;
                } else {
                    generatedBids.removeFirst();
                }
            }
        }
    }
}
