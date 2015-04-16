package net.powermatcher.mock;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.AgentEvent;

public abstract class MockObservableAgent
    extends MockAgent
    implements ObservableAgent {
    private final Set<AgentObserver> observers = new CopyOnWriteArraySet<AgentObserver>();

    public MockObservableAgent(String agentId) {
        super(agentId);
    }

    @Override
    public void addObserver(AgentObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(AgentObserver observer) {
        observers.remove(observer);
    }

    public void publishEvent(AgentEvent event) {
        for (AgentObserver observer : observers) {
            observer.handleAgentEvent(event);
        }
    }
}
