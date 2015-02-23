package net.powermatcher.core.bidcache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;

/**
 * An {@link AggregatedBid} is the combination of several {@link Bid} of agents (as identified by their agentId's) that
 * are aggregated into a single bid. This object is immutable and can only be created using the {@link Builder}.
 */
public final class AggregatedBid
    extends ArrayBid {
    /**
     * A Builder that makes it easier to create an {@link AggregatedBid}. The idea is that you can call the
     * {@link #addAgentBid(String, BidUpdate)} or {@link #addBid(Bid)} several times to define the {@link AggregatedBid}
     * . When calling the {@link #build()} method it returns the created aggregated bid and this {@link Builder} should
     * not be used after that.
     *
     * Example usage:
     *
     * <pre>
     * AggregateBid bid = new AggregatedBid.Builder(marketBasis).addAgentBid(&quot;agent1&quot;, bid1)
     *                                                          .addAgentBid(&quot;agent2&quot;, bid2)
     *                                                          .build();
     * </pre>
     *
     * This class is not thread-safe.
     */
    public static final class Builder {
        private final MarketBasis marketBasis;
        private final Map<String, Integer> agentBidReferences;
        private final double[] aggregatedBid;

        /**
         * Creates a new {@link Builder} that should be used to generate a new {@link AggregatedBid}.
         *
         * @param marketBasis
         *            The {@link MarketBasis} that is used to base the new {@link AggregatedBid} on.
         */
        public Builder(MarketBasis marketBasis) {
            this.marketBasis = marketBasis;
            agentBidReferences = new HashMap<String, Integer>();
            aggregatedBid = new double[marketBasis.getPriceSteps()];
        }

        /**
         * Adds the bid of an agent. This saves the bid internally for later use and adds the bid on the sum of all the
         * other bids.
         *
         * Warning: when this method is called multiple times with the same agentId or the marketBasis does not match
         * the marketBasis of the bid, the call is ignored.
         *
         * @param agentId
         *            The unqiue identifier of the agent that has sent the bid.
         * @param bidUpdate
         *            The bidUpdate that has to be added to the aggregated bid.
         * @return This {@link Builder}
         */
        public Builder addAgentBid(String agentId, BidUpdate bidUpdate) {
            if (!agentBidReferences.containsKey(agentId) && bidUpdate.getBid().getMarketBasis().equals(marketBasis)) {
                agentBidReferences.put(agentId, bidUpdate.getBidNumber());
                addBid(bidUpdate.getBid());
            }

            return this;
        }

        public Builder addBid(Bid bid) {
            if (bid.getMarketBasis().equals(marketBasis)) {
                double[] demand = bid.toArrayBid().getDemand();
                for (int ix = 0; ix < marketBasis.getPriceSteps(); ix++) {
                    aggregatedBid[ix] += demand[ix];
                }
            }
            return this;
        }

        /**
         * @return The new {@link AggregatedBid} object that contains all the added bids. After this method has been
         *         called, the {@link Builder} should not be used any further.
         */
        public AggregatedBid build() {
            return new AggregatedBid(marketBasis, aggregatedBid, agentBidReferences);
        }
    }

    private final Map<String, Integer> agentBidReferences;

    AggregatedBid(MarketBasis marketBasis, double[] demand, Map<String, Integer> agentBidReferences) {
        super(marketBasis, demand);
        this.agentBidReferences = Collections.unmodifiableMap(agentBidReferences);
    }

    public AggregatedBid(ArrayBid bid, Map<String, Integer> agentBidReferences) {
        super(bid);
        this.agentBidReferences = Collections.unmodifiableMap(new HashMap<String, Integer>(agentBidReferences));
    }

    /**
     * @return A {@link Map} of agentId to bid number reference.
     */
    public Map<String, Integer> getAgentBidReferences() {
        return agentBidReferences;
    }

    @Override
    public int hashCode() {
        return 31 * agentBidReferences.hashCode() + 63 * super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        } else {
            AggregatedBid other = (AggregatedBid) obj;
            return other.agentBidReferences.equals(agentBidReferences);
        }
    }

    @Override
    public String toString() {
        return "AggregatedBid [agentBidReferences=" + agentBidReferences + ", aggregatedBid=" + super.toString() + "]";
    }
}
