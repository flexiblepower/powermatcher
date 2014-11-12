package net.powermatcher.core.monitoring;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.AgentEvent;

/**
 * Base class for {@link Observable) services. Handles storage of {@link AgentObserver} services with addObserver and
 * removeObserver routines.
 * 
 * @author FAN
 * @version 1.0
 */
public abstract class BaseObservable implements ObservableAgent {
    /**
     * Collection of {@link AgentObserver} services.
     */
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    /**
     * Adds an {@link AgentObserver}.
     * 
     * @param observer
     *            the {@link AgentObserver} to add.
     */
    @Override
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an {@link AgentObserver}.
     * 
     * @param observer
     *            the {@link AgentObserver} to remove.
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
    public void publishEvent(AgentEvent event) {
        for (AgentObserver observer : observers) {
            observer.update(event);
        }
    }
}
