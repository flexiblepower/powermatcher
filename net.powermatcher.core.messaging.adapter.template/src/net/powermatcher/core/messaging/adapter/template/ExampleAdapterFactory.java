package net.powermatcher.core.messaging.adapter.template;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.template.service.ExampleConnectorService;
import net.powermatcher.core.agent.template.service.ExampleControlService;
import net.powermatcher.core.agent.template.service.ExampleNotificationService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * Adapter factory for ImbalanceAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ExampleConnectorService
 * @see ExampleAdapter
 * @see ExampleNotificationService
 * @see ExampleControlService
 */
public class ExampleAdapterFactory implements TargetAdapterFactoryService<ExampleConnectorService> {

	public ExampleAdapterFactory() {
	}

	@Override
	public ExampleAdapter createAdapter(ConfigurationService configuration, ExampleConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create adapter with the specified configuration and for the
	 * given connector and return the newly created connector.
	 * When the connector is created it is ready to bind to the connector.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param connectorService
	 *            The connector of the agent (<code>ExampleConnectorService</code>)
	 *            to bind to.
	 * @return The newly created (<code>ExampleAdapter</code>)
	 *         adapter.
	 */
	@Override
	public ExampleAdapter createAdapter(final ConfigurationService configuration,
			final ExampleConnectorService connectorService) {
		ExampleAdapter agentAdapter = new ExampleAdapter(configuration);
		agentAdapter.setConnector(connectorService);
		return agentAdapter;
	}

	@Override
	public String getAdapterName() {
		return ExampleAdapter.class.getSimpleName();
	}
	
}
