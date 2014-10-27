package net.powermatcher.core.messaging.mqttv3;


import java.util.Map;
import java.util.WeakHashMap;

import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.configurable.service.Configurable;
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
 * @see Mqttv3Connection
 * @see MessagingConnectionService
 */
public class Mqttv3ConnectionFactory implements TargetAdapterFactoryService<MessagingConnectorService> {

	/**
	 * Define the connection cache (Map) field.
	 */
	private Map<MessagingConnectionDefinition, MessagingConnectionService> connectionCache = new WeakHashMap<MessagingConnectionDefinition, MessagingConnectionService>();

	public Mqttv3ConnectionFactory() {
	}

	@Override
	public MessagingConnectionService createAdapter(Configurable configuration, MessagingConnectorService connector,
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
	public MessagingConnectionService createAdapter(final Configurable configuration,
			final MessagingConnectorService messagingConnector) {
		MessagingConnectionDefinition connectionDefinition = new Mqttv3ConnectionDefinition(configuration);
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
		return Mqttv3Connection.class.getSimpleName();
	}
	
}
