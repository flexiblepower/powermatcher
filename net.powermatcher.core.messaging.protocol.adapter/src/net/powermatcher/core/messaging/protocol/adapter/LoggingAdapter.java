package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.core.messaging.protocol.adapter.constants.ProtocolAdapterConstants;
import net.powermatcher.core.messaging.protocol.adapter.log.BidLogMessage;
import net.powermatcher.core.messaging.protocol.adapter.log.PriceLogMessage;


/**
 * Provides services for receiving price and bid log messages for a log listener agent.  
 * 
 * <p>
 * This adapter listens for price (PriceLogMessage) and bid log (BidLogMessage) messages
 * and invokes the LogListenerService interface for handling the messages.
 * </p>
 * <p>
 * You can bind the adapter to a log listener agent via the connector interface (LoggingConnectorService) 
 * of the agent. Once the connector is defined with the setConnector(LoggingConnectorService) method, 
 * the bind() method will bind the adapter to the agent and invoke the LogListenerService interface to
 * update the market basis. 
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see LoggingConnectorService
 * @see LogListenerService
 * @see PriceLogMessage
 * @see BidLogMessage
 * @see BaseAdapter
 */
public class LoggingAdapter extends BaseAdapter {

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class LoggingPublisher implements LogListenerService {
		/**
		 * Handle bid info logging event.
		 * 
		 * @param bidLogInfo
		 *            Bid log info to handle.
		 */
		@Override
		public void handleBidLogInfo(BidLogInfo bidLogInfo) {
			BidLogMessage msg = new BidLogMessage(bidLogInfo);
			LoggingAdapter.this.publish(LoggingAdapter.this.getBidLogTopic(), msg.toBytes());
		}

		/**
		 * Handle price info logging event.
		 * 
		 * @param priceLogInfo
		 *            Price log info to handle.
		 */
		@Override
		public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
			PriceLogMessage msg = new PriceLogMessage(priceLogInfo);
			LoggingAdapter.this.publish(LoggingAdapter.this.getPriceInfoLogTopic(), msg.toBytes());
		}

	}

	/**
	 * Define the logging agent ID (String) field.
	 * The target is not being addressed in the topic.
	 */
	@SuppressWarnings("unused")
	private String loggingAgentId;
	/**
	 * Define the bid log topic (Topic) field.
	 */
	private Topic bidLogTopic;
	/**
	 * Define the price info log topic (Topic) field.
	 */
	private Topic priceInfoLogTopic;
	/**
	 * Define the agent (LogListenerService) field.
	 */
	private LogListenerService logListener;

	/**
	 * Define the agent connector (LoggingConnectorService) field.
	 */
	private LoggingConnectorService loggingConnector;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #LoggingAdapter(ConfigurationService)
	 */
	public LoggingAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #LoggingAdapter()
	 */
	public LoggingAdapter(final ConfigurationService configuration) {
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
		this.loggingConnector.bind(this.logListener);
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
	 * Gets the bid log topic (Topic) value.
	 * 
	 * @return The bid log topic (<code>String</code>) value.
	 */
	protected Topic getBidLogTopic() {
		if (this.bidLogTopic == null) {
			this.bidLogTopic = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
			.addLevel(getId()).addLevel(getBidTopicSuffix()).addLevel(getLogTopicSuffix());
		}
		return this.bidLogTopic;
	}

	/**
	 * Gets the agent connector (LoggingConnectorService) value.
	 * 
	 * @return The agent connector (LoggingConnectorService) value.
	 * @see #setLoggingConnector(LoggingConnectorService)
	 */
	public LoggingConnectorService getLoggingConnector() {
		return this.loggingConnector;
	}

	/**
	 * Gets the price info log topic (Topic) value.
	 * 
	 * @return The price info log topic (<code>String</code>) value.
	 */
	protected Topic getPriceInfoLogTopic() {
		if (this.priceInfoLogTopic == null) {
			this.priceInfoLogTopic = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
			.addLevel(getId()).addLevel(getPriceInfoTopicSuffix()).addLevel(getLogTopicSuffix());
		}
		return this.priceInfoLogTopic;
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>Topic[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[0];
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.loggingConnector.isEnabled();
	}

	/**
	 * Set the logging agent ID to publish to.
	 * @param loggingAgentId The logging agent ID to publish to.
	 */
	public void setLoggingAgentId(String loggingAgentId) {
		this.loggingAgentId = loggingAgentId;
	}

	/**
	 * Sets the agent connector value.
	 * 
	 * @param agentConnector
	 *            The agent connector (<code>LoggingConnectorService</code>)
	 *            parameter.
	 * @see #getLoggingConnector()
	 */
	public void setLoggingConnector(final LoggingConnectorService agentConnector) {
		this.loggingConnector = agentConnector;
		if (agentConnector == null) {
			this.logListener = null;
		} else {
			this.logListener = new LoggingAdapter.LoggingPublisher();
		}
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.loggingConnector.unbind(this.logListener);
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
