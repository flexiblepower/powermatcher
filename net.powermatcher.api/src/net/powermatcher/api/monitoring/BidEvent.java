package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * An {@link BidEvent} is sent when an {@link Bid} is sent or Received by an {@link AgentRole} or a {@link MatcherRole}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class BidEvent extends AgentEvent {

    /**
     * The new {@link Bid} created by the {@link AgentRole} subclass.
     */
    private final Bid bid;

    /**
     * Constructs an instance of this class.
     * 
     * @param clusterId
	 *            The id of the cluster the {@link AgentRole} subclass sending
	 *            the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link AgentRole} subclass sending the UpdateEvent.
     * @param sessionId
     *            The id of the {@link Session} of the {@link AgentRole} subclass sending the UpdateEvent
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     * @param bid
     *            The new {@link Price} created by the {@link AgentRole} subclass.
     */
    public BidEvent(String clusterId, String agentId, String sessionId, Date timestamp, Bid bid, Qualifier qualifier) {
        super(clusterId, agentId, sessionId, timestamp, qualifier);
        this.bid = bid;
    }

    public Bid getBid() {
        return bid;
    }

    @Override
    public String toString() {
        return OutgoingBidEvent.class.getSimpleName() + " " + super.toString() + ", bid = " + bid.toString();
    }
}
