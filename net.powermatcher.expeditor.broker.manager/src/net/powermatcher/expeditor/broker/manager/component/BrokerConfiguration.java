package net.powermatcher.expeditor.broker.manager.component;


import net.powermatcher.expeditor.broker.manager.config.BrokerManagerConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "MicroBroker MQTT Broker")
public interface BrokerConfiguration extends BrokerManagerConfiguration {

	/**
	 * @return broker.name property.
	 */
	@Meta.AD(required = false, deflt = BROKER_NAME_DEFAULT)
	public String broker_name();

	/**
	 * @return microbroker.dir property.
	 */
	@Meta.AD(required = false, deflt = BROKER_DIR_DEFAULT_STR)
	public String microbroker_dir();

	/**
	 * @return microbroker.maxMessageSize property.
	 */
	@Meta.AD(required = false, deflt = MAX_MESSAGE_SIZE_DEFAULT_STR)
	public int microbroker_maxMessageSize();

	/**
	 * @return microbroker.maxNumberOfClients property.
	 */
	@Meta.AD(required = false, deflt = MAX_NUMBER_OF_CLIENTS_DEFAULT_STR)
	public int microbroker_maxNumberOfClients();

	/**
	 * @return microbroker.numberOfLogFilesToKeep property.
	 */
	@Meta.AD(required = false, deflt = NUMBER_OF_LOGS_TO_KEEP_DEFAULT_STR)
	public int microbroker_numberOfLogFilesToKeep();

	/**
	 * @return microbroker.persistence property.
	 */
	@Meta.AD(required = false, deflt = PERSISTENCE_DEFAULT_STR)
	public int microbroker_persistence();

	/**
	 * @return microbroker.port property.
	 */
	@Meta.AD(required = false, deflt = PORT_DEFAULT_STR)
	public int microbroker_port();

	/**
	 * @return microbroker.queueSize property.
	 */
	@Meta.AD(required = false, deflt = QUEUE_SIZE_DEFAULT_STR)
	public int microbroker_queueSize();

	/**
	 * @return microbroker.waitTimeout property.
	 */
	@Meta.AD(required = false, deflt = BROKER_WAIT_TIMEOUT_DEFAULT_STR)
	public long microbroker_waitTimeout();

	/*
	 * Security is enabled by default if JAAS is supported, and anonymous
	 * connections are permitted by default. Local authentication requires
	 * customization of the default acl file that is generated when a new broker
	 * is started and requires a custom JAAS authentication module (the default
	 * MicroBroker JAAS module must be configured somehow, but this does not
	 * appear to be documented.
	 * 
	 * @Meta.AD(required=false,deflt=DEFAULT_SECURITY_ENABLED_STR) public
	 * boolean microbroker_security_enabled();
	 */

}
