package net.powermatcher.core.direct.protocol.adapter;


import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.service.ChildConnectable;
import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.agent.framework.service.ParentConnectable;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * Adapter factory for DirectProtocolAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ChildConnectable
 * @see DirectProtocolAdapter
 * @see DownMessagable
 */
public class DirectProtocolAdapterFactory implements DirectAdapterFactoryService<ChildConnectable, ParentConnectable> {

	public DirectProtocolAdapterFactory() {
	}

	@Override
	public DirectProtocolAdapter createAdapter(Configurable configuration, ChildConnectable connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String parentMatcherId = getTargetConnectorIds(connector)[adapterIndex];
		ConnectorReference<ParentConnectable> matcherRef = new ConnectorReference<ParentConnectable>(
				connectorLocater, connector.getConnectorId(), ParentConnectable.class, connector.getClusterId(), parentMatcherId);
		DirectProtocolAdapter agentAdapter = new DirectProtocolAdapter(configuration);
		agentAdapter.setAgentConnector(connector);
		agentAdapter.setMatcherRef(matcherRef);
		return agentAdapter;
	}

	/**
	 * Create adapter with the specified configuration and agent and matcher connector
	 * parameters and return the DirectProtocolAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param agentConnector
	 *            The agent connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 * @param matcherConnector
	 *            The matcher connector (<code>MatcherConnectorService</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>DirectProtocolAdapter</code>)
	 *         value.
	 */
	@Override
	public DirectProtocolAdapter createAdapter(final Configurable configuration,
			final ChildConnectable agentConnector, final ParentConnectable matcherConnector) {
		DirectProtocolAdapter agentAdapter = new DirectProtocolAdapter(configuration);
		agentAdapter.setAgentConnector(agentConnector);
		agentAdapter.setMatcherConnector(matcherConnector);
		return agentAdapter;
	}

	/**
	 * Get the parent matcher ids configured for the agent.
	 * @param connector The agent to get the parent matcher id from
	 * @return The parent matcher id configured for the agent.
	 */
	@Override
	public String[] getTargetConnectorIds(final ChildConnectable connector) {
		return connector.getConfiguration().getProperty(AgentConfiguration.PARENT_MATCHER_ID_PROPERTY, AgentConfiguration.PARENT_MATCHER_ID_DEFAULT);
	}

	@Override
	public String getAdapterName() {
		return DirectProtocolAdapter.class.getSimpleName();
	}
	
}
