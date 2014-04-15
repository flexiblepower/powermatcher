package net.powermatcher.expeditor.messaging.mqttv5;


import java.util.HashMap;
import java.util.Map;

import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.service.MessagingConnectionDefinition;
import net.powermatcher.core.messaging.service.MessagingConnectionService;
import net.powermatcher.core.messaging.service.MessagingConnectorService;


/**
 * Messaging connection factory for Mqttv3Connection.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see MessagingConnectorService
 * @see Mqttv5Connection
 * @see MessagingConnectionService
 */
public class Mqttv5ConnectionFactory implements TargetAdapterFactoryService<MessagingConnectorService> {

	/**
	 * Define the connection cache (Map) field.
	 */
	private Map<MessagingConnectionDefinition, MessagingConnectionService> connectionCache = new HashMap<MessagingConnectionDefinition, MessagingConnectionService>();

	public Mqttv5ConnectionFactory() {
	}

	@Override
	public MessagingConnectionService createAdapter(ConfigurationService configuration, MessagingConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create connection with the specified configuration and agent connector
	 * parameters and return the Adapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param messagingConnector
	 *            The messaging connector (
	 *            <code>MessagingConnectorService</code>) parameter.
	 * @return Results of the create connection (
	 *         <code>MessagingConnection</code>) value.
	 */
	@Override
	public MessagingConnectionService createAdapter(final ConfigurationService configuration,
			final MessagingConnectorService messagingConnector) {
		MessagingConnectionDefinition connectionDefinition = new Mqttv5ConnectionDefinition(configuration);
		synchronized (connectionCache) {
			MessagingConnectionService connection = connectionCache.get(connectionDefinition);
			if (connection == null) {
				connection = connectionDefinition.createConnection();
				connectionCache.put(connectionDefinition, connection);
			}
			connection.addConnector(messagingConnector);
			return connection;
		}
	}

	@Override
	public String getAdapterName() {
		return Mqttv5Connection.class.getSimpleName();
	}
	
}
