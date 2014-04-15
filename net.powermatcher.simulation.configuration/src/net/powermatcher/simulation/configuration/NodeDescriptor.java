package net.powermatcher.simulation.configuration;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;

@XmlRootElement(name = "node")
@XmlType(propOrder = { "factoryPid", "configuration" })
public abstract class NodeDescriptor<C extends ConfigurationElement<?>> extends ConfigurationElementImpl<C> {
	private HashMap<String, Object> configuration;

	private String factoryPid;

	public void changeConfigurationParameter(String key, Object value) {
		this.configuration.put(key, value);
		this.observers.notifyChanged(this);
	}

	public String getClusterId() {
		ConfigurationElement<?> parent = this;
		while ((parent = parent.getParent()) != null) {
			if (parent instanceof ClusterDescriptor) {
				return ((ClusterDescriptor) parent).getClusterId();
			}
		}

		if (getConfiguration() == null) {
			return null;
		}

		return (String) getConfiguration().get(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY);
	}

	@XmlElement(name = "configuration")
	@XmlJavaTypeAdapter(HashMapAdapter.class)
	public HashMap<String, Object> getConfiguration() {
		// TODO: prettier serialization
		return configuration;
	}

	public String getFactoryPid() {
		return factoryPid;
	}

	public String getId() {
		if (configuration == null) {
			return null;
		}

		return (String) configuration.get(IdentifiableObjectConfiguration.ID_PROPERTY);
	}

	public void setConfiguration(HashMap<String, Object> configuration) {
		this.configuration = configuration;
	}

	public void setFactoryPid(String factoryPid) {
		this.factoryPid = factoryPid;
	}
}
