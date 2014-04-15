package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.agent.framework.config.AgentConfiguration.LoggingLevel;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.protocol.adapter.config.ProtocolAdapterConfiguration;
import net.powermatcher.core.messaging.protocol.adapter.config.ProtocolAdapterConfiguration.Protocol;
import net.powermatcher.core.messaging.protocol.adapter.constants.ProtocolAdapterConstants;
import net.powermatcher.core.messaging.protocol.adapter.han.HANMessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.internal.InternalMessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.msg.MessageFactory;


/**
 * Abstract class that implements the generic functionality of a messaging
 * adapter for the PowerMatcher protocol. 
 * 
 * <p>
 * The class extends the messaging functionality of the BaseAdapter by adding support
 * for sending PowerMatcher messages over the Internal V1 and HAN rev6 protocol.
 * It also adds specific PowerMatcher functionality for setting and retrieving the
 * current market basis (MarketBasis), current price info and methods for logging bid
 * info and price info. The extent of logging can be controlled by defining the LoggingLevel.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see Protocol
 * @see LoggingLevel
 * @see MarketBasis
 * @see BidInfo
 * @see PriceInfo
 */
public abstract class ProtocolAdapter extends BaseAdapter implements ProtocolAdapterConstants {
	/**
	 * Define the protocol (Protocol) field.
	 */
	private Protocol protocol;
	/**
	 * Define the message factory (MessageFactory) field.
	 */
	private MessageFactory messageFactory;
	/**
	 * Define the current market basis (MarketBasis) field.
	 */
	private MarketBasis currentMarketBasis;
	/**
	 * Define the current price info (PriceInfo) field.
	 */
	private PriceInfo currentPriceInfo;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #ProtocolAdapter(ConfigurationService)
	 */
	protected ProtocolAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #ProtocolAdapter()
	 */
	protected ProtocolAdapter(final ConfigurationService configuration) {
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
	 * Gets the current market basis (MarketBasis) value.
	 * 
	 * @return The market current basis (<code>MarketBasis</code>) value.
	 * @see #setCurrentMarketBasis(MarketBasis)
	 */
	public MarketBasis getCurrentMarketBasis() {
		return this.currentMarketBasis;
	}

	/**
	 * Gets the current price info (PriceInfo) value.
	 * 
	 * @return The current price info (<code>PriceInfo</code>) value.
	 * @see #setCurrentPriceInfo(PriceInfo)
	 */
	public PriceInfo getCurrentPriceInfo() {
		return this.currentPriceInfo;
	}

	/**
	 * Gets the protocol message factory.
	 * 
	 * @return The protocol message factory (<code>MessageFactory</code>) value.
	 */
	protected MessageFactory getMessageFactory() {
		return this.messageFactory;
	}

	/**
	 * Gets the protocol value.
	 * 
	 * @return The protocol (<code>Protocol</code>) value.
	 */
	protected ProtocolAdapterConfiguration.Protocol getProtocol() {
		return this.protocol;
	}

	/**
	 * Gets the protocol property (String) value.
	 * 
	 * @return The protocol property (<code>String</code>) value.
	 */
	protected abstract String getProtocolProperty();

	/**
	 * Initialize.
	 */
	private void initialize() {
		String defaultProtocolName = getProperty(ProtocolAdapterConfiguration.PROTOCOL_PROPERTY,
				ProtocolAdapterConfiguration.PROTOCOL_PROPERTY_DEFAULT);
		String protocolName = getProperty(getProtocolProperty(), defaultProtocolName);
		this.protocol = Protocol.valueOf(protocolName);

		logInfo("Configuring adapter for protocol " + protocolName);
		if (this.protocol == Protocol.INTERNAL_v1) {
			this.messageFactory = InternalMessageFactory.getInstance();
		} else if (this.protocol == Protocol.HAN_rev6) {
			this.messageFactory = HANMessageFactory.getInstance();
		} else {
			logError("Unimplemented protocol: " + this.protocol);
		}
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
	 * Sets the current market basis value.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 * @see #getCurrentMarketBasis()
	 */
	public void setCurrentMarketBasis(final MarketBasis newMarketBasis) {
		this.currentMarketBasis = newMarketBasis;
	}

	/**
	 * Sets the current price info value.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @see #getCurrentPriceInfo()
	 */
	public void setCurrentPriceInfo(final PriceInfo newPriceInfo) {
		this.currentPriceInfo = newPriceInfo;
	}

	/**
	 * Unbinding.
	 */
	@Override
	protected void unbinding() {
		super.unbinding();
	}

}
