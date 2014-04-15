package net.powermatcher.core.messaging.protocol.adapter;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerConnectorService;
import net.powermatcher.core.agent.framework.log.LogListenerService;
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
 * You can bind the adapter to a log listener agent via the connector interface (LogListenerConnectorService) 
 * of the agent. Once the connector is defined with the setConnector(LogListenerConnectorService) method, 
 * the bind() method will bind the adapter to the agent and invoke the LogListenerService interface to
 * update the market basis. 
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogListenerConnectorService
 * @see LogListenerService
 * @see PriceLogMessage
 * @see BidLogMessage
 * @see BaseAdapter
 */
public class LogListenerAdapter extends BaseAdapter {

	/**
	 * Define the bid log pattern (Topic) field.
	 */
	private Topic bidLogPattern;
	/**
	 * Define the price log pattern (Topic) field.
	 */
	private Topic priceLogPattern;
	/**
	 * Define the agent (LogListenerService) field.
	 */
	private LogListenerService logListener;

	/**
	 * Define the agent connector (LogListenerConnectorService) field.
	 */
	private LogListenerConnectorService logListenerConnector;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #LogListenerAdapter(ConfigurationService)
	 */
	public LogListenerAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #LogListenerAdapter()
	 */
	public LogListenerAdapter(final ConfigurationService configuration) {
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
		this.logListenerConnector.bind();
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
	 * Gets the bid log pattern (Topic) value.
	 * 
	 * @return The bid log pattern (<code>String</code>) value.
	 */
	protected Topic getBidLogPattern() {
		return this.bidLogPattern;
	}

	/**
	 * Gets the agent connector (LogListenerConnectorService) value.
	 * 
	 * @return The agent connector (LogListenerConnectorService) value.
	 * @see #setLogListenerConnector(LogListenerConnectorService)
	 */
	public LogListenerConnectorService getLogListenerConnector() {
		return this.logListenerConnector;
	}

	/**
	 * Gets the price log pattern (Topic) value.
	 * 
	 * @return The price log pattern (<code>String</code>) value.
	 */
	protected Topic getPriceLogPattern() {
		return this.priceLogPattern;
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[] { getBidLogPattern(), getPriceLogPattern() };
	}

	/**
	 * Handle bid log event with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	protected void handleBidLogEvent(final Topic topic, final byte[] data) {
		try {
			BidLogMessage bidLogMessage = new BidLogMessage(data);
			BidLogInfo bidLogInfo = bidLogMessage.getBidLogInfo();
			if (this.logListener != null) {
				this.logListener.handleBidLogInfo(bidLogInfo);
			}
		} catch (final InvalidObjectException e) {
			logError("Error processing bid log event " + topic, e);
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
		if (topic.matches(getBidLogPattern())) {
			handleBidLogEvent(topic, data);
		} else if (topic.matches(getPriceLogPattern())) {
			handlePriceLogEvent(topic, data);
		} else {
			return super.handleMessageArrived(topic, data);
		}
		return true;
	}

	/**
	 * Handle price log event with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	protected void handlePriceLogEvent(final Topic topic, final byte[] data) {
		try {
			PriceLogMessage priceLogMessage = new PriceLogMessage(data);
			PriceLogInfo priceLogInfo = priceLogMessage.getPriceLogInfo();
			if (this.logListener != null) {
				this.logListener.handlePriceLogInfo(priceLogInfo);
			}
		} catch (final InvalidObjectException e) {
			logError("Error processing price log event " + topic, e);
		}
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.bidLogPattern = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
				.addLevel(Topic.SINGLE_LEVEL_WILDCARD).addLevel(getBidTopicSuffix()).addLevel(getLogTopicSuffix());
		this.priceLogPattern = Topic.create(ProtocolAdapterConstants.POWERMATCHER_TOPIC_PREFIX).addLevel(getClusterId())
				.addLevel(Topic.SINGLE_LEVEL_WILDCARD).addLevel(getPriceInfoTopicSuffix()).addLevel(getLogTopicSuffix());
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.logListenerConnector.isEnabled();
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
	 * Sets the agent connector value.
	 * 
	 * @param agentConnector
	 *            The agent connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 * @see #getLogListenerConnector()
	 */
	public void setLogListenerConnector(final LogListenerConnectorService agentConnector) {
		this.logListenerConnector = agentConnector;
		if (agentConnector == null) {
			this.logListener = null;
		} else {
			this.logListener = agentConnector.getLogListener();
		}
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.logListenerConnector.unbind();
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
