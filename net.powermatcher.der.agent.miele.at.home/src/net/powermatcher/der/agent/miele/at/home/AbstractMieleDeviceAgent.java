package net.powermatcher.der.agent.miele.at.home;


import java.util.Date;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.der.agent.miele.at.home.config.MieleApplianceConfiguration;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;
import net.powermatcher.der.agent.miele.at.home.msg.MieleApplianceInfoMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayActionOkMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayErrorMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayMessage;
import net.powermatcher.der.agent.miele.at.home.xml.MieleGatewayMessageParser;
import net.powermatcher.der.agent.miele.at.home.xml.MieleGatewayMessageParserException;
import net.powermatcher.telemetry.framework.TelemetryDataPublisher;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractMieleDeviceAgent extends Agent implements TelemetryConnectorService {

	// Constants for calculating bid price

	/**
	 *
	 */
	public enum ActionResult {
		/**
		 * ordinal 0
		 */
		UNKNOWN,
		/**
		 * ordinal 1
		 */
		SUCCESS,
		/**
		 * ordinal 2
		 */
		FAILURE;
	}

	/** Miniumum price that a Miele agent can bid. */
	public static final int MIN_BID_PRICE = 0;

	/** Maximum price that a Miele agent can bid. */
	public static final int MAX_BID_PRICE = 127;

	// Agent fields

	/* * Number of seconds between actions */
	private static final int ACTION_TIME_INTERVAL = 3;

	/* * ID of appliance of this agent */
	private String applianceID;

	/* * Type of appliance for this agent */
	private String applianceType;

	/* * Preferred language code for this agent */
	private String languageCode;

	/* * Power consumption for this appliance */
	private int powerConsumption;

	/* * Miele gateway URL */
	private String gatewayUrl;

	// Helper fields required for processing

	/* * Parser for Miele Gateway responses */
	private MieleGatewayMessageParser parser;

	/* * URL base for appliance operations */
	private String applianceUrlBase;

	/* * Parameters for identifying appliance */
	private String applianceParameters;

	/* * Last appliance info received */
	protected MieleApplianceInfoMessage lastApplianceInfo;

	/* * Last appliance info received */
	protected Date lastApplianceInfoTimestamp;

	/* * Last bid price sent to the concentrator */
	private int lastBidPrice;

	/* * Last appliance info received */
	protected Date lastPriceInfoTimestamp;

	/* * Timestamp last appliance action performed */
	protected Date lastActionTimestamp;

	protected TelemetryDataPublisher telemetryDataPublisher;

	/**
	 * Constructs an instance of this class.
	 * 
	 */
	public AbstractMieleDeviceAgent() {
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public AbstractMieleDeviceAgent(final Configurable configuration) {
		super(configuration);
	}

	@Override
	public void bind(TelemetryService telemetryPublisher) {
		this.telemetryDataPublisher = new TelemetryDataPublisher(getConfiguration(), telemetryPublisher);
	}

	@Override
	public void unbind(TelemetryService telemetryPublisher) {
		this.telemetryDataPublisher = null;
	}

	/**
	 * Abstract method to be implemented by subclass. Calculates the bid price.
	 * 
	 * @return The new bid price.
	 */
	abstract protected int calculateBidPrice();

	/**
	 * Checks the time stamp of the last action to prevent that the gateway will
	 * be overloaded by requests and to give it time to process the action.
	 */
	private void checkActionWaitTime() {
		if (this.lastActionTimestamp != null) {
			Date current = new Date(getCurrentTimeMillis());
			int timeSinceLastAction = Math.round((current.getTime() - this.lastActionTimestamp.getTime()) / 1000);

			// Wait before completing action
			if (this.lastActionTimestamp != null && (timeSinceLastAction < ACTION_TIME_INTERVAL)) {
				try {
					Thread.sleep((ACTION_TIME_INTERVAL - timeSinceLastAction) * 1000);
				} catch (InterruptedException e) {
					// Do nothing
				}
			}
		}
	}

	/**
	 * Create a new bid message
	 * 
	 * @param The
	 *            bid price.
	 * @return The new bid for the appliance.
	 */
	private BidInfo createBid(final int price) {
		// Create the bid message
		MarketBasis marketBasis = getCurrentMarketBasis();
		if (marketBasis != null) {
			PricePoint pricePoint1 = new PricePoint(price, this.powerConsumption);
			PricePoint pricePoint2 = new PricePoint(price, 0);
			BidInfo newBidInfo = new BidInfo(marketBasis, pricePoint1, pricePoint2);
			return newBidInfo;
		}
		return null;
	}

	/**
	 * Do the periodic bid update.
	 */
	@Override
	protected synchronized void doBidUpdate() {
		try {
			// Get appliance info
			getApplianceInfo();

			// Publish statistics
			publishStatistics();

			// Calculate the price
			int price = calculateBidPrice();

			// Send the bid
			sendBid(price);

			// If necessary perform status change
			performApplianceStateChange();

		} catch (Throwable t) {
			logError("Error occurred in while updating agent '" + getId() + "'", t);
		}
	}

	/**
	 * Retrieve the action from the appliance info object and return the action.
	 * If the action is not available log an error and return null
	 * 
	 * @param action
	 *            The action name.
	 * @return The action url if currently available or otherwise null.
	 */
	private String getActionUrl(final String action) {
		String actionURL = null;
		if (this.lastApplianceInfo != null && this.lastApplianceInfo.getActions() != null) {
			// Get the action from the available action list of the appliance
			actionURL = this.lastApplianceInfo.getActions().get(action);
		}

		if (actionURL == null) {
			String msg = "Action '" + action + "' for " + this.applianceID + " is (currently) not available.";
			if (this.lastApplianceInfo == null) {
				msg += " No appliance info received. ";
			}
			logWarning(msg);
		}
		return actionURL;
	}

	/**
	 * @return TODO
	 */
	public String getApplianceID() {
		return this.applianceID;
	}

	/**
	 * Get information about the appliance from the gateway. If an appliance
	 * info message is received then the lastApplianceInfo property is set
	 * otherwise this property is cleared to null. The return value is a
	 * MieleGatewayMessage message instance
	 * 
	 * @return A MieleGatewayMessage from the Miele Gateway
	 */
	protected MieleGatewayMessage getApplianceInfo() {

		MieleGatewayMessage response = sendGatewayRequest(this.applianceUrlBase, this.applianceParameters);

		// Store if there is an appliance info message received
		if (response instanceof MieleApplianceInfoMessage) {
			this.lastApplianceInfo = (MieleApplianceInfoMessage) response;
			this.lastApplianceInfoTimestamp = new Date(getCurrentTimeMillis());
		} else {
			String msg = "No appliance info received. ";
			if (response instanceof MieleGatewayErrorMessage) {
				msg += "Error: " + ((MieleGatewayErrorMessage) response).getMessage();
			}
			logError(msg);

			this.lastApplianceInfo = null;
			this.lastApplianceInfoTimestamp = null;
		}

		return response;
	}

	/**
	 * Getters and setters
	 * @return TODO
	 */

	public int getApplianceState() {
		if (this.lastApplianceInfo == null) {
			return MieleApplianceConstants.MA_STATE_UNKNOWN;
		}
		return this.lastApplianceInfo.getApplianceState();
	}

	/**
	 * @return TODO
	 */
	public String getLanguageCode() {
		return this.languageCode;
	}

	/**
	 * Get the last bid price sent to the concentrator.
	 * 
	 * @return TODO
	 */
	public int getLastBidPrice() {
		return this.lastBidPrice;
	}

	/**
	 * Return the state description based on the current language settings.
	 * 
	 * @param state
	 *            The current state (int).
	 * @return The current state description.
	 */
	public String getStateDescription(final int state) {
		return MieleApplianceUtil.getStateDescription(getLanguageCode(), state);
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.parser = new MieleGatewayMessageParser();
		// Set the fields from the configuration properties
		this.applianceID = getProperty(MieleApplianceConfiguration.MIELE_APPLIANCE_ID, "UNKNOWNID");
		this.applianceType = getProperty(MieleApplianceConfiguration.MIELE_APPLIANCE_TYPE, "UNKNOWNTYPE");
		this.languageCode = getProperty(MieleApplianceConfiguration.MIELE_AGENT_LANGUAGE_CODE,
				MieleApplianceConfiguration.MIELE_AGENT_LANGUAGE_CODE_DEFAULT);
		this.powerConsumption = getProperty(MieleApplianceConfiguration.MIELE_APPLIANCE_POWER_CONSUMPTION,
				MieleApplianceConfiguration.MIELE_APPLIANCE_POWER_CONSUMPTION_DEFAULT);

		// Get Miele Gateway properties for this Miele agent
		String protocol = getProperty(MieleApplianceConfiguration.MIELE_GATEWAY_PROTOCOL_PROPERTY,
				MieleApplianceConfiguration.MIELE_GATEWAY_PROTOCOL_DEFAULT);
		String hostname = getProperty(MieleApplianceConfiguration.MIELE_GATEWAY_HOSTNAME_PROPERTY,
				MieleApplianceConfiguration.MIELE_GATEWAY_HOSTNAME_DEFAULT);
		String port = getProperty(MieleApplianceConfiguration.MIELE_GATEWAY_PORT_PROPERTY,
				MieleApplianceConfiguration.MIELE_GATEWAY_PORT_DEFAULT);

		// Set the gateway URL
		this.gatewayUrl = protocol + "://" + hostname + ":" + port;

		// Construct the URL for retrieving the appliance info
		this.applianceUrlBase = this.gatewayUrl + MieleGatewayConstants.MG_URL_DEVICE_TARGET;

		// Set the URL parameter string
		this.applianceParameters = MieleGatewayConstants.MG_URL_PARAM_LANGUAGE + '=' + this.languageCode + '&'
				+ MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_ID + '=' + this.applianceID + '&'
				+ MieleGatewayConstants.MG_URL_PARAM_APPLIANCE_TYPE + '=' + this.applianceType;
	}

	/**
	 * Perform action on appliance by sending a request to the Miele Gateway.
	 * 
	 * @param action
	 *            The action to be performed.
	 * @param paramName
	 *            The parameter name (e.g. 'p1').
	 * @param paramValue
	 *            The value of the parameter.
	 * @return The action result (ActionResult).
	 */
	protected ActionResult performApplianceAction(final String action, final String paramName, final String paramValue) {
		ActionResult result = ActionResult.FAILURE;

		// Retrieve the action from the appliance info object
		String actionURL = getActionUrl(action);

		if (actionURL != null) {
			// Check time interval between actions
			checkActionWaitTime();

			// Perform request and parse the response
			this.lastActionTimestamp = new Date(getCurrentTimeMillis());
			MieleGatewayMessage response = sendGatewayRequest(actionURL, null);

			// When an ActionOk message is received the action will be performed
			if (response instanceof MieleGatewayActionOkMessage) {
				logInfo("Action '" + action + "' for " + this.applianceID + " completed successfully.");
				result = ActionResult.SUCCESS;
			} else if (response instanceof MieleGatewayErrorMessage) {
				logError("Action '" + action + "' for " + this.applianceID + " failed. Error: "
						+ ((MieleGatewayErrorMessage) response).getMessage());
			} else {
				String logMsg = "Unexpected error. Action '" + action + "' for " + this.applianceID + " failed. ";
				if (response != null) {
					logMsg += " Gateway response: " + response.toString();
				}
				logError(logMsg);
				result = ActionResult.UNKNOWN;
			}
		}
		return result;
	}

	/**
	 * Abstract method that in which subclasses will manipulate the state of the
	 * appliance depending on factors like bid and market price.
	 */
	abstract protected void performApplianceStateChange();

	/**
	 * Abstract method to be implemented by subclass. Publishes the appliance
	 * status and measurement data.
	 */
	abstract protected void publishStatistics();

	/**
	 * Send a bid message to the PowerMatcher concentrator. No bid message is
	 * sent when the state is unknown. When the state changes to END or OFF only
	 * once a bid message with price 0 is sent.
	 * 
	 * @param price
	 */
	protected void sendBid(final int price) {
		// Get the current appliance state
		int state = getApplianceState();

		// Do only send a message when:
		// 1. The appliance state is NOT unknown, AND
		// 2. The appliance state is NOT OFF and NOT END, except when the last
		// bid sent had a price <> 0.
		if (state != MieleApplianceConstants.MA_STATE_UNKNOWN
				&& ((state != MieleApplianceConstants.MA_STATE_OFF && state != MieleApplianceConstants.MA_STATE_END) || (getLastBid() != null && this.lastBidPrice != 0))) {

			// Create the bid message
			BidInfo newBidInfo = createBid(price);
			BidInfo updatedBidInfo = publishBidUpdate(newBidInfo);
			if (isDebugEnabled()) {
				logDebug("Published new bid " + updatedBidInfo);
			}
			this.lastBidPrice = price;
		} else {
			// Log warning messages, except when state is OFF or END
			if (state != MieleApplianceConstants.MA_STATE_OFF && state != MieleApplianceConstants.MA_STATE_END) {

				String infoMsg = "No bid sent. Reason: ";
				if (this.lastApplianceInfo == null) {
					infoMsg += " No appliance info received. ";
				}
				if (state == MieleApplianceConstants.MA_STATE_UNKNOWN) {
					infoMsg += " Appliance state is : " + MieleApplianceUtil.getStateDescription(getLanguageCode(), state);
				}
				logWarning(infoMsg);
			}
		}
	}

	private MieleGatewayMessage sendGatewayRequest(final String url, final String parameters) {
		MieleGatewayMessage response = null;
		try {
			response = this.parser.parse(HttpUtils.httpGet(url, parameters, MieleGatewayConstants.MG_DEFAULT_ENCODING),
					this.languageCode);
		} catch (MieleGatewayMessageParserException e) {
			logError("Error parsing Miele Gateway response", e);
		} catch (HttpUtilException e) {
			logError("HTTP error when requesting Miele Gateway information", e);
		}
		return response;
	}

	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * @param languageCode
	 */
	public void setLanguageCode(final String languageCode) {
		this.languageCode = languageCode;
	}

	@Override
	public void updatePriceInfo(final PriceInfo newPriceInfo) {
		if (isInfoEnabled()) {
			logInfo("New price info received: " + newPriceInfo);
		}
		super.updatePriceInfo(newPriceInfo);
	}

}