package net.powermatcher.core.messaging.protocol.adapter;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.core.messaging.protocol.adapter.config.AgentProtocolAdapterConfiguration;
import net.powermatcher.core.messaging.protocol.adapter.constants.ProtocolAdapterConstants;
import net.powermatcher.core.messaging.protocol.adapter.msg.BidMessage;


/**
 * <p>
 * Adapter class to provide an agent with functionality to publish
 * and receive messages using the Power Matcher protocol.
 * </p>
 * <p>
 * The class provides implementations of the MatcherService and AgentLoggingService
 * interfaces using an inner classes. After a new agent connector is set, the bind()
 * method will bind instances of the classes to the agent using the AgentConnectorService
 * interface.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see MatcherService
 * @see AgentConnectorService
 */
public class AgentProtocolAdapter extends ProtocolAdapter {
	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class MatcherPublisher implements MatcherService {
		/**
		 * Update bid info with the specified agent ID and new bid parameters.
		 * 
		 * @param agentId
		 *            The agent ID (<code>String</code>) parameter.
		 * @param newBidInfo
		 *            The new bid info (<code>BidInfo</code>) parameter.
		 */
		@Override
		public void updateBidInfo(final String agentId, final BidInfo newBidInfo) {
			BidMessage bidMessage = getMessageFactory().toBidMessage(newBidInfo);
			AgentProtocolAdapter.this.publish(getBidTopic(), bidMessage.toBytes());
		}

	}

	/**
	 * Define the parent matcher ID (String) field.
	 */
	private String parentMatcherId;
	/**
	 * Define the bid topic (Topic) field.
	 */
	private Topic bidTopic;
	/**
	 * Define the price info pattern (Topic) field.
	 */
	private Topic priceInfoPattern;
	/**
	 * Define the agent (AgentService) field.
	 */
	private AgentService agent;
	/**
	 * Define the parent matcher (MatcherService) field.
	 */
	private MatcherService parentMatcherAdapter;

	/**
	 * Define the agent connector (AgentConnectorService) field.
	 */
	private AgentConnectorService agentConnector;

	/**
	 * Constructs an instance of this class.
	 */
	public AgentProtocolAdapter() {
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
	public AgentProtocolAdapter(final ConfigurationService configuration) {
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
		this.agentConnector.bind(this.parentMatcherAdapter);
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
	 * Gets the agent connector (AgentConnectorService) value.
	 * 
	 * @return The agent connector (AgentConnectorService) value.
	 */
	public AgentConnectorService getAgentConnector() {
		return this.agentConnector;
	}

	/**
	 * Gets the bid topic (Topic) value.
	 * 
	 * @return The bid topic (<code>String</code>) value.
	 */
	protected Topic getBidTopic() {
		if (this.bidTopic == null) {
			this.bidTopic = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
			.addLevel(getParentMatcherId()).addLevel(getId()).addLevel(getBidTopicSuffix());
		}
		return this.bidTopic;
	}

	/**
	 * Gets the parent matcher ID (String) value.
	 * 
	 * @return The parent matcher ID (<code>String</code>) value.
	 */
	public String getParentMatcherId() {
		return this.parentMatcherId;
	}

	/**
	 * Gets the price info topic (Topic) value.
	 * 
	 * @return The price info topic (<code>String</code>) value.
	 */
	protected Topic getPriceInfoPattern() {
		if (this.priceInfoPattern == null) {
			this.priceInfoPattern = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
			.addLevel(getParentMatcherId()).addLevel(getPriceInfoTopicSuffix());
		}
		return this.priceInfoPattern;
	}

	/**
	 * Gets the protocol property (String) value.
	 * 
	 * @return The protocol property (<code>String</code>) value.
	 */
	@Override
	protected String getProtocolProperty() {
		return AgentProtocolAdapterConfiguration.AGENT_PROTOCOL_PROPERTY;
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[] { getPriceInfoPattern() };
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
		if (topic.matches(getPriceInfoPattern())) {
			handlePriceInfoUpdate(topic, data);
		} else {
			return super.handleMessageArrived(topic, data);
		}
		return true;
	}

	/**
	 * Handle price info update with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	protected void handlePriceInfoUpdate(final Topic topic, final byte[] data) {
		try {
			PriceInfo newPriceInfo = getMessageFactory().toPriceInfo(data);
			setCurrentMarketBasis(newPriceInfo.getMarketBasis());
			setCurrentPriceInfo(newPriceInfo);
			if (this.agent != null) {
				this.agent.updatePriceInfo(newPriceInfo);
			}
		} catch (final InvalidObjectException e) {
			logError("Error processing price info update " + topic, e);
		}
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
			this.agent = null;
			this.parentMatcherAdapter = null;
		} else {
			this.agent = agentConnector.getAgent();
			this.parentMatcherAdapter = new AgentProtocolAdapter.MatcherPublisher();
		}
	}

	/**
	 * Sets the current market basis value.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 */
	@Override
	public void setCurrentMarketBasis(final MarketBasis newMarketBasis) {
		if (getCurrentMarketBasis() != newMarketBasis) {
			super.setCurrentMarketBasis(newMarketBasis);
			if (this.agent != null) {
				this.agent.updateMarketBasis(newMarketBasis);
			}
		}
	}

	/**
	 * Set the parent matcher ID to publish to.
	 * @param parentMatcherId The parent matcher ID to publish to.
	 */
	public void setParentMatcherId(String parentMatcherId) {
		this.parentMatcherId = parentMatcherId;
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.agentConnector.unbind(this.parentMatcherAdapter);
		super.unbind();
	}

	/**
	 * Unbinding.
	 */
	@Override
	protected void unbinding() {
		super.unbinding();
	}

}
