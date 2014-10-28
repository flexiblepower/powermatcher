package net.powermatcher.core.messaging.service;

import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.framework.Topic;

/**
 * The MessagingConnectorService defines the interface to connect a component
 * that provides message reception services to another object or component. The interface
 * defines the messaging services available for the connector users. The interface contains
 * method definitions for subscribing to message topics and handling the arrival of
 * messages on the subscribed topics.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface MessagingConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "messaging.adapter.factory";
	
	/**
	 * Binding callback event for the messaging connection.
	 * 
	 * @param connection
	 *            The messaging connection that has now become active.
	 * @throws Exception
	 */
	public void binding(MessagingConnectionService connection) throws Exception;

	/**
	 * Gets the configuration (ConfigurationService) value.
	 * 
	 * @return The configuration (<code>ConfigurationService</code>) value.
	 */
	public ConfigurationService getConfiguration();

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	public Topic[] getSubscriptions();

	/**
	 * Handle message arrived with the specified topic parameter.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 */
	public boolean handleMessageArrived(final Topic topic);

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 */
	public boolean handleMessageArrived(final Topic topic, final byte[] data);

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 */
	public boolean handleMessageArrived(final Topic topic, final String data);

	/**
	 * Unbinding callback event for the messaging connection.
	 * 
	 * @param connection
	 *            The messaging connection that has now become inactive.
	 */
	public void unbinding(MessagingConnectionService connection);

}
