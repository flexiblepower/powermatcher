package net.powermatcher.core.direct.protocol.adapter;


import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherConnectorService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * <p>
 * Adapter class to provide an agent with functionality to publish
 * and receive messages using the Power Matcher protocol.
 * </p>
 * <p>
 * This adapter directly connects a PowerMatcher agent to its matcher using
 * the AgentService and MatcherService interfaces of the agent and matcher's connectors.
 * </p>
 * <p>
 * The upstream connection from agent to matcher is always direct. The downstream connection
 * from matcher to agent is via a proxy that processes price update events synchronously or
 * asynchronously, depending on the configuration.
 * </p>
 * <p>
 * The adapter is created for the agent connector as primary connector interface. Upon binding,
 * the adapter locates the parent matcher using the connector locater.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see MatcherService
 * @see AgentConnectorService
 */
public class DirectProtocolAdapter extends Adapter {

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class AgentProxy implements AgentService {

		private AgentService agent;

		public AgentProxy(AgentService agent) {
			this.agent = agent;
		}

		/**
		 * Update market basis with the specified new market basis parameter.
		 * 
		 * @param newMarketBasis
		 *            The new market basis (<code>MarketBasis</code>) parameter.
		 */
		@Override
		public void updateMarketBasis(final MarketBasis newMarketBasis) {
			ScheduledExecutorService scheduler = getScheduler();
			if (scheduler != null) {
				Runnable async = new Runnable() {
					
					@Override
					public void run() {
						agent.updateMarketBasis(newMarketBasis);
					}
				};
				scheduler.execute(async);
			} else {
				agent.updateMarketBasis(newMarketBasis);
			}
		}

		/**
		 * Update price info with the specified new price info parameter.
		 * 
		 * @param newPriceInfo
		 *            The new price info (<code>PriceInfo</code>) parameter.
		 */
		@Override
		public void updatePriceInfo(final PriceInfo newPriceInfo) {
			ScheduledExecutorService scheduler = getScheduler();
			if (scheduler != null) {
				Runnable async = new Runnable() {
					
					@Override
					public void run() {
						agent.updatePriceInfo(newPriceInfo);
					}
				};
				scheduler.execute(async);
			} else {
				agent.updatePriceInfo(newPriceInfo);
			}
		}

	}

	/**
	 * Define the agent connector (AgentConnectorService) field.
	 */
	private AgentConnectorService agentConnector;

	/**
	 * Define the agent connector (AgentConnectorService) field.
	 */
	private MatcherConnectorService matcherConnector;

	/**
	 * Define the matcher reference (ConnectorReference) field.
	 */
	private ConnectorReference<MatcherConnectorService> matcherRef;

	/**
	 * Define the agent proxy (AgentProxy) field.
	 */
	private AgentProxy agentProxy;

	/**
	 * Constructs an instance of this class.
	 */
	public DirectProtocolAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public DirectProtocolAdapter(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		super.bind();
		if (this.matcherRef != null) {
			this.matcherConnector =  this.matcherRef.getConnector();
		}
		this.agentConnector.bind(this.matcherConnector.getMatcher());
		this.matcherConnector.bind(this.agentProxy);
	}

	/**
	 * Gets the agent connector (AgentConnectorService) value.
	 * 
	 * @return The agent connector (AgentConnectorService) value.
	 */
	public AgentConnectorService getAgentConnector() {
		return this.agentConnector;
	}

	/**
	 * Gets the matcher connector (MatcherConnectorService) value.
	 * 
	 * @return The matcher connector (MatcherConnectorService) value.
	 */
	public MatcherConnectorService getMatcherConnector() {
		return this.matcherConnector;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.agentConnector.isEnabled();
	}

	/**
	 * Sets the agent connector value.
	 * 
	 * @param agentConnector
	 *            The agent connector (<code>AgentConnectorService</code>)
	 *            parameter.
	 */
	public void setAgentConnector(final AgentConnectorService agentConnector) {
		this.agentConnector = agentConnector;
		if (agentConnector == null) {
			this.agentProxy = null;
		} else {
			this.agentProxy = new DirectProtocolAdapter.AgentProxy(agentConnector.getAgent());
		}
	}

	/**
	 * Sets the matcher connector value.
	 * 
	 * @param matcherConnector
	 *            The agent connector (<code>MatcherConnectorService</code>)
	 *            parameter.
	 */
	public void setMatcherConnector(final MatcherConnectorService matcherConnector) {
		this.matcherConnector = matcherConnector;
	}

	void setMatcherRef(ConnectorReference<MatcherConnectorService> matcherRef) {
		this.matcherRef = matcherRef;
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.agentConnector.unbind(this.matcherConnector.getMatcher());
		this.matcherConnector.unbind(this.agentProxy);
	}

}
