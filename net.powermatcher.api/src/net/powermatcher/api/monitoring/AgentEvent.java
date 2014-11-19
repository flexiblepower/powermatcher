package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;

/**
 * The base class for update events.
 * 
 * {@link AgentEndpoint} subclasses implement the {@link ObservableAgent} interface and can send an {@link AgentEvent} to
 * every {@link AgentObserver} that observes this class.
 * 
 * @author FAN
 * @version 1.0
 */

public abstract class AgentEvent {

    /**
     * The id of cluster the {@link AgentEndpoint} subclass sending the UpdateEvent is running in.
     */
    private final String clusterId;

    /**
     * The id of the {@link AgentEndpoint} subclass sending the UpdateEvent.
     */
    private final String agentId;

    /**
     * The id of the {@link Session} of the {@link AgentEndpoint} subclass sending the UpdateEvent
     */
    private final String sessionId;

    /**
     * The {@link Date} received from the {@link TimeService}
     */
    private final Date timestamp;

    /**
     * The qualifier of the event
     */
    private Qualifier qualifier;

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
     */
    public AgentEvent(String clusterId, String agentId, String sessionId, Date timestamp, Qualifier qualifier) {
        this.clusterId = clusterId;
        this.agentId = agentId;
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.qualifier = qualifier;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getClusterId() {
        return clusterId;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    @Override
    public String toString() {
        return "clusterId = " + this.clusterId + ", agentId = " + this.agentId + ", sessionId = " + this.sessionId
                + ", timestamp = " + this.timestamp + ", qualifier = " + qualifier.getDescription();
    }

}
