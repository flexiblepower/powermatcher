package net.powermatcher.core.configurable;


import java.util.Map;
import java.util.Properties;

import net.powermatcher.core.configurable.service.Configurable;


/**
 * @author IBM
 * @version 0.9.0
 * 
 * <p>
 * The prefixed configuration extends the BaseConfiguration with the
 * option to use prefixes for property names.
 * </p>
 * <p>
 * For example this makes it possible to create a configuration where
 * </p>
 * <code>
 * agent.auctioneer1.name = "auctioneer1";
 * agent.auctioneer1.currency = "USD";
 * agent.auctioneer1.update_interval = "15";
 * </code>
 * <p>
 * In this case the configuration can have a prefix "agent.auctioneer1" and
 * you can simply retrieve the name value using:
 * <p>
 * <code>
 * String name = conf.getStringProperty("name");
 * </code>
 * </p>
 */
public class PrefixedConfiguration extends BaseConfiguration {
	/**
	 * Define the property prefix (String) field.
	 */
	private String propertyPrefix;

	/**
	 * Constructs an instance of this class from the specified parent,
	 * properties and property prefix parameters.
	 * 
	 * @param parent
	 *            The parent (<code>ConfigurationService</code>) parameter.
	 * @param properties
	 *            The properties (<code>Map<String, Object></code>) parameter.
	 * @param propertyPrefix
	 *            The property prefix (<code>String</code>) parameter.
	 * @see BaseConfiguration#BaseConfiguration(Configurable, Map)
	 */
	public PrefixedConfiguration(final Configurable parent, final Map<String, Object> properties,
			final String propertyPrefix) {
		super(parent, properties);
		this.propertyPrefix = propertyPrefix;
	}

	/**
	 * Constructs an instance of this class from the specified properties and
	 * property prefix parameters.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String, Object></code>) parameter.
	 * @param propertyPrefix
	 *            The property prefix (<code>String</code>) parameter.
	 * @see #PrefixedConfiguration(Configurable,Map,String)
	 */
	public PrefixedConfiguration(final Map<String, Object> properties, final String propertyPrefix) {
		this(null, properties, propertyPrefix);
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Properties</code>) parameter.
	 * @param propertyPrefix
	 *            The property prefix (<code>String</code>) parameter.
	 * @see #PrefixedConfiguration(Configurable, Properties, String)
	 */
	public PrefixedConfiguration(final Properties properties, final String propertyPrefix) {
		this(null, properties, propertyPrefix);
	}

	/**
	 * Constructs an instance of this class from the specified parent,
	 * properties and property prefix parameters.
	 * 
	 * @param parent
	 *            The parent (<code>ConfigurationService</code>) parameter.
	 * @param properties
	 *            The properties (<code>Properties</code>) parameter.
	 * @param propertyPrefix
	 *            The property prefix (<code>String</code>) parameter.
	 * @see #PrefixedConfiguration(Configurable, Map, String)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public PrefixedConfiguration(final Configurable parent, final Properties properties, final String propertyPrefix) {
		this(parent, (Map) properties, propertyPrefix);
	}

	/**
	 * Get property with the specified name parameter and return the Object
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get property (<code>Object</code>) value.
	 */
	@Override
	public Object getProperty(final String name) {
		try {
			return super.getProperty(this.propertyPrefix + SEPARATOR + name);
		} catch (IllegalArgumentException e) {
			return super.getProperty(name);
		}
	}

}
