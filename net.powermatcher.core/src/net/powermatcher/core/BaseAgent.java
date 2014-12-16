package net.powermatcher.core;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.Agent;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * Base implementation of an {@link Agent}. It provides basic functionality required in each {@link Agent}. Implements
 * the {@link Observable} interface, providing Observable functionality.
 * 
 * @author FAN
 * @version 1.0
 */
public abstract class BaseAgent implements ObservableAgent {

    private String agentId;

    private String clusterId;

    private String desiredParentId;

    /**
     * Collection of {@link Observer} services.
     */
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    @Override
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(AgentObserver observer) {
        observers.remove(observer);
    }

    @Override
    public String getAgentId() {
        return this.agentId;
    }

    protected void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getClusterId() {
        return this.clusterId;
    }

    protected void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public String getDesiredParentId() {
        return this.desiredParentId;
    }

    protected void setDesiredParentId(String desiredParentId) {
        this.desiredParentId = desiredParentId;
    }

    /**
     * Publish an {@link UpdateEvent} to the attached {@link Observer} services.
     * 
     * @param event
     *            The event to publish.
     */
    public void publishEvent(AgentEvent event) {
        for (AgentObserver observer : observers) {
            observer.update(event);
        }
    }

    public boolean canEqual(Object other) {
        return other instanceof BaseAgent;
    }

    @Override
    public boolean equals(Object obj) {
        BaseDeviceAgent other = (BaseDeviceAgent) ((obj instanceof BaseDeviceAgent) ? obj : null);
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        return canEqual(other) && other.getAgentId() == this.getClusterId() && this.getAgentId() == other.getAgentId()
                && this.getDesiredParentId() == other.getDesiredParentId();
    }

}
