package net.powermatcher.expeditor.broker.manager.config;


import com.ibm.micro.admin.BrokerDefinition;
import com.ibm.micro.admin.PersistenceDefinition;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface BrokerManagerConfiguration {
	/**
	 * Define the broker name property (String) constant.
	 */
	public static final String BROKER_NAME_PROPERTY = "broker.name";
	/**
	 * Define the broker name default (String) constant.
	 */
	public static final String BROKER_NAME_DEFAULT = "MicroBroker";
	/**
	 * Define the broker dir property (String) constant.
	 */
	public static final String BROKER_DIR_PROPERTY = "microbroker.dir";
	/**
	 * Define the broker dir default str (String) constant.
	 */
	public static final String BROKER_DIR_DEFAULT_STR = ".";
	/**
	 * Define the default dir (String) constant.
	 */
	public static final String DEFAULT_DIR = System.getProperty(BrokerDefinition.BROKER_DATA_DIR, BROKER_DIR_DEFAULT_STR);
	/**
	 * Define the max message size property (String) constant.
	 */
	public static final String MAX_MESSAGE_SIZE_PROPERTY = "microbroker.maxMessageSize";
	/**
	 * Define the max message size default str (String) constant.
	 */
	public static final String MAX_MESSAGE_SIZE_DEFAULT_STR = "100";
	/**
	 * Define the max message size default (int) constant.
	 */
	public static final int MAX_MESSAGE_SIZE_DEFAULT = Integer.parseInt(MAX_MESSAGE_SIZE_DEFAULT_STR);
	/**
	 * Define the max number of clients property (String) constant.
	 */
	public static final String MAX_NUMBER_OF_CLIENTS_PROPERTY = "microbroker.maxNumberOfClients";
	/**
	 * Define the max number of clients default str (String) constant.
	 */
	public static final String MAX_NUMBER_OF_CLIENTS_DEFAULT_STR = "-1";
	/**
	 * Define the max number of clients default (int) constant.
	 */
	public static final int MAX_NUMBER_OF_CLIENTS_DEFAULT = Integer.parseInt(MAX_NUMBER_OF_CLIENTS_DEFAULT_STR);
	/**
	 * Define the persistence property (String) constant.
	 */
	public static final String PERSISTENCE_PROPERTY = "microbroker.persistence";
	/**
	 * Define the persistence default (int) constant.
	 */
	public static final int PERSISTENCE_DEFAULT = PersistenceDefinition.NO_PERSISTENCE;
	/**
	 * Define the persistence default str (String) constant.
	 */
	public static final String PERSISTENCE_DEFAULT_STR = "0";
	/**
	 * Define the port property (String) constant.
	 */
	public static final String PORT_PROPERTY = "microbroker.port";
	/**
	 * Define the port default str (String) constant.
	 */
	public static final String PORT_DEFAULT_STR = "1883";
	/**
	 * Define the port default (int) constant.
	 */
	public static final int PORT_DEFAULT = Integer.parseInt(PORT_DEFAULT_STR);
	/**
	 * Define the queue size property (String) constant.
	 */
	public static final String QUEUE_SIZE_PROPERTY = "microbroker.queueSize";
	/**
	 * Define the queue size default str (String) constant.
	 */
	public static final String QUEUE_SIZE_DEFAULT_STR = "-1";
	/**
	 * Define the queue size default (int) constant.
	 */
	public static final int QUEUE_SIZE_DEFAULT = Integer.parseInt(QUEUE_SIZE_DEFAULT_STR);
	/**
	 * Define the number of logs to keep property (String) constant.
	 */
	public static final String NUMBER_OF_LOGS_TO_KEEP_PROPERTY = "microbroker.numberOfLogFilesToKeep";
	/**
	 * Define the number of logs to keep default str (String) constant.
	 */
	public static final String NUMBER_OF_LOGS_TO_KEEP_DEFAULT_STR = "10";
	/**
	 * Define the number of logs to keep default (int) constant.
	 */
	public static final int NUMBER_OF_LOGS_TO_KEEP_DEFAULT = Integer.parseInt(NUMBER_OF_LOGS_TO_KEEP_DEFAULT_STR);
	/**
	 * Define the broker wait timeout property (String) constant.
	 */
	public static final String BROKER_WAIT_TIMEOUT_PROPERTY = "microbroker.waitTimeout";
	/**
	 * Define the broker wait timeout default str (String) constant.
	 */
	public static final String BROKER_WAIT_TIMEOUT_DEFAULT_STR = "10000";
	/**
	 * Define the broker wait timeout default (long) constant.
	 */
	public static final long BROKER_WAIT_TIMEOUT_DEFAULT = Long.parseLong(BROKER_WAIT_TIMEOUT_DEFAULT_STR);
	/**
	 * Define the security enabled property (String) constant.
	 */
	public static final String SECURITY_ENABLED_PROPERTY = "microbroker.security.enabled";
	/**
	 * Define the security enabled default str (String) constant.
	 */
	public static final String SECURITY_ENABLED_DEFAULT_STR = "true";
	/**
	 * Define the security enabled default (boolean) constant.
	 */
	public static final boolean SECURITY_ENABLED_DEFAULT = Boolean.parseBoolean(SECURITY_ENABLED_DEFAULT_STR);

}
