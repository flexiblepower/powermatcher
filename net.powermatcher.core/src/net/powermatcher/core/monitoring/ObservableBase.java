package net.powermatcher.core.monitoring;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.UpdateEvent;

public abstract class ObservableBase implements Observable {
	private final Set<Observer> observers = new CopyOnWriteArraySet<Observer>();

	@Override
	public void addObserver(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	public void publishEvent(UpdateEvent event) {
		for (Observer observer : observers) {
			observer.update(event);
		}
	}
}
