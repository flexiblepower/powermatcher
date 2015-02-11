package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.messages.PriceUpdate;

/**
 * An {@link OutgoingPriceUpdateEvent} is sent when an {@link AgentEndpoint} sends a new {@link PriceUpdate}.
 *
 * @author FAN
 * @version 2.0
 */
public class OutgoingPriceUpdateEvent
    extends PriceUpdateEvent {

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
     * @param priceUpdate
     *            The new {@link PriceUpdate} created by the {@link AgentEndpoint} subclass.
     */
    public OutgoingPriceUpdateEvent(String clusterId, String agentId, String sessionId, Date timestamp,
                                    PriceUpdate priceUpdate) {
        super(clusterId, agentId, sessionId, timestamp, priceUpdate);
    }
}
