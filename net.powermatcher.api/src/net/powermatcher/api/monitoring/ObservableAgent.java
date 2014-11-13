package net.powermatcher.api.monitoring;

public interface ObservableAgent {
    String getObserverId();

    void addObserver(AgentObserver observer);

    void removeObserver(AgentObserver observer);
}
