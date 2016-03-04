package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;

/**
 * This immutable abstract data object defines the basis for a event. AgentEvent is an event worthy of notifying other
 * object of.
 *
 * Classes implementing the {@link ObservableAgent} interface and can send an {@link AgentEvent} to every
 * {@link AgentObserver} that observes this class.
 *
 * @author FAN
 * @version 2.1
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
     * The time at which this event occurred
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
     *            The time at which this event occurred
     */
    public AgentEvent(String clusterId, String agentId, Date timestamp) {
        this.clusterId = clusterId;
        this.agentId = agentId;
        this.timestamp = timestamp;
    }

    /**
     * @return the current value of agentId.
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @return the current value of timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @return the current value of clusterId.
     */
    public String getClusterId() {
        return clusterId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + ": clusterId = "
               + clusterId
               + ", agentId = "
               + agentId
               + ", timestamp = "
               + timestamp;
    }
}
