package net.powermatcher.api.monitoring;

import net.powermatcher.api.monitoring.events.AgentEvent;

public interface AgentObserver {
    void update(AgentEvent event);
}
