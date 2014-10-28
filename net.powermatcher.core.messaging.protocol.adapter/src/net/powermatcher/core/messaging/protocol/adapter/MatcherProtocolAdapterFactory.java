package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.framework.service.MatcherConnectorService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * Adapter factory for MatcherProtocolAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see MatcherConnectorService
 * @see MatcherProtocolAdapter
 * @see MatcherService
 */
public class MatcherProtocolAdapterFactory implements TargetAdapterFactoryService<MatcherConnectorService> {

	public MatcherProtocolAdapterFactory() {
	}

	@Override
	public MatcherProtocolAdapter createAdapter(ConfigurationService configuration, MatcherConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create adapter with the specified configuration and matcher connector
	 * parameters and return the MatcherProtocolAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param matcherConnector
	 *            The matcher connector (<code>MatcherConnectorService</code>)
	 *            parameter.
	 * @return Results of the create adapter (
	 *         <code>MatcherProtocolAdapter</code>) value.
	 */
	@Override
	public MatcherProtocolAdapter createAdapter(final ConfigurationService configuration,
			final MatcherConnectorService matcherConnector) {
		MatcherProtocolAdapter matcherAdapter = new MatcherProtocolAdapter(configuration);
		matcherAdapter.setMatcherConnector(matcherConnector);
		return matcherAdapter;
	}

	@Override
	public String getAdapterName() {
		return MatcherProtocolAdapter.class.getSimpleName();
	}
	
}
