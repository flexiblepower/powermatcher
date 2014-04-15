package net.powermatcher.simulation.configuration;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ConfigurationElementObservers implements ConfigurationElementObserver {
	private Set<ConfigurationElementObserver> observers = new CopyOnWriteArraySet<ConfigurationElementObserver>();

	public void addObserver(ConfigurationElementObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(ConfigurationElementObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void notifyChanged(ConfigurationElement element) {
		for (ConfigurationElementObserver observer : observers) {
			observer.notifyChanged(element);
		}
	}

	@Override
	public void notifyChildAdded(ConfigurationElement parent, ConfigurationElement child) {
		for (ConfigurationElementObserver observer : observers) {
			observer.notifyChildAdded(parent, child);
		}
	}

	@Override
	public void notifyChildRemoved(ConfigurationElement parent, ConfigurationElement child) {
		for (ConfigurationElementObserver observer : observers) {
			observer.notifyChildRemoved(parent, child);
		}
	}
}
