package net.powermatcher.core.messaging.protocol.adapter;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.config.MatcherAgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherConnectorService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.core.messaging.protocol.adapter.config.MatcherProtocolAdapterConfiguration;
import net.powermatcher.core.messaging.protocol.adapter.constants.ProtocolAdapterConstants;
import net.powermatcher.core.messaging.protocol.adapter.msg.PriceInfoMessage;


/**
 * A MatcherProtocolAdapter provides the messaging functionality required for a MatcherAgent.
 * 
 * <p>
 * The adapter provides the following services for a matcher agent:
 * <ul>
 * <li>Listen for bid updates from connected agents and forward them to the matcher.
 * </li>
 * <li>Implements the AgentService interface for receiving price info update and market basis
 * updates. The inner class AgentPublisher implements the AgentService interface and publishes
 * the updated price info to the price info update topic.
 * </li>
 * </ul>
 * </p>
 * <p>
 * A MatcherProtocolAdapter is connected to a Matcher through the MatcherConnectorService interface.
 * The bind() method will supply the matcher with a AgentService object and a MatcherLoggingService
 * object. 
 * </p>
 * <p>
 * The adapter is configured using a a configuration object (ConfigurationService). The properties in
 * a configuration object are defined in the MatcherProtocolAdapterConfiguration interface. 
 * Special attention is required for the MatcherProtocolAdapter.MATCHER_LISTENER_ID_PROPERTY property
 * which usually has a default value that is equal to the id property. There is one situation, however, 
 * where this property must have a different value. In case this adapter for an agent that
 * receives the same messages as the matcher agent, the MatcherProtocolAdapter.MATCHER_LISTENER_ID_PROPERTY
 * should contain the id of the matcher agent.
 * </p>
 * @author IBM
 * @version 0.9.0
 */
public class MatcherProtocolAdapter extends ProtocolAdapter {
	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class AgentPublisher implements AgentService {
		/**
		 * Update market basis with the specified new market basis parameter.
		 * 
		 * @param newMarketBasis
		 *            The new market basis (<code>MarketBasis</code>) parameter.
		 */
		@Override
		public void updateMarketBasis(final MarketBasis newMarketBasis) {
			getMessageFactory().getMarketBasisCache().registerInternalMarketBasis(newMarketBasis);
			setCurrentMarketBasis(newMarketBasis);
		}

		/**
		 * Update price info with the specified new price info parameter.
		 * 
		 * @param newPriceInfo
		 *            The new price info (<code>PriceInfo</code>) parameter.
		 */
		@Override
		public void updatePriceInfo(final PriceInfo newPriceInfo) {
			setCurrentPriceInfo(newPriceInfo);
			PriceInfoMessage priceInfoMessage = getMessageFactory().toPriceInfoMessage(newPriceInfo);
			MatcherProtocolAdapter.this.publish(getPriceInfoTopic(), priceInfoMessage.toBytes());
		}

	}

	/**
	 * Define the bid topic pattern (Topic) field.
	 */
	private Topic bidTopicPattern;

	/**
	 * Define the matcher (MatcherService) field.
	 */
	private MatcherService matcher;
	/**
	 * Define the matcher ID (String) field.
	 */
	private String listenerMatcherId;
	/**
	 * Define the price info topic (Topic) field.
	 */
	private Topic priceInfoTopic;
	/**
	 * Define the agent id level (int) field.
	 */
	private int agentIdLevel;
	/**
	 * Define the child agent (AgentService) field.
	 */
	private AgentService childAgentAdapter;
	/**
	 * Define the matcher connector (MatcherConnectorService) field.
	 */
	private MatcherConnectorService matcherConnector;

	/**
	 * Constructs an instance of this class.
	 */
	public MatcherProtocolAdapter() {
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
	public MatcherProtocolAdapter(final ConfigurationService configuration) {
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
		this.matcherConnector.bind(this.childAgentAdapter);
	}

	/**
	 * Binding.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	@Override
	protected void binding() throws Exception {
		super.binding();
	}

	/**
	 * Get agent ID from topic with the specified topic parameter and return the
	 * String result.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @return Results of the get agent ID from topic (<code>String</code>)
	 *         value.
	 */
	protected String getAgentIdFromTopic(final Topic topic) {
		return topic.getLevel(this.agentIdLevel);
	}

	/**
	 * Gets the bid topic pattern (Topic) value.
	 * 
	 * @return The bid topic pattern (<code>String</code>) value.
	 */
	protected Topic getBidTopicPattern() {
		return this.bidTopicPattern;
	}

	/**
	 * Gets the listener matcher ID (String) value.
	 * 
	 * @return The listener matcher ID (<code>String</code>) value.
	 */
	protected String getListenerMatcherId() {
		return this.listenerMatcherId;
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
	 * Gets the price info topic (Topic) value.
	 * 
	 * @return The price info topic (<code>String</code>) value.
	 */
	protected Topic getPriceInfoTopic() {
		return this.priceInfoTopic;
	}

	/**
	 * Gets the protocol property (String) value.
	 * 
	 * @return The protocol property (<code>String</code>) value.
	 */
	@Override
	protected String getProtocolProperty() {
		return MatcherProtocolAdapterConfiguration.MATCHER_PROTOCOL_PROPERTY;
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[] { getBidTopicPattern() };
	}

	/**
	 * Handle bid update with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	protected void handleBidUpdate(final Topic topic, final byte[] data) {
		if (this.matcher != null) {
			try {
				String agentId = getAgentIdFromTopic(topic);
				BidInfo newBidInfo = getMessageFactory().toBidInfo(data);
				if (newBidInfo == null) {
					logWarning("Ignoring bid update, market basis unknown");
				} else {
					this.matcher.updateBidInfo(agentId, newBidInfo);
				}
			} catch (final InvalidObjectException e) {
				logError("Error processing bid update " + topic, e);
			}
		}
	}

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has
	 *         been handled.
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic, final byte[] data) {
		if (topic.matches(getBidTopicPattern())) {
			handleBidUpdate(topic, data);
		} else {
			return super.handleMessageArrived(topic, data);
		}
		return true;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.listenerMatcherId = getProperty(MatcherAgentConfiguration.MATCHER_LISTENER_ID_PROPERTY, getId());
		this.bidTopicPattern = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
				.addLevel(getListenerMatcherId()).addLevel(Topic.SINGLE_LEVEL_WILDCARD).addLevel(getBidTopicSuffix());
		this.priceInfoTopic = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
				.addLevel(getListenerMatcherId()).addLevel(getPriceInfoTopicSuffix());
		this.agentIdLevel = getBidTopicPattern().getLevelCount();
		do {
			this.agentIdLevel -= 1;
		} while (!getBidTopicPattern().isWildcard(this.agentIdLevel));
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.matcherConnector.isEnabled();
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
	 * Sets the matcher connector value.
	 * 
	 * @param matcherConnector
	 *            The matcher connector (<code>MatcherConnectorService</code>)
	 *            parameter.
	 */
	public void setMatcherConnector(final MatcherConnectorService matcherConnector) {
		this.matcherConnector = matcherConnector;
		if (matcherConnector == null) {
			this.matcher = null;
			this.childAgentAdapter = null;
		} else {
			this.matcher = matcherConnector.getMatcher();
			this.childAgentAdapter = new MatcherProtocolAdapter.AgentPublisher();
		}
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.matcherConnector.unbind(this.childAgentAdapter);
		super.unbinding();
	}

	/**
	 * Unbinding.
	 */
	@Override
	protected void unbinding() {
		super.unbinding();
	}

}
