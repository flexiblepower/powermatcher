package net.powermatcher.core;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.Agent;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * Base implementation of an {@link Agent}. It provides basic functionality required in each {@link Agent}. Implements
 * the {@link ObservableAgent} interface to make sure the instance van send {@link AgentEvent}s to {@link AgentObserver}
 * s.
 *
 * @author FAN
 * @version 2.1
 */
public abstract class BaseAgent
    implements ObservableAgent {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected volatile FlexiblePowerContext context;

    /**
     * @see net.powermatcher.api.Agent#setContext(org.flexiblepower.context.FlexiblePowerContext)
     */
    @Override
    public void setContext(FlexiblePowerContext context) {
        this.context = context;
    }

    /**
     * Returns the current time in a {@link Date} object.
     *
     * @return A {@link Date} object, representing the current date and time
     */
    protected Date now() {
        if (context == null) {
            throw new IllegalStateException("The FlexiblePowerContext has not been set, is the PowerMatcher runtime active?");
        }
        return context.currentTime();
    }

    /**
     * Collection of {@link AgentObserver} services.
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
     * Publish an {@link AgentEvent} to the attached {@link AgentObserver} services.
     *
     * @param event
     *            The event to publish.
     */
    protected final void publishEvent(AgentEvent event) {
        for (AgentObserver observer : observers) {
            try {
                observer.handleAgentEvent(event);
            } catch (RuntimeException ex) {
                LOGGER.warn("Could not publish an event to observer [{}]: {}", observer, ex.getMessage());
            }
        }
    }
}
