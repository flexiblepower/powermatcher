package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.messages.BidUpdate;

/**
 * An {@link IncomingBidUpdateEvent} is sent when an {@link AgentEndpoint} receives a new {@link Bid}.
 *
 * @author FAN
 * @version 2.0
 */
public class IncomingBidUpdateEvent
    extends BidUpdateEvent {

    /**
     * The id of the Agent that sent the {@link Bid}
     */
    private final String fromAgentId;

    /**
     * Constructs an instance of this class.
     *
     * @param clusterId
     *            The id of the cluster the {@link AgentEndpoint} subclass sending the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link AgentEndpoint} subclass sending the UpdateEvent.
     * @param sessionId
     *            The id of the {@link Session} of the {@link AgentEndpoint} subclass sending the UpdateEvent
     * @param timestamp
     *            The time at which this event occurred
     * @param fromAgentId
     *            The id of the Agent that sent the {@link Bid}.
     * @param bidUpdate
     *            The received {@link BidUpdate}.
     */
    public IncomingBidUpdateEvent(String clusterId, String agentId, String sessionId, Date timestamp, String fromAgentId,
                            BidUpdate bidUpdate) {
        super(clusterId, agentId, sessionId, timestamp, bidUpdate);
        this.fromAgentId = fromAgentId;
    }

    /**
     * @return the current value of agentId.
     */
    public String getFromAgentId() {
        return fromAgentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ", fromAgentId = " + fromAgentId;
    }
}
