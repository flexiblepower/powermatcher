package net.powermatcher.core.messaging.mqttv3;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.mqttv3.config.Mqttv3ConnectionConfiguration;
import net.powermatcher.core.messaging.service.MessagingConnectionDefinition;
import net.powermatcher.core.messaging.service.MessagingConnectionService;
import net.powermatcher.core.object.IdentifiableObject;


/**
 * @author IBM
 * @version 0.9.0
 */
public class Mqttv3ConnectionDefinition extends IdentifiableObject implements MessagingConnectionDefinition {
	/**
	 * Define the broker URI (String) field.
	 */
	private String brokerURI;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #Mqttv3ConnectionDefinition(ConfigurationService)
	 */
	public Mqttv3ConnectionDefinition() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #Mqttv3ConnectionDefinition()
	 */
	public Mqttv3ConnectionDefinition(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Create connection and return the MessagingConnectionService result.
	 * 
	 * @return Results of the create connection (
	 *         <code>MessagingConnectionService</code>) value.
	 */
	@Override
	public MessagingConnectionService createConnection() {
		Mqttv3Connection connection = new Mqttv3Connection(this);
		return connection;
	}

	/**
	 * Equals with the specified obj parameter and return the boolean result.
	 * 
	 * @param obj
	 *            The obj (<code>Object</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Mqttv3ConnectionDefinition other = (Mqttv3ConnectionDefinition) obj;
		if (getClusterId() == null) {
			if (other.getClusterId() != null) {
				return false;
			}
		} else if (!getClusterId().equals(other.getClusterId())) {
			return false;
		}
		if (getId() == null) {
			if (other.getId() != null) {
				return false;
			}
		} else if (!getId().equals(other.getId())) {
			return false;
		}
		if (this.brokerURI == null) {
			if (other.brokerURI != null) {
				return false;
			}
		} else if (!this.brokerURI.equals(other.brokerURI)) {
			return false;
		}
		return true;
	}

	/**
	 * The URI to connect to the broker in the same VM or via TCP/IP.
	 * 
	 * @return Results of the get broker uri (<code>String</code>) value.
	 */
	public String getBrokerURI() {
		return this.brokerURI;
	}

	/**
	 * Each MQTT client connecting to a broker must have a unique name on the
	 * bus, of no more than 23 characters.
	 * 
	 * @return Results of the get client ID (<code>String</code>) value.
	 */
	public String getClientId() {
		String clientId = getId();
		if (clientId.length() > 19) {
			clientId = clientId.substring(0, 19);
		}
		clientId += '.' + getClusterId();
		if (clientId.length() > 23) {
			clientId = clientId.substring(0, 23);
		}
		return clientId.replace('-', '_');
	}

	/**
	 * Gets the notification clean disconnected message (String) value.
	 * 
	 * @return The notification clean disconnected message (<code>String</code>)
	 *         value.
	 */
	public String getNotificationCleanDisconnectedMessage() {
		return getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_CLEANDISCONNECTED_MESSAGE_PROPERTY,
				Mqttv3ConnectionConfiguration.NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT);
	}

	/**
	 * Gets the notification connected message (String) value.
	 * 
	 * @return The notification connected message (<code>String</code>) value.
	 */
	public String getNotificationConnectedMessage() {
		return getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_CONNECTED_MESSAGE_PROPERTY,
				Mqttv3ConnectionConfiguration.NOTIFICATION_CONNECTED_MESSAGE_DEFAULT);
	}

	/**
	 * Gets the notification disconnected message (String) value.
	 * 
	 * @return The notification disconnected message (<code>String</code>)
	 *         value.
	 */
	public String getNotificationDisconnectedMessage() {
		return getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_DISCONNECTED_MESSAGE_PROPERTY,
				Mqttv3ConnectionConfiguration.NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT);
	}

	/**
	 * Gets the notification topic (String) value.
	 * 
	 * @return The notification topic (<code>String</code>) value.
	 */
	public String getNotificationTopic() {
		String prefix = getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_TOPIC_PREFIX_PROPERTY,
				Mqttv3ConnectionConfiguration.NOTIFICATION_TOPIC_PREFIX_DEFAULT);
		return prefix + '/' + getClusterId() + '/' + getId();
	}

	/**
	 * Gets the password (char[]) value.
	 * 
	 * @return The password (<code>char[]</code>) value.
	 */
	public char[] getPassword() {
		String password = getProperty(Mqttv3ConnectionConfiguration.PASSWORD_PROPERTY, (String) null);
		return password == null ? null : password.toCharArray();
	}

	/**
	 * Gets the reconnect interval (int) value.
	 * 
	 * @return The reconnect interval (<code>int</code>) value.
	 */
	public int getReconnectInterval() {
		return getProperty(Mqttv3ConnectionConfiguration.RECONNECT_INTERVAL_PROPERTY,
				Mqttv3ConnectionConfiguration.RECONNECT_INTERVAL_DEFAULT);
	}

	/**
	 * Gets the user name (String) value.
	 * 
	 * @return The user name (<code>String</code>) value.
	 */
	public String getUserName() {
		return getProperty(Mqttv3ConnectionConfiguration.USERNAME_PROPERTY, (String) null);
	}

	/**
	 * Hash code and return the int result.
	 * 
	 * @return Results of the hash code (<code>int</code>) value.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.brokerURI == null) ? 0 : this.brokerURI.hashCode());
		result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	/**
	 * Gets the notification enabled (boolean) value.
	 * 
	 * @return The notification enabled (<code>boolean</code>) value.
	 */
	public boolean isNotificationEnabled() {
		return getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_ENABLED_PROPERTY,
				Mqttv3ConnectionConfiguration.NOTIFICATION_ENABLED_DEFAULT);
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostname = "unknown";
		}
		Map<String, Object> defaultProperties = new HashMap<String, Object>();
		defaultProperties.put(Mqttv3ConnectionConfiguration.NOTIFICATION_HOST_NAME_PROPERTY,
				configuration.getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_HOST_NAME_PROPERTY, hostname));
		defaultProperties.put(Mqttv3ConnectionConfiguration.NOTIFICATION_COMPONENT_NAME_PROPERTY,
				configuration.getProperty(Mqttv3ConnectionConfiguration.NOTIFICATION_COMPONENT_NAME_PROPERTY, Mqttv3ConnectionConfiguration.NOTIFICATION_COMPONENT_NAME_DEFAULT));
		ConfigurationService augmentedConfiguration = new BaseConfiguration(configuration, defaultProperties);
		super.setConfiguration(augmentedConfiguration);
		this.brokerURI = getProperty(Mqttv3ConnectionConfiguration.BROKER_URI_PROPERTY,
				Mqttv3ConnectionConfiguration.BROKER_DEFAULT_URI);
	}

}
