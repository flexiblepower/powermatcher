package net.powermatcher.core.agent.marketbasis.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * Adapter factory for MarketBasisAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see AgentConnectorService
 * @see MarketBasisAdapter
 * @see AgentService
 */
public class MarketBasisAdapterFactory implements TargetAdapterFactoryService<AgentConnectorService> {

	public MarketBasisAdapterFactory() {
	}

	@Override
	public MarketBasisAdapter createAdapter(ConfigurationService configuration, AgentConnectorService connector,
			ConnectorLocaterService connectorLocater, int factoryIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create adapter with the specified configuration and agent connector
	 * parameters and return the Adapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param agentConnector
	 *            The agent connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>MarketBasisAdapter</code>) value.
	 */
	@Override
	public MarketBasisAdapter createAdapter(final ConfigurationService configuration, final AgentConnectorService agentConnector) {
		MarketBasisAdapter adapter = new MarketBasisAdapter(configuration);
		adapter.setAgentConnector(agentConnector);
		return adapter;
	}

	@Override
	public String getAdapterName() {
		return MarketBasisAdapter.class.getSimpleName();
	}
	
}
