package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.SourceAdapterFactoryService;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * Adapter factory for AgentProtocolAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see AgentConnectorService
 * @see AgentProtocolAdapter
 * @see AgentService
 */
public class AgentProtocolAdapterFactory implements SourceAdapterFactoryService<AgentConnectorService> {

	public AgentProtocolAdapterFactory() {
	}

	@Override
	public AgentProtocolAdapter createAdapter(ConfigurationService configuration, AgentConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String parentMatcherId = getTargetConnectorIds(connector)[adapterIndex];
		return createAdapter(configuration, connector, parentMatcherId);
	}

	/**
	 * Create adapter with the specified configuration and agent connector
	 * parameters and return the AgentProtocolAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param agentConnector
	 *            The agent connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>AgentProtocolAdapter</code>)
	 *         value.
	 */
	@Override
	public AgentProtocolAdapter createAdapter(final ConfigurationService configuration,
			final AgentConnectorService agentConnector, final String parentMatcherId) {
		AgentProtocolAdapter agentAdapter = new AgentProtocolAdapter(configuration);
		agentAdapter.setAgentConnector(agentConnector);
		agentAdapter.setParentMatcherId(parentMatcherId);
		return agentAdapter;
	}

	/**
	 * Get the parent matcher ids configured for the agent.
	 * @param connector The agent to get the parent matcher id from
	 * @return The parent matcher id configured for the agent.
	 */
	@Override
	public String[] getTargetConnectorIds(final AgentConnectorService connector) {
		return connector.getConfiguration().getProperty(AgentConfiguration.PARENT_MATCHER_ID_PROPERTY, AgentConfiguration.PARENT_MATCHER_ID_DEFAULT);
	}

	@Override
	public String getAdapterName() {
		return AgentProtocolAdapter.class.getSimpleName();
	}
	
}
