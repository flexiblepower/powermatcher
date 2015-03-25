package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.Bid;

/**
 * An {@link AggregatedBidEvent} is sent by a {@link MatcherEndpoint}, such as the Auctioneer, when a new aggregated
 * {@link Bid} is created. This event always contains the raw aggregated bid. Some concentrators might modify the
 * aggregated bid to achieve some target. If you are interested in the final bid, you should look at the
 * {@link OutgoingBidUpdateEvent}.
 *
 * @author FAN
 * @version 2.0
 */
public class AggregatedBidEvent
    extends AgentEvent {

    /**
     * The new {@link Bid} created by the {@link MatcherEndpoint}.
     */
    private final Bid aggregatedBid;

    /**
     * Constructs an instance of this class.
     *
     * @param clusterId
     *            The id of the cluster the {@link MatcherEndpoint} subclass sending the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link MatcherEndpoint} subclass sending the UpdateEvent.
     * @param timestamp
     *            The time at which this event occurred
     * @param aggregatedBid
     *            The new {@link Bid} created by the {@link MatcherEndpoint} subclass.
     */
    public AggregatedBidEvent(String clusterId, String agentId, Date timestamp, Bid aggregatedBid) {
        super(clusterId, agentId, timestamp);
        this.aggregatedBid = aggregatedBid;
    }

    /**
     * @return the aggregated bid internally generated in the {@link MatcherEndpoint}.
     */
    public Bid getAggregatedBid() {
        return aggregatedBid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString()
               + ", aggregatedBid = "
               + aggregatedBid.toString();
    }

}
