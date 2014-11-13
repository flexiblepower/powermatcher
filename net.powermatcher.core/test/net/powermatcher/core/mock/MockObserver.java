package net.powermatcher.core.mock;

import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.AgentEvent;

public class MockObserver implements AgentObserver {

    private boolean hasReceivedEvent;

    @Override
    public void update(AgentEvent event) {
        this.hasReceivedEvent = true;
    }

    public boolean hasReceivedEvent() {
        return hasReceivedEvent;
    }
}
