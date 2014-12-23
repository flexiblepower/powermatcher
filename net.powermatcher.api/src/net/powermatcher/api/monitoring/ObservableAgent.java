package net.powermatcher.api.monitoring;

import net.powermatcher.api.Agent;

public interface ObservableAgent extends Agent {
    void addObserver(AgentObserver observer);

    void removeObserver(AgentObserver observer);
}
