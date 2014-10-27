package net.powermatcher.core.agent.marketbasis.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.framework.service.ChildConnectable;
import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * Adapter factory for MarketBasisAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ChildConnectable
 * @see MarketBasisAdapter
 * @see DownMessagable
 */
public class MarketBasisAdapterFactory implements TargetAdapterFactoryService<ChildConnectable> {

	public MarketBasisAdapterFactory() {
	}

	@Override
	public MarketBasisAdapter createAdapter(Configurable configuration, ChildConnectable connector,
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
	public MarketBasisAdapter createAdapter(final Configurable configuration, final ChildConnectable agentConnector) {
		MarketBasisAdapter adapter = new MarketBasisAdapter(configuration);
		adapter.setAgentConnector(agentConnector);
		return adapter;
	}

	@Override
	public String getAdapterName() {
		return MarketBasisAdapter.class.getSimpleName();
	}
	
}
