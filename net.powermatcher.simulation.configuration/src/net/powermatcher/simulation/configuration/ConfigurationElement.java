package net.powermatcher.simulation.configuration;

import java.util.List;

public interface ConfigurationElement<C extends ConfigurationElement<?>> {
	ConfigurationElement<?> getParent();

	void setParent(ConfigurationElement<?> parent);

	boolean hasChildren();

	List<C> getChildren();

	boolean addChild(C child);

	boolean removeChild(C child);

	void addObserver(ConfigurationElementObserver observer);

	void removeObserver(ConfigurationElementObserver observer);
}
