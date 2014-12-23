package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.Qualifier;

/**
 * An {@link IncomingPriceUpdateEvent} is sent when an {@link AgentEndpoint} receives a new {@link PriceUpdate}.
 * 
 * @author FAN
 * @version 1.0
 */
public class IncomingPriceUpdateEvent extends PriceUpdateEvent {

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
     *            The received {@link PriceUpdate}.
     */
    public IncomingPriceUpdateEvent(String clusterId, String agentId, String sessionId, Date timestamp,
            PriceUpdate priceUpdate, Qualifier qualifier) {
        super(clusterId, agentId, sessionId, timestamp, priceUpdate, qualifier);
    }

    @Override
    public String toString() {
        return IncomingPriceUpdateEvent.class.getSimpleName() + " " + super.toString() + ", priceUpdate = "
                + getPriceUpdate().toString();
    }
}
