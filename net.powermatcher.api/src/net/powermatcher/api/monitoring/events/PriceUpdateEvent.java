package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.messages.PriceUpdate;

/**
 * An {@link PriceUpdateEvent} is sent when an {@link AgentEndpoint} receives a new {@link PriceUpdate}.
 *
 * @author FAN
 * @version 2.0
 */
public abstract class PriceUpdateEvent
    extends AgentEvent {

    /**
     * The received {@link PriceUpdate}
     */
    private final PriceUpdate priceUpdate;

    /**
     * The id of the {@link Session} of the {@link AgentEndpoint} subclass sending the UpdateEvent
     */
    private final String sessionId;

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
    public PriceUpdateEvent(String clusterId, String agentId, String sessionId, Date timestamp,
                            PriceUpdate priceUpdate) {
        super(clusterId, agentId, timestamp);
        this.priceUpdate = priceUpdate;
        this.sessionId = sessionId;
    }

    /**
     * @return the current value of priceUpdate.
     */
    public PriceUpdate getPriceUpdate() {
        return priceUpdate;
    }

    /**
     * @return the current value of sessionId.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ", sessionId = " + sessionId + ", priceUpdate = "
               + priceUpdate.toString();
    }
}
