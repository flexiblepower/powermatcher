package net.powermatcher.expeditor.messaging.mqttv5.config;

import net.powermatcher.core.adapter.config.AdapterConfiguration;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface Mqttv5ConnectionConfiguration extends AdapterConfiguration {
	/**
	 * Define the broker uri property (String) constant.
	 */
	public static final String BROKER_URI_PROPERTY = "broker.uri";
	/**
	 * Define the broker default uri (String) constant.
	 */
	public static final String BROKER_DEFAULT_URI = "local://MicroBroker";
	/**
	 * Define the username property (String) constant.
	 */
	public static final String USERNAME_PROPERTY = "broker.username";
	/**
	 * Define the password property (String) constant.
	 */
	public static final String PASSWORD_PROPERTY = "broker.password";
	/**
	 * Define the reconnect interval property (String) constant.
	 */
	public static final String RECONNECT_INTERVAL_PROPERTY = "reconnect.interval";
	/**
	 * Define the reconnect interval default (int) constant.
	 */
	public static final int RECONNECT_INTERVAL_DEFAULT = 10;
	/**
	 * Define the reconnect interval default str (String) constant.
	 */
	public static final String RECONNECT_INTERVAL_DEFAULT_STR = "10";
	/**
	 * Define the LWAT enabled property (String) constant.
	 */
	public static final String NOTIFICATION_ENABLED_PROPERTY = "notification.enabled";
	/**
	 * Define the LWAT enabled default (String) constant.
	 */
	public static final String NOTIFICATION_ENABLED_DEFAULT_STR = "false";
	/**
	 * Define the LWAT enabled default (boolean) constant.
	 */
	public static final boolean NOTIFICATION_ENABLED_DEFAULT = Boolean.parseBoolean(NOTIFICATION_ENABLED_DEFAULT_STR);
	/**
	 * Define the notification host name property (String) constant.
	 */
	public static final String NOTIFICATION_HOST_NAME_PROPERTY = "host.name";
	/**
	 * Define the notification component name property (String) constant.
	 */
	public static final String NOTIFICATION_COMPONENT_NAME_PROPERTY = "component.name";
	/**
	 * Define the notification component name default (String) constant.
	 */
	public static final String NOTIFICATION_COMPONENT_NAME_DEFAULT = "net.powermatcher.expeditor.messaging.mqttv5.Mqttv5Connection";
	/**
	 * Define the LWAT topic property (String) constant.
	 */
	public static final String NOTIFICATION_TOPIC_PREFIX_PROPERTY = "notification.topic";
	/**
	 * Define the LWAT topic default (String) constant.
	 */
	public static final String NOTIFICATION_TOPIC_PREFIX_DEFAULT = "Status/Agent";
	/**
	 * Define the LWAT connected message property (String) constant.
	 */
	public static final String NOTIFICATION_CONNECTED_MESSAGE_PROPERTY = "notification.connected.message";
	/**
	 * Define the LWAT connected message default (String) constant.
	 */
	public static final String NOTIFICATION_CONNECTED_MESSAGE_DEFAULT = "CONNECTED,I,${cluster.id},${id},${component.name},${component.name},${host.name}";
	/**
	 * Define the LWAT connected message default (String) constant, with escaped characters.
	 */
	public static final String NOTIFICATION_CONNECTED_MESSAGE_DEFAULT_STR = "CONNECTED\\,I\\,${cluster.id}\\,${id}\\,${component.name}\\,${component.name}\\,${host.name}";
	/**
	 * Define the LWAT connected message property (String) constant.
	 */
	public static final String NOTIFICATION_DISCONNECTED_MESSAGE_PROPERTY = "notification.disconnected.message";
	/**
	 * Define the LWAT connected message default (String) constant.
	 */
	public static final String NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT = "DISCONNECTED,I,${cluster.id},${id},${component.name},${component.name},${host.name}";
	/**
	 * Define the LWAT connected message default (String) constant, with escaped characters.
	 */
	public static final String NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT_STR = "DISCONNECTED\\,I\\,${cluster.id}\\,${id}\\,${component.name}\\,${component.name}\\,${host.name}";
	/**
	 * Define the LWAT connected message property (String) constant.
	 */
	public static final String NOTIFICATION_CLEANDISCONNECTED_MESSAGE_PROPERTY = "notification.cleandisconnected.message";
	/**
	 * Define the LWAT connected message default (String) constant.
	 */
	public static final String NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT = "CLEANDISCONNECTED,W,${cluster.id},${id},${component.name},${component.name},${host.name}";
	/**
	 * Define the LWAT connected message default (String) constant, with escaped characters.
	 */
	public static final String NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT_STR = "CLEANDISCONNECTED\\,W\\,${cluster.id}\\,${id}\\,${component.name}\\,${component.name}\\,${host.name}";

	/**
	 * Broker_password and return the String result.
	 * 
	 * @return Results of the broker_password (<code>String</code>) value.
	 */
	public String broker_password();

	/**
	 * Broker_uri and return the String result.
	 * 
	 * @return Results of the broker_uri (<code>String</code>) value.
	 */
	public String broker_uri();

	/**
	 * Broker_username and return the String result.
	 * 
	 * @return Results of the broker_username (<code>String</code>) value.
	 */
	public String broker_username();

	/**
	 * @return notification.enabled property.
	 */
	public boolean notification_enabled();

	/**
	 * @return host.name property.
	 */
	public String host_name();

	/**
	 * @return component.name property.
	 */
	public String component_name();

	/**
	 * @return notification.topic property.
	 */
	public String notification_topic();

	/**
	 * @return notification.connected.message property.
	 */
	public String notification_connected_message();

	/**
	 * @return notification.cleandisconnected.message property.
	 */
	public String notification_cleandisconnected_message();

	/**
	 * @return notification.disconnected.message property.
	 */
	public String notification_disconnected_message();

	/**
	 * Reconnect_interval and return the int result.
	 * 
	 * @return Results of the reconnect_interval (<code>int</code>) value.
	 */
	public int reconnect_interval();

}
