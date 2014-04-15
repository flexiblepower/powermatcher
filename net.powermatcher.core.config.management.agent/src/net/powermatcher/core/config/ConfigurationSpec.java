package net.powermatcher.core.config;


import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.service.cm.Configuration;

/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigurationSpec {
	/**
	 *
	 */
	public enum ConfigurationType {
		/**
		 * 
		 */
		factory, /**
		 * 
		 */
		singleton, /**
		 * 
		 */
		group
	}

	/**
	 * Define the type (ConfigurationType) field.
	 */
	private ConfigurationType type;
	/**
	 * Define the ID (String) field.
	 */
	private String id;
	/**
	 * Define the cluster ID (String) field.
	 */
	private String clusterId;
	/**
	 * Define the PID (String) field.
	 */
	private String pid;
	/**
	 * Define the template (boolean) field.
	 */
	private boolean template = false;
	/**
	 * Define the parent (ConfigurationSpec) field.
	 */
	private ConfigurationSpec parent;
	/**
	 * Define the children (Set<ConfigurationSpec>) field.
	 */
	private Set<ConfigurationSpec> children;
	/**
	 * Define the properties (Map<String,Object>) field.
	 */
	private Map<String, Object> properties;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param config
	 *            The configuration (<code>ConfigurationSpec</code>) parameter.
	 * @see #ConfigurationSpec(ConfigurationType,String,String,String,boolean,ConfigurationSpec)
	 */
	public ConfigurationSpec(final ConfigurationSpec config) {
		super();
		this.type = config.getType();
		this.id = config.getId();
		this.clusterId = config.getClusterId();
		this.pid = config.getPid();
		this.parent = null;
		this.children = null;
		this.properties = config.getAllProperties();
	}

	/**
	 * Constructs an instance of this class from the specified type, ID, cluster
	 * ID, PID, template and parent parameters.
	 * 
	 * @param type
	 *            The type (<code>ConfigurationType</code>) parameter.
	 * @param id
	 *            The ID (<code>String</code>) parameter.
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param pid
	 *            The PID (<code>String</code>) parameter.
	 * @param template
	 *            The template (<code>boolean</code>) parameter.
	 * @param parent
	 *            The parent (<code>ConfigurationSpec</code>) parameter.
	 * @see #ConfigurationSpec(ConfigurationSpec)
	 */
	public ConfigurationSpec(final ConfigurationType type, final String id, final String clusterId, final String pid,
			final boolean template, final ConfigurationSpec parent) {
		super();
		this.type = type;
		this.id = id;
		this.clusterId = clusterId;
		this.pid = pid;
		this.template = template;
		this.parent = parent;
		this.properties = new HashMap<String, Object>();
	}

	/**
	 * Add child with the specified configuration parameter.
	 * 
	 * @param config
	 *            The configuration (<code>ConfigurationSpec</code>) parameter.
	 */
	public void addChild(final ConfigurationSpec config) {
		if (this.children == null) {
			this.children = new HashSet<ConfigurationSpec>();
		}
		this.children.add(config);
	}

	/**
	 * Add property with the specified key and value parameters.
	 * 
	 * @param key
	 *            The key (<code>String</code>) parameter.
	 * @param value
	 *            The value (<code>Object</code>) parameter.
	 */
	public void addProperty(final String key, final Object value) {
		if (this.properties == null) {
			this.properties = new HashMap<String, Object>();
		}
		this.properties.put(key, value);
	}

	/**
	 * Configuration set and return the Set<ConfigurationSpec> result.
	 * 
	 * @return the set of configurations of all configurations in the hierarchy.
	 *         The hierarchy will not be preserved, all relations to the parent
	 *         configuration groups will be set to null.
	 */
	public Set<ConfigurationSpec> configurationSet() {
		Set<ConfigurationSpec> all = new HashSet<ConfigurationSpec>();

		/* Add itself to the configuration set */
		if (this.getType().equals(ConfigurationType.singleton) || this.getType().equals(ConfigurationType.factory)) {
			all.add(new ConfigurationSpec(this));
		}

		/* Add the children */
		if (this.children != null) {
			for (ConfigurationSpec c : this.children) {
				if (!c.isTemplate()) {
					all.addAll(c.configurationSet());
				}
			}
		}

		return all;
	}

	/**
	 * Equals with the specified configuration parameter and return the boolean
	 * result.
	 * 
	 * @param config
	 *            The configuration (<code>Configuration</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 * @see #equals(Object)
	 */
	public boolean equals(final Configuration config) {
		String fpid = config.getFactoryPid();
		ConfigurationType type = (fpid == null) ? ConfigurationSpec.ConfigurationType.singleton
				: ConfigurationSpec.ConfigurationType.factory;

		/*
		 * Compare the id and pid. Cluster id is ignored because there is no
		 * corresponding attribute in the osgi Configuration object.
		 */
		if (!this.type.equals(type)
				|| (this.type.equals(ConfigurationType.factory) && this.pid != null && !this.pid.equals(fpid))
				|| (this.type.equals(ConfigurationType.singleton) && this.pid != null && !this.pid.equals(config.getPid()))) {
			return false;
		}

		/*
		 * Compare the properties. Property sets are considered equal when all
		 * properties of this instance have also the same value as in the
		 * compared object.
		 */
		@SuppressWarnings("unchecked")
		Dictionary<String, Object> dict = config.getProperties();
		for (String key : this.properties.keySet()) {
			if (dict.get(key) == null || !dict.get(key).equals(this.properties.get(key))) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Equals with the specified c parameter and return the boolean result.
	 * 
	 * @param c
	 *            The c (<code>Object</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 * @see #equals(Configuration)
	 */
	@Override
	public boolean equals(final Object c) {
		if (!(c instanceof ConfigurationSpec)) {
			return false;
		}

		ConfigurationSpec config = (ConfigurationSpec) c;

		/* Compare the id and pid */
		if (!this.type.equals(config.getType()) || !this.id.equals(config.getId())
				|| !this.clusterId.equals(config.getClusterId()) || (this.pid != null && !this.pid.equals(config.getPid()))) {
			return false;
		}

		/*
		 * Compare the properties. Property sets are considered equal when all
		 * properties of this instance have also the same value as in the
		 * compared object.
		 */
		Map<String, Object> targetProperties = config.getProperties();
		for (String key : this.properties.keySet()) {
			if (!(targetProperties.containsKey(key) && targetProperties.get(key).equals(this.properties.get(key)))) {
				return false;
			}

		}

		return true;
	}

	/**
	 * Find configuration with the specified PID and ID parameters and return
	 * the ConfigurationSpec result.
	 * 
	 * @param pid
	 *            The PID (<code>String</code>) parameter.
	 * @param id
	 *            The ID (<code>String</code>) parameter.
	 * @return Results of the find configuration (<code>ConfigurationSpec</code>
	 *         ) value.
	 */
	public ConfigurationSpec findConfiguration(final String pid, final String id) {
		if ((this.getType().equals(ConfigurationType.singleton) || this.getType().equals(ConfigurationType.factory))
				&& !this.isTemplate() && this.id != null && this.id.equals(id) && this.pid != null && this.pid.equals(pid)) {

			return this;
		}

		if (this.children != null) {
			for (ConfigurationSpec cs : this.children) {
				ConfigurationSpec config = cs.findConfiguration(pid, id);
				if (config != null) {
					return config;
				}
			}
		}

		/* Configuration not found */
		return null;
	}

	/**
	 * Find template with the specified name parameter and return the
	 * ConfigurationSpec result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the find template (<code>ConfigurationSpec</code>)
	 *         value.
	 * @see #isTemplate()
	 * @see #setTemplate(boolean)
	 */
	private ConfigurationSpec findTemplate(final String name) {
		if (this.isTemplate()) {
			return null;
		}

		/*
		 * First look in this configuration (group) for the template being searched.
		 */
		if (getChildren() != null) {
			for (ConfigurationSpec conf : getChildren()) {
				if (conf.isTemplate() && conf.getPid().equals(name)) {
					return conf;
				}
			}
		}

		/* Start looking on level higher in the hierarchy */
		if (this.parent != null) {
			return this.parent.findTemplate(name);
		}

		/* Template configuration not found: return null */
		return null;
	}

	/**
	 * Gets the all properties (Map<String,Object>) value.
	 * 
	 * @return The all properties (<code>Map<String,Object></code>) value.
	 */
	public Map<String, Object> getAllProperties() {
		Map<String, Object> allProps = new HashMap<String, Object>();
		if (this.parent != null) {
			allProps.putAll(this.parent.getAllProperties());
		}
		if (this.pid != null && !this.type.equals(ConfigurationType.group) && this.parent != null) {
			ConfigurationSpec template = this.parent.findTemplate(this.pid);
			if (template != null) {
				allProps.putAll(template.getProperties());
			}
		}
		allProps.putAll(this.properties);
		return allProps;
	}

	/**
	 * Gets the children (Set<ConfigurationSpec>) value.
	 * 
	 * @return TODO
	 * @see #setChildren(Set)
	 */
	public Set<ConfigurationSpec> getChildren() {
		return this.children;
	}

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return the clusterId
	 * @see #setClusterId(String)
	 */
	public String getClusterId() {
		return this.clusterId;
	}

	/**
	 * Gets the ID (String) value.
	 * 
	 * @return the id
	 * @see #getClusterId()
	 * @see #setClusterId(String)
	 * @see #setId(String)
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the parent (ConfigurationSpec) value.
	 * 
	 * @return the parent
	 * @see #setParent(ConfigurationSpec)
	 */
	public ConfigurationSpec getParent() {
		return this.parent;
	}

	/**
	 * Gets the PID (String) value.
	 * 
	 * @return the pid
	 * @see #setPid(String)
	 */
	public String getPid() {
		return this.pid;
	}

	/**
	 * Gets the properties (Map<String,Object>) value.
	 * 
	 * @return the properties
	 * @see #getAllProperties()
	 * @see #setProperties(Map)
	 */
	public Map<String, Object> getProperties() {
		return this.properties;
	}

	/**
	 * Gets the type (ConfigurationType) value.
	 * 
	 * @return the type
	 * @see #setType(ConfigurationType)
	 */
	public ConfigurationType getType() {
		return this.type;
	}

	/**
	 * Gets the template (boolean) value.
	 * 
	 * @return Results of the is template (<code>boolean</code>) value.
	 * @see #setTemplate(boolean)
	 */
	public boolean isTemplate() {
		return this.template;
	}

	/**
	 * Sets the children value.
	 * 
	 * @param children
	 *            The children (<code>Set<ConfigurationSpec></code>) parameter.
	 * @see #getChildren()
	 */
	public void setChildren(final Set<ConfigurationSpec> children) {
		this.children = children;
	}

	/**
	 * Sets the cluster ID value.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @see #getClusterId()
	 */
	public void setClusterId(final String clusterId) {
		this.clusterId = clusterId;
	}

	/**
	 * Sets the ID value.
	 * 
	 * @param id
	 *            The ID (<code>String</code>) parameter.
	 * @see #getClusterId()
	 * @see #getId()
	 * @see #setClusterId(String)
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * Sets the parent value.
	 * 
	 * @param parent
	 *            The parent (<code>ConfigurationSpec</code>) parameter.
	 * @see #getParent()
	 */
	public void setParent(final ConfigurationSpec parent) {
		this.parent = parent;
	}

	/**
	 * Sets the PID value.
	 * 
	 * @param pid
	 *            The PID (<code>String</code>) parameter.
	 * @see #getPid()
	 */
	public void setPid(final String pid) {
		this.pid = pid;
	}

	/**
	 * Sets the properties value.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 * @see #getAllProperties()
	 * @see #getProperties()
	 */
	public void setProperties(final Map<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * Sets the template value.
	 * 
	 * @param template
	 *            The template (<code>boolean</code>) parameter.
	 * @see #isTemplate()
	 */
	public void setTemplate(final boolean template) {
		this.template = template;
	}

	/**
	 * Sets the type value.
	 * 
	 * @param type
	 *            The type (<code>ConfigurationType</code>) parameter.
	 * @see #getType()
	 */
	public void setType(final ConfigurationType type) {
		this.type = type;
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("------------------------------------------\n");
		sb.append("Configuration id=");
		sb.append(this.id);
		sb.append(" cluster_id=");
		sb.append(this.clusterId);
		sb.append(" pid=");
		sb.append(this.pid);
		sb.append(" type=");
		sb.append(this.type);
		sb.append(" template= " + this.template);
		sb.append("\n");
		sb.append((this.parent == null) ? "no parent\n" : "(id=" + this.parent.getId() + ", pid=" + this.parent.getPid()
				+ ")\n");
		sb.append("properties:");
		for (String key : this.properties.keySet()) {
			sb.append(" key=");
			sb.append(key);
			sb.append(" value=");
			sb.append(this.properties.get(key));
			sb.append("\n");
		}
		sb.append("\n");

		if (this.children != null && this.children.size() != 0) {
			for (ConfigurationSpec c : this.children) {
				sb.append(c);
			}
		} else {
			sb.append("no child configurations:\n");
		}

		return sb.toString();
	}

}
