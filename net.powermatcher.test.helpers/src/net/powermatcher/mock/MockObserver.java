package net.powermatcher.mock;

import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * 
 * @author FAN
 * @version 2.1
 */
public class MockObserver implements AgentObserver {

    private boolean hasReceivedEvent;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAgentEvent(AgentEvent event) {
        this.hasReceivedEvent = true;
    }

    public boolean hasReceivedEvent() {
        return hasReceivedEvent;
    }
}
