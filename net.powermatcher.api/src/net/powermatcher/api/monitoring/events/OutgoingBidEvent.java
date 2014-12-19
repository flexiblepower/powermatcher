package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.Qualifier;

/**
 * An {@link OutgoingBidEvent} is sent when an {@link AgentEndpoint} sends a new {@link Bid}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class OutgoingBidEvent extends BidEvent {

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
     *            The {@link Date} received from the {@link TimeService}
     * @param bid
     *            The new {@link Price} created by the {@link AgentEndpoint} subclass.
     */
    public OutgoingBidEvent(String clusterId, String agentId, String sessionId, Date timestamp, Bid bid,
            Qualifier qualifier) {
        super(clusterId, agentId, sessionId, timestamp, bid, qualifier);
    }

    @Override
    public String toString() {
        return OutgoingBidEvent.class.getSimpleName() + " " + super.toString() + ", bid = " + getBid().toString();
    }
}
