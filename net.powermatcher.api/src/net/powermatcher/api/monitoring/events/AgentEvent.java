package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;

/**
 * The base class for update events.
 * 
 * {@link AgentEndpoint} subclasses implement the {@link ObservableAgent} interface and can send an {@link AgentEvent}
 * to every {@link AgentObserver} that observes this class.
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
     * The {@link Date} received from the {@link TimeService}
     */
    private final Date timestamp;

    /**
     * Constructs an instance of this class.
     * 
     * @param clusterId
     *            The id of the cluster the {@link AgentEndpoint} subclass sending the UpdateEvent is running in.
     * @param agentId
     *            The id of the {@link AgentEndpoint} subclass sending the UpdateEvent.
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     */
    public AgentEvent(String clusterId, String agentId, Date timestamp) {
        this.clusterId = clusterId;
        this.agentId = agentId;
        this.timestamp = timestamp;
    }

    public String getAgentId() {
        return agentId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getClusterId() {
        return clusterId;
    }

    @Override
    public String toString() {
        return "clusterId = " + this.clusterId + ", agentId = " + this.agentId + ", timestamp = " + this.timestamp;
    }

}
