package net.powermatcher.api.monitoring;

import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * {@link AgentObserver} defines the interface with the basic functionality needed to observe an {@link ObservableAgent}
 * and receive {@link AgentEvent}s
 * 
 * @author FAN
 * @version 2.0
 */
public interface AgentObserver {

    /**
     * This method is called when an {@link ObservableAgent} sends an {@link AgentEvent}.
     * 
     * @param event
     *            the {@link AgentEvent} sent by the {@link ObservableAgent}
     */
    void update(AgentEvent event);
}
