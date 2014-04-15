package net.powermatcher.expeditor.broker.manager.component;


import net.powermatcher.expeditor.broker.manager.config.BridgeManagerConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "MicroBroker MQTT Bridge Pipe")
public interface PipeConfiguration extends BridgeManagerConfiguration {

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	/**
	 * @return pipe.name property.
	 */
	@Meta.AD(required = false)
	public String pipe_name();

	/**
	 * @return in.topics property.
	 */
	@Meta.AD(required = false, deflt = IN_TOPICS_DEFAULT_STR)
	public String in_topics();

	/**
	 * @return in.target.topic property.
	 */
	@Meta.AD(required = false)
	public String in_target_topic();

	/**
	 * @return out.topics property.
	 */
	@Meta.AD(required = false, deflt = OUT_TOPICS_DEFAULT_STR)
	public String out_topics();

	/**
	 * @return out.target.topic property.
	 */
	@Meta.AD(required = false)
	public String out_target_topic();

	/**
	 * @return keep.alive.secs property.
	 */
	@Meta.AD(required = false, deflt = KEEP_ALIVE_SECS_DEFAULT_STR)
	public short keep_alive_secs();

	/**
	 * @return remote.host property.
	 */
	@Meta.AD(required = true)
	public String remote_host();

	/**
	 * @return remote.port property.
	 */
	@Meta.AD(required = false, deflt = PORT_DEFAULT_STR)
	public int remote_port();

	/**
	 * @return remote.secure property.
	 */
	@Meta.AD(required = false, deflt = SECURE_DEFAULT_STR)
	public boolean remote_secure();

	/**
	 * @return remote.username property.
	 */
	@Meta.AD(required = false)
	public String remote_username();

	/**
	 * @return remote.password property.
	 */
	@Meta.AD(required = false)
	public String remote_password();

	/**
	 * @return notification.enabled property.
	 */
	@Meta.AD(required = false, deflt = NOTIFICATION_ENABLED_DEFAULT_STR)
	public boolean notification_enabled();

	/**
	 * @return host.name property.
	 */
	@Meta.AD(required = false)
	public String host_name();

	/**
	 * @return component.name property.
	 */
	@Meta.AD(required = false)
	public String component_name();

	/**
	 * @return notification.topic property.
	 */
	@Meta.AD(required = false, deflt = NOTIFICATION_TOPIC_PREFIX_DEFAULT)
	public String notification_topic();

	/**
	 * @return notification.connected.message property.
	 */
	@Meta.AD(required = false, deflt = NOTIFICATION_CONNECTED_MESSAGE_DEFAULT_STR)
	public String notification_connected_message();

	/**
	 * @return notification.cleandisconnected.message property.
	 */
	@Meta.AD(required = false, deflt = NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT_STR)
	public String notification_cleandisconnected_message();

	/**
	 * @return notification.disconnected.message property.
	 */
	@Meta.AD(required = false, deflt = NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT_STR)
	public String notification_disconnected_message();

	@Override
	@Meta.AD(required = false, deflt = TIME_ADAPTER_FACTORY_DEFAULT, description = TIME_ADAPTER_FACTORY_DESCRIPTION)
	public String time_adapter_factory();
	
}
