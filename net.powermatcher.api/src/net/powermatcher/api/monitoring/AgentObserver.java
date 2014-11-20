package net.powermatcher.api.monitoring;

public interface AgentObserver {
    void update(AgentEvent event);
}
