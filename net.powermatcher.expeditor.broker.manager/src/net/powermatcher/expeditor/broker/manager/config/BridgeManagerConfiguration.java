package net.powermatcher.expeditor.broker.manager.config;


import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import com.ibm.micro.admin.bridge.NotificationDefinition;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface BridgeManagerConfiguration extends ConnectableObjectConfiguration {
	/**
	 * Define the pipe name property (String) constant.
	 */
	public static final String PIPE_NAME_PROPERTY = "pipe.name";
	/**
	 * Define the host property (String) constant.
	 */
	public static final String HOST_PROPERTY = "remote.host";
	/**
	 * Define the port property (String) constant.
	 */
	public static final String PORT_PROPERTY = "remote.port";
	/**
	 * Define the port default str (String) constant.
	 */
	public static final String PORT_DEFAULT_STR = "1883";
	/**
	 * Define the port default (int) constant.
	 */
	public static final int PORT_DEFAULT = Integer.parseInt(PORT_DEFAULT_STR);
	/**
	 * Define the username property (String) constant.
	 */
	public static final String USERNAME_PROPERTY = "remote.username";
	/**
	 * Define the password property (String) constant.
	 */
	public static final String PASSWORD_PROPERTY = "remote.password";
	/**
	 * Define the secure property (String) constant.
	 */
	public static final String SECURE_PROPERTY = "remote.secure";
	/**
	 * Define the secure default str (String) constant.
	 */
	public static final String SECURE_DEFAULT_STR = "false";
	/**
	 * Define the secure default (boolean) constant.
	 */
	public static final boolean SECURE_DEFAULT = Boolean.parseBoolean(SECURE_DEFAULT_STR);
	/**
	 * Define the keep alive secs property (String) constant.
	 */
	public static final String KEEP_ALIVE_SECS_PROPERTY = "keep.alive.secs";
	/**
	 * Define the keep alive secs default str (String) constant.
	 */
	public static final String KEEP_ALIVE_SECS_DEFAULT_STR = "60";
	/**
	 * Define the keep alive secs default (short) constant.
	 */
	public static final short KEEP_ALIVE_SECS_DEFAULT = Short.parseShort(KEEP_ALIVE_SECS_DEFAULT_STR);
	/**
	 * Define the out topics property (String) constant.
	 */
	public static final String OUT_TOPICS_PROPERTY = "out.topics";
	/**
	 * Define the out target topic property (String) constant.
	 */
	public static final String OUT_TARGET_TOPIC_PROPERTY = "out.target.topic";
	/**
	 * Define the out topics default (String[]) constant.
	 */
	public static final String[] OUT_TOPICS_DEFAULT = { "PowerMatcher/${cluster.id}/+/+/UpdateBid", "PowerMatcher/${cluster.id}/+/+/Log", "Telemetry/#", "Status/Agent" };
	/**
	 * Define the out topics default str (String) constant.
	 */
	public static final String OUT_TOPICS_DEFAULT_STR = "PowerMatcher/${cluster.id}/+/+/UpdateBid\\,PowerMatcher/${cluster.id}/+/+/Log\\,Telemetry/#\\,Status/Agent";
	/**
	 * Define the in topics property (String) constant.
	 */
	public static final String IN_TOPICS_PROPERTY = "in.topics";
	/**
	 * Define the in target topic property (String) constant.
	 */
	public static final String IN_TARGET_TOPIC_PROPERTY = "in.target.topic";
	/**
	 * Define the in topics default (String[]) constant.
	 */
	public static final String[] IN_TOPICS_DEFAULT = { "PowerMatcher/${cluster.id}/+/UpdatePriceInfo", "Configuration/Update/+" };
	/**
	 * Define the in topics default str (String) constant.
	 */
	public static final String IN_TOPICS_DEFAULT_STR = "PowerMatcher/${cluster.id}/+/UpdatePriceInfo,Configuration/Update/+";
	/**
	 * Define the LWAT enabled property (String) constant.
	 */
	public static final String NOTIFICATION_ENABLED_PROPERTY = "notification.enabled";
	/**
	 * Define the LWAT enabled default (String) constant.
	 */
	public static final String NOTIFICATION_ENABLED_DEFAULT_STR = "true";
	/**
	 * Define the notification host name property (String) constant.
	 */
	public static final String NOTIFICATION_HOST_NAME_PROPERTY = "host.name";
	/**
	 * Define the notification component name property (String) constant.
	 */
	public static final String NOTIFICATION_COMPONENT_NAME_PROPERTY = "component.name";
	/**
	 * Define the LWAT enabled default (boolean) constant.
	 */
	public static final boolean NOTIFICATION_ENABLED_DEFAULT = Boolean.parseBoolean(NOTIFICATION_ENABLED_DEFAULT_STR);
	/**
	 * Define the LWAT topic property (String) constant.
	 */
	public static final String NOTIFICATION_TOPIC_PREFIX_PROPERTY = "notification.topic";
	/**
	 * Define the LWAT topic default (String) constant.
	 */
	public static final String NOTIFICATION_TOPIC_PREFIX_DEFAULT = NotificationDefinition.DEFAULT_TOPIC;
	/**
	 * Define the LWAT connected message property (String) constant.
	 */
	public static final String NOTIFICATION_CONNECTED_MESSAGE_PROPERTY = "notification.connected.message";
	/**
	 * Define the LWAT connected message default (String) constant.
	 */
	public static final String NOTIFICATION_CONNECTED_MESSAGE_DEFAULT = "CONNECTED,I,${cluster.id},${id},${pipe.name},${component.name},${host.name}";
	/**
	 * Define the LWAT connected message default (String) constant, with escaped characters.
	 */
	public static final String NOTIFICATION_CONNECTED_MESSAGE_DEFAULT_STR = "CONNECTED\\,I\\,${cluster.id}\\,${id}\\,${pipe.name}\\,${component.name}\\,${host.name}";
	/**
	 * Define the LWAT connected message property (String) constant.
	 */
	public static final String NOTIFICATION_DISCONNECTED_MESSAGE_PROPERTY = "notification.disconnected.message";
	/**
	 * Define the LWAT connected message default (String) constant.
	 */
	public static final String NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT = "DISCONNECTED,W,${cluster.id},${id},${pipe.name},${component.name},${host.name}";
	/**
	 * Define the LWAT connected message default (String) constant, with escaped characters.
	 */
	public static final String NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT_STR = "DISCONNECTED\\,W\\,${cluster.id}\\,${id}\\,${pipe.name}\\,${component.name}\\,${host.name}";
	/**
	 * Define the LWAT connected message property (String) constant.
	 */
	public static final String NOTIFICATION_CLEANDISCONNECTED_MESSAGE_PROPERTY = "notification.cleandisconnected.message";
	/**
	 * Define the LWAT connected message default (String) constant.
	 */
	public static final String NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT = "CLEANDISCONNECTED,I,${cluster.id},${id},${pipe.name},${component.name},${host.name}";
	/**
	 * Define the LWAT connected message default (String) constant, with escaped characters.
	 */
	public static final String NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT_STR = "CLEANDISCONNECTED\\,I\\,${cluster.id}\\,${id}\\,${pipe.name}\\,${component.name}\\,${host.name}";

}
