package net.powermatcher.expeditor.messaging.mqttv5.component;


import net.powermatcher.expeditor.messaging.mqttv5.config.Mqttv5ConnectionConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a Mqttv5ConnectionFactoryComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a Mqttv5ConnectionFactoryComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see Mqttv5ConnectionFactoryComponent
 */
@OCD(name = Mqttv5ConnectionFactoryComponentConfiguration.CONFIGURATION_NAME, description = Mqttv5ConnectionFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface Mqttv5ConnectionFactoryComponentConfiguration extends Mqttv5ConnectionConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "MQTTv5 Messaging Connection Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Factory for (shared) MQTTv5 connections for messaging adapters";

	/**
	 * Define the messaging adapter factory default id (String) constant.
	 */
	public static final String MESSAGING_ADAPTER_FACTORY_DEFAULT = "mqttv5ConnectionFactory";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = false, deflt = BROKER_DEFAULT_URI)
	public String broker_uri();

	@Override
	@Meta.AD(required = false)
	public String broker_username();

	@Override
	@Meta.AD(required = false)
	public String broker_password();

	@Override
	@Meta.AD(required = false, deflt = NOTIFICATION_ENABLED_DEFAULT_STR)
	public boolean notification_enabled();

	@Override
	@Meta.AD(required = false)
	public String host_name();

	@Override
	@Meta.AD(required = false)
	public String component_name();

	@Override
	@Meta.AD(required = false, deflt = NOTIFICATION_TOPIC_PREFIX_DEFAULT)
	public String notification_topic();

	@Override
	@Meta.AD(required = false, deflt = NOTIFICATION_CONNECTED_MESSAGE_DEFAULT_STR)
	public String notification_connected_message();

	@Override
	@Meta.AD(required = false, deflt = NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT_STR)
	public String notification_cleandisconnected_message();

	@Override
	@Meta.AD(required = false, deflt = NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT_STR)
	public String notification_disconnected_message();

	@Override
	@Meta.AD(required = false, deflt = RECONNECT_INTERVAL_DEFAULT_STR)
	public int reconnect_interval();

	@Override
	@Meta.AD(required = false, deflt = TIME_ADAPTER_FACTORY_DEFAULT, description = TIME_ADAPTER_FACTORY_DESCRIPTION)
	public String time_adapter_factory();

	@Override
	@Meta.AD(required = false, deflt = SCHEDULER_ADAPTER_FACTORY_DEFAULT, description = SCHEDULER_ADAPTER_FACTORY_DESCRIPTION)
	public String scheduler_adapter_factory();
	
}
