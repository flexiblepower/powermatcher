package net.powermatcher.core.config.management.agent;


import net.powermatcher.core.messaging.framework.config.MessagingAdapterConfiguration;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface ConfigManagerConfiguration extends MessagingAdapterConfiguration {

	/**
	 * Define the prefix for reading property value defaults from the system
	 * properties.
	 */
	public static final String PROPERTY_PREFIX = "net.powermatcher.core.config.management.agent";

	/**
	 * Define the node id name constant and default for the configuration data.
	 */
	public static final String CONFIGURATION_NODE_ID_PROPERTY = "node.id";

	/**
	 * 
	 */
	public final static String CONFIGURATION_NODE_ID_DEFAULT = "defaultnode";

	/**
	 * Define the node id description.
	 */
	public static final String CONFIGURATION_NODE_ID_DESCRIPTION = "ID of the node to be configured";

	/**
	 * Define the property name constant and default for the update interval
	 * property.
	 */
	public static final String UPDATE_INTERVAL_PROPERTY = "update.interval";

	/**
	 * 
	 */
	public final static String UPDATE_INTERVAL_DEFAULT_STR = "300";

	/**
	 * Define the property name constant and default for the update interval
	 * property.
	 */
	public static final String UPDATE_INTERVAL_DESCRIPTION = "Configuration update interval in seconds.";
	/**
	 * 
	 */
	public static final int UPDATE_INTERVAL_DEFAULT = Integer.valueOf(UPDATE_INTERVAL_DEFAULT_STR);

	/**
	 * Define the property name constant and default for the configuration data
	 * url.
	 */
	public static final String CONFIGURATION_DATA_URL_PROPERTY = "configuration.data.url";

	/**
	 * 
	 */
	public final static String CONFIGURATION_DATA_URL_DEFAULT = "file:conf-local/default_config.xml";

	/**
	 * 
	 */
	public final static String CONFIGURATION_DATA_URL_DESCRIPTION = "URL for retrieving the configuration xml";

	/**
	 * Define the property name constant for the configuration data username.
	 */
	public static final String CONFIGURATION_DATA_USERNAME_PROPERTY = "configuration.data.username";

	/**
	 * Define the description for the configuration data username.
	 */
	public static final String CONFIGURATION_DATA_USERNAME_DESCRIPTION = "User name for getting configuration URL";

	/**
	 * Define the property name constant for the configuration data password.
	 */
	public static final String CONFIGURATION_DATA_PASSWORD_PROPERTY = "configuration.data.password";

	/**
	 * Define the description for the configuration data password.
	 */
	public static final String CONFIGURATION_DATA_PASSWORD_DESCRIPTION = "Password for getting configuration URL";

	/**
	 * @return configuration.data.password property.
	 */
	public String configuration_data_password();

	/**
	 * @return configuration.data.url property.
	 */
	public String configuration_data_url();

	/**
	 * @return configuration.data.userid property.
	 */
	public String configuration_data_userid();

	/**
	 * @return node.id property.
	 */
	public String node_id();

	/**
	 * @return update.interval property.
	 */
	public int update_interval();

}
