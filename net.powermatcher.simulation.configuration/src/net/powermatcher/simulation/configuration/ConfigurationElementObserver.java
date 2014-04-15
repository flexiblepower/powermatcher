package net.powermatcher.simulation.configuration;

public interface ConfigurationElementObserver {
	void notifyChanged(ConfigurationElement<?> element);

	void notifyChildAdded(ConfigurationElement<?> parent, ConfigurationElement<?> child);

	void notifyChildRemoved(ConfigurationElement<?> parent, ConfigurationElement<?> child);
}
