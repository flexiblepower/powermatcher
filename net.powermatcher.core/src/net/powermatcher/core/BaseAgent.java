package net.powermatcher.core;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.Agent;
import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;

/**
 * Base implementation of an {@link Agent}. It provides basic functionality required in each {@link Agent}.
 * Implements the {@link Observable} interface, providing Observable functionality.
 * 
 * @author FAN
 * @version 1.0
 */
public abstract class BaseAgent implements Agent, Observable {

    private String agentId;

    private String clusterId;
    
    private String desiredParentId;
    
    /**
     * Collection of {@link Observer} services.
     */
    private final Set<Observer> observers = new CopyOnWriteArraySet<Observer>();
    
	@Override
	public String getObserverId() {
		// TODO remove this, no longer needed when agentId is available.
		return this.getAgentId();
	}

	@Override
	public void addObserver(Observer observer) {
        observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer) {
        observers.remove(observer);
	}

	@Override
	public String getAgentId() {
		return this.agentId;
	}

	protected void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	@Override
	public String getClusterId() {
		return this.clusterId;
	}

	protected void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	@Override
	public String getDesiredParentId() {
		return this.desiredParentId;
	}

	protected void setDesiredParentId(String desiredParentId) {
		this.desiredParentId = desiredParentId;
	}

    /**
     * Publish an {@link UpdateEvent} to the attached {@link Observer} services.
     * 
     * @param event
     *            The event to publish.
     */
    public void publishEvent(UpdateEvent event) {
        for (Observer observer : observers) {
            observer.update(event);
        }
    }
}
