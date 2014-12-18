package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.Qualifier;

/**
 * An {@link PriceUpdateEvent} is sent when an {@link AgentEndpoint} receives a new {@link PriceUpdate}.
 * 
 * @author FAN
 * @version 1.0
 */
public abstract class PriceUpdateEvent extends AgentEvent {

    /**
     * The received {@link PriceUpdate}
     */
    private final PriceUpdate priceUpdate;

    /**
     * The qualifier of the event
     */
    private Qualifier qualifier;

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
     *            The {@link Date} received from the {@link TimeService}
     * @param priceUpdate
     *            The received {@link PriceUpdate}.
     */
    public PriceUpdateEvent(String clusterId, String agentId, String sessionId, Date timestamp,
            PriceUpdate priceUpdate, Qualifier qualifier) {
        super(clusterId, agentId, timestamp);
        this.priceUpdate = priceUpdate;
        this.qualifier = qualifier;
        this.sessionId = sessionId;
    }

    public PriceUpdate getPriceUpdate() {
        return priceUpdate;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return PriceUpdateEvent.class.getSimpleName() + " " + super.toString() + ", qualifier = "
                + qualifier.getDescription() + ", sessionId = " + this.sessionId + ", priceUpdate = "
                + priceUpdate.toString();
    }
}
