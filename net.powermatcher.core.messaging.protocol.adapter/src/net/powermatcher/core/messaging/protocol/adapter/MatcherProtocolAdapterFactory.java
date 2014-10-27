package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.framework.service.ParentConnectable;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * Adapter factory for MatcherProtocolAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ParentConnectable
 * @see MatcherProtocolAdapter
 * @see UpMessagable
 */
public class MatcherProtocolAdapterFactory implements TargetAdapterFactoryService<ParentConnectable> {

	public MatcherProtocolAdapterFactory() {
	}

	@Override
	public MatcherProtocolAdapter createAdapter(Configurable configuration, ParentConnectable connector,
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
	public MatcherProtocolAdapter createAdapter(final Configurable configuration,
			final ParentConnectable matcherConnector) {
		MatcherProtocolAdapter matcherAdapter = new MatcherProtocolAdapter(configuration);
		matcherAdapter.setMatcherConnector(matcherConnector);
		return matcherAdapter;
	}

	@Override
	public String getAdapterName() {
		return MatcherProtocolAdapter.class.getSimpleName();
	}
	
}
