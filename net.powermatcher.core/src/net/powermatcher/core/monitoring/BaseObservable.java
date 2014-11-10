package net.powermatcher.core.monitoring;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;

/**
 * Base class for {@link Observable) services.
 * Handles storage of {@link Observer} services with addObserver and 
 * removeObserver routines.
 *
 * @author FAN
 * @version 1.0
 */
public abstract class BaseObservable implements Observable {
	/**
	 * Collection of {@link Observer} services.
	 */
	private final Set<Observer> observers = new CopyOnWriteArraySet<Observer>();

	/**
	 * Adds an {@link Observer}.
	 * 
	 * @param observer the {@link Observer} to add. 
	 */
	@Override
	public void addObserver(Observer observer) {
		observers.add(observer);
	}

	/**
	 * Removes an {@link Observer}.
	 * 
	 * @param observer the {@link Observer} to remove. 
	 */
	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	/**
	 * Publish an {@link UpdateEvent} to the attached {@link Observer} services. 
	 * @param event The event to publish.
	 */
	public void publishEvent(UpdateEvent event) {
		for (Observer observer : observers) {
			observer.update(event);
		}
	}
}
