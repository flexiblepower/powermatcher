package net.powermatcher.core.messaging.service;


import net.powermatcher.core.adapter.service.AdapterService;
import net.powermatcher.core.messaging.framework.Topic;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface MessagingConnectionService extends AdapterService {

	/**
	 * Add a client to a (shared) messaging connection.
	 * 
	 * @param connector
	 *            The client of the messaging connection
	 */
	public void addConnector(MessagingConnectorService connector);

	/**
	 * Get the connection definition from which this connection was created.
	 * 
	 * @return the connection definition for this connection.
	 */
	public MessagingConnectionDefinition getConnectionDefinition();

	/**
	 * Is connection currently bound to one or more adapters.
	 * 
	 * @return is bound.
	 */
	public boolean isBound();

	/**
	 * Publish with the specified topic parameter.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 */
	public void publish(final Topic topic);

	/**
	 * Publish with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	public void publish(final Topic topic, final byte[] data);

	/**
	 * Publish with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 */
	public void publish(final Topic topic, final String data);

	/**
	 * Remove a client from a (shared) messaging connection.
	 * 
	 * @param connector
	 *            The client of the messaging connection
	 */
	public void removeConnector(MessagingConnectorService connector);

}
