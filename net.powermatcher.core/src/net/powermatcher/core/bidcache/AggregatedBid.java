package net.powermatcher.core.bidcache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

/**
 * An {@link AggregatedBid} is the combination of several {@link Bid} of agents (as identified by their agentId's) that
 * are aggregated into a single bid (@see {@link #getAggregatedBid()}). This object is immutable and can only be created
 * using the {@link Builder}.
 */
public final class AggregatedBid {
    /**
     * A Builder that makes it easier to create an {@link AggregatedBid}. The idea is that you can call the
     * {@link #addAgentBid(String, Bid)} several times and the {@link #bidNumber(int)} once to define the
     * {@link AggregatedBid}. When calling the {@link #build()} method it returns the created aggregated bid and this
     * {@link Builder} should not be used after that.
     *
     * Example usage:
     *
     * <pre>
     * AggregateBid bid = new AggregatedBid.Builder(marketBasis).bidNumber(generator.next())
     *                                                          .addAgentBid(&quot;agent1&quot;, bid1)
     *                                                          .addAgentBid(&quot;agent2&quot;, bid2)
     *                                                          .build();
     * </pre>
     *
     * This class is not thread-safe.
     */
    public static final class Builder {
        private final MarketBasis marketBasis;
        private int bidNumber;
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
            bidNumber = 0;
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
         * @param bid
         *            The bid that has to be added to the aggregated bid.
         * @return This {@link Builder}
         */
        public Builder addAgentBid(String agentId, Bid bid) {
            if (!agentBidReferences.containsKey(agentId) && bid.getMarketBasis().equals(marketBasis)) {
                agentBidReferences.put(agentId, bid.getBidNumber());
                double[] demand = bid.toArrayBid().getDemand();
                for (int ix = 0; ix < marketBasis.getPriceSteps(); ix++) {
                    aggregatedBid[ix] += demand[ix];
                }
            }

            return this;
        }

        /**
         * Changes the bidnumber of the aggregated bid.
         *
         * @param bidNumber
         *            The new bidnumber
         * @return This {@link Builder}
         */
        public Builder bidNumber(int bidNumber) {
            this.bidNumber = bidNumber;
            return this;
        }

        /**
         * @return The new {@link AggregatedBid} object that contains all the added bids. After this method has been
         *         called, the {@link Builder} should not be used any further.
         */
        public AggregatedBid build() {
            Bid bid = new ArrayBid(marketBasis, bidNumber, aggregatedBid);
            return new AggregatedBid(agentBidReferences, bid);
        }
    }

    private final Map<String, Integer> agentBidReferences;

    private final Bid aggregatedBid;

    AggregatedBid(Map<String, Integer> agentBidReferences, Bid aggregatedBid) {
        this.agentBidReferences = Collections.unmodifiableMap(agentBidReferences);
        this.aggregatedBid = aggregatedBid;
    }

    /**
     * @return A {@link Map} of agentId to bid number reference.
     */
    public Map<String, Integer> getAgentBidReferences() {
        return agentBidReferences;
    }

    /**
     * @return The {@link Bid} that describes the sum of all the bids that have been stored in this
     *         {@link AggregatedBid}.
     * @see #getAgentBidReferences()
     */
    public Bid getAggregatedBid() {
        return aggregatedBid;
    }

    @Override
    public int hashCode() {
        return 31 * agentBidReferences.hashCode() + 63 * aggregatedBid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            AggregatedBid other = (AggregatedBid) obj;
            return other.agentBidReferences.equals(agentBidReferences) && other.aggregatedBid.equals(aggregatedBid);
        }
    }

    @Override
    public String toString() {
        return "AggregatedBid [agentBidReferences=" + agentBidReferences + ", aggregatedBid=" + aggregatedBid + "]";
    }
}
