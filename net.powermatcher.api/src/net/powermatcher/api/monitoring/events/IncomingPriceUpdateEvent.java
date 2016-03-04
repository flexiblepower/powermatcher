package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.messages.PriceUpdate;

/**
 * An {@link IncomingPriceUpdateEvent} is sent when an {@link AgentEndpoint} receives a new {@link PriceUpdate}.
 *
 * @author FAN
 * @version 2.1
 */
public class IncomingPriceUpdateEvent
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
     *            The time at which this event occurred
     * @param priceUpdate
     *            The received {@link PriceUpdate}.
     */
    public IncomingPriceUpdateEvent(String clusterId, String agentId, String sessionId, Date timestamp,
                                    PriceUpdate priceUpdate) {
        super(clusterId, agentId, sessionId, timestamp, priceUpdate);
    }
}
