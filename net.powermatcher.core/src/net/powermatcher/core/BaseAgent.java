package net.powermatcher.core;

import java.util.Date;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.api.Agent;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.TimeService;
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
public abstract class BaseAgent
    implements ObservableAgent {

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
     * Collection of {@link Observer} services.
     */
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    protected TimeService timeService;

    protected ScheduledExecutorService executorService;

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
        return agentId;
    }

    /**
     * @param the
     *            new <code>String</code> value of agentId
     */
    protected void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClusterId() {
        return clusterId;
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
        return desiredParentId;
    }

    /**
     * @param the
     *            new <code>String</code> value of desiredParentId.
     */
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

    /**
     * Returns the current time in a {@link Date} object.
     *
     * @return A {@link Date} object, representing the current date and time
     */
    protected Date now() {
        if (timeService == null) {
            return null;
        } else {
            return timeService.currentDate();
        }
    }

    @Override
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    protected boolean isInitialized() {
        return !(executorService == null || timeService == null);
    }

}
