package net.powermatcher.core;

import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.Agent;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * Base implementation of an {@link Agent}. It provides basic functionality required in each {@link Agent}. Implements
 * the {@link ObservableAgent} interface to make sure the instance van send {@link AgentEvent}s to {@link AgentObserver}
 * s.
 * 
 * @author FAN
 * @version 2.0
 */
public abstract class BaseAgent implements ObservableAgent {

    /**
     * The id of this Agent.
     */
    private String agentId;

    /**
     * The id of the cluster this Agent is running in.
     */
    private String clusterId;

    /**
     * The id of the {@link MatcherEndpoint} this Agent wants to connect to.
     */
    private String desiredParentId;

    /**
     * The service pid of a the managed Service of this instance in the OSGi container
     */
    private String servicePid;

    /**
     * Collection of {@link Observer} services.
     */
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(AgentObserver observer) {
        observers.remove(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentId() {
        return this.agentId;
    }

    /**
     * @param the
     *            new <code>String</code> value of agentId
     */
    protected void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * @param the
     *            new <code>String</code> value of servicePid
     */
    protected void setServicePid(String servicePid) {
        this.servicePid = servicePid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterId() {
        return this.clusterId;
    }

    /**
     * @param the
     *            new <code>String</code> value of clusterId.
     */
    protected void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDesiredParentId() {
        return this.desiredParentId;
    }

    /**
     * @param the
     *            new <code>String</code> value of desiredParentId.
     */
    protected void setDesiredParentId(String desiredParentId) {
        this.desiredParentId = desiredParentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServicePid() {
        return this.servicePid;
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 211 * ((agentId == null ? 0 : agentId.hashCode()) + (clusterId == null ? 0 : clusterId.hashCode()) + (desiredParentId == null ? 0
                : desiredParentId.hashCode()));
    }

}
