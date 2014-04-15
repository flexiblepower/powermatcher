package net.powermatcher.simulation.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "children" })
public abstract class ConfigurationElementImpl<C extends ConfigurationElement<?>> implements ConfigurationElement<C> {
	private final List<C> children = new ArrayList<C>();
	protected final ConfigurationElementObservers observers = new ConfigurationElementObservers();

	private ConfigurationElement<?> parent;

	public boolean addChild(C child) {
		this.children.add(child);
		child.setParent(this);
		this.observers.notifyChildAdded(this, child);
		return true;
	}

	@Override
	public void addObserver(ConfigurationElementObserver observer) {
		observers.addObserver(observer);
	}

	@Override
	@XmlAnyElement(lax = true)
	public List<C> getChildren() {
		// TODO make unmodifiable
		return children;
	}

	@XmlTransient
	public ConfigurationElement<?> getParent() {
		return parent;
	}

	@XmlTransient
	public ScenarioDescriptor getScenarioDescriptor() {
		@SuppressWarnings("rawtypes")
		ConfigurationElement parent = this;
		while ((parent = parent.getParent()) != null) {
			if (parent instanceof ScenarioDescriptor) {
				return (ScenarioDescriptor) parent;
			}
		}

		return null;
	}

	@Override
	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}

	@Override
	public boolean removeChild(C child) {
		boolean succes = this.children.remove(child);
		this.observers.notifyChildRemoved(this, child);
		return succes;
	}

	@Override
	public void removeObserver(ConfigurationElementObserver observer) {
		this.observers.removeObserver(observer);
	}

	@Override
	public void setParent(ConfigurationElement<?> parent) {
		this.parent = parent;
		this.observers.notifyChanged(this);
	}
}
