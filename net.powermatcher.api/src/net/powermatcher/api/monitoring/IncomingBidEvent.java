package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;

/**
 * An {@link IncomingBidEvent} is sent when an {@link AgentRole} receives a new {@link Bid}.
 * 
 * @author FAN
 * @version 1.0
 */
public class IncomingBidEvent extends BidEvent {

    /**
     * The id of the Agent that sent the {@link Bid}
     */
    private String fromAgentId;

    /**
     * Constructs an instance of this class.
     * @param clusterId
	 *            The id of the cluster the {@link AgentRole} subclass sending
	 *            the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link AgentRole} subclass sending the UpdateEvent.
     * @param sessionId
     *            The id of the {@link Session} of the {@link AgentRole} subclass sending the UpdateEvent
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     * @param fromAgentId
     *            The id of the Agent that sent the {@link Bid}.
     * @param bid
     *            The received {@link Bid}.
     */
    public IncomingBidEvent(String clusterId, String agentId, String sessionId, Date timestamp, String fromAgentId, Bid bid, Qualifier qualifier) {
        super(clusterId, agentId, sessionId, timestamp, bid, qualifier);
        this.fromAgentId = fromAgentId;
    }

    public String getFromAgentId() {
        return fromAgentId;
    }

    @Override
    public String toString() {
        return IncomingBidEvent.class.getSimpleName() + " " + super.toString() + ", fromAgentId = "
                + this.fromAgentId + ", bid = " + getBid().toString();
    }
}
