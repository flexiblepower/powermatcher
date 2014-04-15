package net.powermatcher.der.agent.miele.at.home;


import java.util.Date;

import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.der.agent.miele.at.home.config.MieleFridgeFreezerDeviceAgentConfiguration;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleFridgeFreezerConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;
import net.powermatcher.der.agent.miele.at.home.msg.MieleApplianceInfoMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleFridgeFreezerInfoMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleFridgeFreezerDeviceAgent extends AbstractMieleDeviceAgent {

	/*
	 * Constants
	 */

	/* * Initialization retry interval in seconds */
	private static final int INIT_RETRY_INTERVAL = 20;

	/*
	 * Property fields
	 */

	/** The defined (agent) target temperature for the freezer */
	private float freezerTargetTemperature;

	/** The defined (agent) target temperature for the refrigerator */
	private float fridgeTargetTemperature;

	/**
	 * The lowest target temperature setting that can be defined for the freezer
	 */
	private float freezerMinTemperature = MieleFridgeFreezerConstants.FREEZER_MIN_TEMPERATURE;

	/**
	 * The highest target temperature setting that can be defined for the
	 * freezer
	 */
	private float freezerMaxTemperature = MieleFridgeFreezerConstants.FREEZER_MAX_TEMPERATURE;

	/**
	 * The lowest target temperature setting that can be defined for the
	 * refrigerator
	 */
	private float fridgeMinTemperature = MieleFridgeFreezerConstants.FRIDGE_MIN_TEMPERATURE;

	/**
	 * The highest target temperature setting that can be defined for the
	 * freezer
	 */
	private float fridgeMaxTemperature = MieleFridgeFreezerConstants.FRIDGE_MAX_TEMPERATURE;

	/** The most recent calculated bid price for the freezer */
	private int lastFreezerBidPrice;

	/** The most recent calculated bid price for the refrigerator */
	private int lastFridgeBidPrice;

	// Fields required for calculation
	private float temperatureAtStartSuperFrost; // Temperature at start
												// SuperFrost to calculate
												// temperature change
	private float temperatureAtStartSuperCool; // Temperature at start SuperCool
												// to calculate temperature
												// change
	private Date timeStampStartSuperFrost; // Timestamp of start SuperFrost On.
	private Date timeStampStartSuperCool; // Timestamp of start SuperFrost Off.
	private int lastAgentDefinedRefrigeratorState; // Last state defined by
													// agent to detect SuperCool
													// on by user
	private int lastAgentDefinedFreezerState; // Last state defined by agent to
												// detect SuperFreezer on by
												// user

	/**
	 * Constructs an instance of this class.
	 * 
	 */
	public MieleFridgeFreezerDeviceAgent() {

	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public MieleFridgeFreezerDeviceAgent(final ConfigurationService configuration) {
		super(configuration);
	}

	@Override
	public void bind(final MatcherService parentMatcherAdapter) {
		logInfo("Initializing refrigerator-freezer agent.");

		// Set target temperatures
		setFreezerTargetTemperature(getProperty(MieleFridgeFreezerDeviceAgentConfiguration.MIELE_FREEZER_TARGET_TEMPERATURE,
				MieleFridgeFreezerDeviceAgentConfiguration.MIELE_FREEZER_TARGET_TEMPERATURE_DEFAULT));
		setFridgeTargetTemperature(getProperty(MieleFridgeFreezerDeviceAgentConfiguration.MIELE_FRIDGE_TARGET_TEMPERATURE,
				MieleFridgeFreezerDeviceAgentConfiguration.MIELE_FRIDGE_TARGET_TEMPERATURE_DEFAULT));

		// Init local bid info
		this.lastFreezerBidPrice = MIN_BID_PRICE;
		this.lastFridgeBidPrice = MIN_BID_PRICE;

		// Check SuperCool/Frost and switch off when on.
		resetApplianceState();

		super.bind(parentMatcherAdapter);
		logInfo("Initialization completed.");
	}

	/**
	 * Calculates the (overall) bid price of the appliance. The individual bid
	 * prices for the refrigerator and the freezer will be calculated first and
	 * stored in the lastFridgeBidPrice and the lastFreezerBidPrice instance
	 * fields respectively. The overall bid price is the maximum of both values.
	 * 
	 * When there is no appliance info or the state is 'off', the minimum bid
	 * price will be returned. This counts also for the individual bid prices of
	 * the refrigerator and the freezer.
	 * 
	 * @return The (overall) bid price for the appliance.
	 */
	@Override
	protected int calculateBidPrice() {
		// TODO Do not assume price ranges from 0 to 127 with step 1. The actual
		// market basis received should be used.

		int state = getApplianceState();

		if (this.lastApplianceInfo == null || state == MieleApplianceConstants.MA_STATE_OFF) {
			this.lastFridgeBidPrice = MIN_BID_PRICE;
			this.lastFreezerBidPrice = MIN_BID_PRICE;
			if (isInfoEnabled()) {
				logInfo("Price cannot be calculated. Appliance state is "
						+ MieleApplianceUtil.getStateDescription(getLanguageCode(), state) + " price set to" + MIN_BID_PRICE);
			}
		} else {
			this.lastFreezerBidPrice = calculateBidPrice(getFreezerState(), getFreezerTemperature(),
					getFreezerMaxTemperature(), getFreezerTargetTemperature());
			if (isInfoEnabled()) {
				logInfo("Calculated Freezer bid price=" + this.lastFreezerBidPrice + " temperature=" + getFreezerTemperature());
			}

			this.lastFridgeBidPrice = calculateBidPrice(getFridgeState(), getFridgeTemperature(), getFridgeMaxTemperature(),
					getFridgeTargetTemperature());
			if (isInfoEnabled()) {
				logInfo("Calculated Fridge bid price=" + this.lastFridgeBidPrice + " temperature=" + getFridgeTemperature());
			}
		}
		int newPrice = calculateOverallBidPrice(this.lastFridgeBidPrice, this.lastFreezerBidPrice);
		if (isInfoEnabled()) {
			logInfo("Fridge Freezer Calculated overall price=" + newPrice);
		}
		return newPrice;
	}

	/**
	 * Calculates the bid price for the appliance in the state as described by
	 * the parameters.
	 * 
	 * @param state
	 *            State of the appliance.
	 * @param temperature
	 *            Current temperature
	 * @param maxTemperature
	 *            Maximum temperature for this appliance
	 * @param targetTemperature
	 *            Target temperature
	 * @return The calculated bid price.
	 */
	private int calculateBidPrice(final int state, final Float temperature, final float maxTemperature,
			final float targetTemperature) {
		float newPrice = MIN_BID_PRICE;

		if (state != MieleApplianceConstants.MA_STATE_OFF && temperature != null && maxTemperature > targetTemperature) {

			newPrice = MAX_BID_PRICE / (maxTemperature - targetTemperature) * (temperature.floatValue() - targetTemperature);
		}
		return toBoundedPrice(newPrice);
	}

	/**
	 * The new 'overall' appliance bid price is always the MAX_BID_PRICE when
	 * the freezer or the refrigerator are cooling. The calculated prices for
	 * the refrigerator and freezer (lastFridgeBidPrice and lastFreezerBidPrice
	 * resp.) are used in that case to determine if the one or both should
	 * switch SuperCool/SuperFrost off. When SuperCool/SuperFrost are not
	 * currently on, the returned price is the maximum of the two calculated
	 * prices.
	 * 
	 * @param fridgePrice
	 *            The bid price for the refrigerator.
	 * @param freezerPrice
	 *            The bid price for the freezer
	 * @return The 'overall' price for the appliance.
	 */
	private int calculateOverallBidPrice(final int fridgePrice, final int freezerPrice) {
		if (getFridgeState() == MieleApplianceConstants.MA_STATE_SUPERCOOL
				|| getFreezerState() == MieleApplianceConstants.MA_STATE_SUPERFROST) {
			return MAX_BID_PRICE;
		} else {
			return Math.max(this.lastFridgeBidPrice, this.lastFreezerBidPrice);
		}
	}

	/**
	 * @return the freezerMaxTemperature
	 */
	public float getFreezerMaxTemperature() {
		return this.freezerMaxTemperature;
	}

	/**
	 * @return the freezerMinTemperature
	 */
	public float getFreezerMinTemperature() {
		return this.freezerMinTemperature;
	}

	private int getFreezerState() {
		if (this.lastApplianceInfo == null) {
			return MieleApplianceConstants.MA_STATE_UNKNOWN;
		}
		return ((MieleFridgeFreezerInfoMessage) this.lastApplianceInfo).getFreezerState();
	}

	/**
	 * @return TODO
	 */
	public float getFreezerTargetTemperature() {
		return this.freezerTargetTemperature;
	}

	private Float getFreezerTemperature() {
		return ((MieleFridgeFreezerInfoMessage) this.lastApplianceInfo).getFreezerTemperature();
	}

	/**
	 * @return the fridgeMaxTemperature
	 */
	public float getFridgeMaxTemperature() {
		return this.fridgeMaxTemperature;
	}

	/**
	 * @return the fridgeMinTemperature
	 */
	public float getFridgeMinTemperature() {
		return this.fridgeMinTemperature;
	}

	private int getFridgeState() {
		if (this.lastApplianceInfo == null) {
			return MieleApplianceConstants.MA_STATE_UNKNOWN;
		}
		return ((MieleFridgeFreezerInfoMessage) this.lastApplianceInfo).getRefrigeratorState();
	}

	/**
	 * @return TODO
	 */
	public float getFridgeTargetTemperature() {
		return this.fridgeTargetTemperature;
	}

	private Float getFridgeTemperature() {
		return ((MieleFridgeFreezerInfoMessage) this.lastApplianceInfo).getRefrigeratorTemperature();
	}

	/**
	 * Perform actions on the appliance depending on the following data last bid
	 * price, current price, current freezer temperature, current freezer
	 * operational status (SuperFrost and/or SuperCool on/off).
	 */
	@Override
	protected synchronized void performApplianceStateChange() {

		// Do nothing when no price info has been received or
		// last bid been sent.

		PriceInfo lastPriceInfo = getLastPriceInfo();
		if (this.lastApplianceInfo != null && lastPriceInfo != null && getLastBid() != null
				&& lastPriceInfo.getNormalizedPrice() > 0) {

			if (getApplianceState() == MieleApplianceConstants.MA_STATE_OFF
					|| getApplianceState() == MieleApplianceConstants.MA_STATE_UNKNOWN) {
				logInfo("No state change can be performed. Appliance is "
						+ MieleApplianceUtil.getStateDescription(getLanguageCode(), getApplianceState()));
			} else {
				// Perform Refrigerator state change (SuperCool on/off)
				if (getFridgeState() != MieleApplianceConstants.MA_STATE_OFF
						&& getFridgeState() != MieleApplianceConstants.MA_STATE_UNKNOWN) {
					performFridgeStateChange();
				} else {
					logInfo("No refrigerator change can be performed. Freezer state is "
							+ MieleApplianceUtil.getStateDescription(getLanguageCode(), getFridgeState()));
				}

				// Perform Refrigerator state change (SuperFrost on/off)
				if (getFreezerState() != MieleApplianceConstants.MA_STATE_OFF
						&& getFreezerState() != MieleApplianceConstants.MA_STATE_UNKNOWN) {
					performFreezerStateChange();
				} else {
					logInfo("No freezer change can be performed. Freezer state is "
							+ MieleApplianceUtil.getStateDescription(getLanguageCode(), getFreezerState()));
				}
			}
		} else {
			// No state change possible. Log warning.
			String msg = "No appliance state change can be initiated. Reason:";
			if (lastPriceInfo == null) {
				msg += " No last price info available.";
			}
			if (getLastBid() == null) {
				msg += " No last bid available. ";
			}
			if (this.lastApplianceInfo == null) {
				msg += " No appliance info available. ";
			}
			if (lastPriceInfo != null && lastPriceInfo.getNormalizedPrice() == 0) {
				msg += " Market price is 0. ";
			}
			logWarning(msg);
		}
	}

	/**
	 * Perform actions on the freezer depending on the following data last bid
	 * price, current price, current freezer temperature, current freezer
	 * operational status (SuperFrost on/off).
	 */
	private void performFreezerStateChange() {
		String action = null;
		Date currentTimeStamp = new Date(getCurrentTimeMillis());
		// TODO: Simplify this method
		logInfo("Freezer state:" + getStateDescription(getFreezerState()) + " temperature: " + getFreezerTemperature()
				+ " agent target: " + getFreezerTargetTemperature());

		// Freezer
		PriceInfo lastPriceInfo = getLastPriceInfo();
		if (getFreezerTemperature() > getFreezerTargetTemperature()
				&& lastPriceInfo.getNormalizedPrice() <= this.lastFreezerBidPrice
				&& getFreezerState() != MieleApplianceConstants.MA_STATE_SUPERFROST) {

			// Switch ON SuperFrost: If current temperature is lower than target
			// temperature, current price is lower
			// than bid price and SuperFrost is currently OFF.
			action = MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_ON;

			// Store temperature since start of SuperFrost
			this.temperatureAtStartSuperFrost = getFreezerTemperature();

			// Log info
			logInfo("Switching ON SuperFrost. Reason: price " + lastPriceInfo.getNormalizedPrice() + " <= "
					+ this.lastFreezerBidPrice + "(freezer bid) and current temp " + getFreezerTemperature() + " > target "
					+ getFreezerTargetTemperature());

		} else if (getFreezerTemperature() <= (getFreezerTargetTemperature() + MieleFridgeFreezerDeviceAgentConfiguration.SUPERFROST_OFF_TARGET_MARGIN)
				&& getFreezerState() == MieleApplianceConstants.MA_STATE_SUPERFROST
				&& (this.lastAgentDefinedFreezerState == MieleApplianceConstants.MA_STATE_UNKNOWN || (this.lastAgentDefinedFreezerState == MieleApplianceConstants.MA_STATE_SUPERFROST
						&& this.timeStampStartSuperFrost != null && (currentTimeStamp.getTime() - this.timeStampStartSuperFrost
						.getTime()) / 1000 > getProperty(
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERFROST,
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERFROST_DEFAULT)))) {

			// Switch OFF SuperFrost: if the target temperature plus a margin
			// has been reached and SuperFrost
			// is currently ON AND SuperFrost has been switched on by the agent.
			action = MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_OFF;

			// Log info
			logInfo("Switching OFF SuperFrost. Reason: temperature reached target + margin (" + getFreezerTargetTemperature()
					+ " + " + MieleFridgeFreezerDeviceAgentConfiguration.SUPERFROST_OFF_TARGET_MARGIN + ")");
		} else if (getFreezerTemperature() - this.temperatureAtStartSuperFrost <= MieleFridgeFreezerDeviceAgentConfiguration.SUPERFROST_OFF_MINIMUM_CHANGE
				&& lastPriceInfo.getNormalizedPrice() > this.lastFreezerBidPrice
				&& getFreezerState() == MieleApplianceConstants.MA_STATE_SUPERFROST
				&& (this.lastAgentDefinedFreezerState == MieleApplianceConstants.MA_STATE_UNKNOWN || (this.lastAgentDefinedFreezerState == MieleApplianceConstants.MA_STATE_SUPERFROST
						&& this.timeStampStartSuperFrost != null && (currentTimeStamp.getTime() - this.timeStampStartSuperFrost
						.getTime()) / 1000 > getProperty(
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERFROST,
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERFROST_DEFAULT)))) {

			// Switch OFF SuperFrost: if the price is higher than the bid price,
			// the temperature
			// has been decreased by at least SUPERFROST_OFF_MINIMUM_CHANGE,
			// SuperFrost is currently ON AND SuperFrost has been switched on by
			// the agent
			// AND has been running for at least a defined period
			// (MIELE_MIN_DURATION_SUPERFROST).
			action = MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_OFF;

			// Log info
			logInfo("Switching OFF SuperFrost. Reason: price " + lastPriceInfo.getNormalizedPrice() + " > bid "
					+ this.lastFreezerBidPrice + " (freezer bid) and temp decrease "
					+ (getFreezerTemperature() - this.temperatureAtStartSuperFrost));
		} else if (((getFreezerTemperature() <= (getFreezerTargetTemperature() + MieleFridgeFreezerDeviceAgentConfiguration.SUPERFROST_OFF_TARGET_MARGIN)) || (getFreezerTemperature()
				- this.temperatureAtStartSuperFrost <= MieleFridgeFreezerDeviceAgentConfiguration.SUPERFROST_OFF_MINIMUM_CHANGE && lastPriceInfo
				.getNormalizedPrice() > this.lastFreezerBidPrice))
				&& getFreezerState() == MieleApplianceConstants.MA_STATE_SUPERFROST
				&& (this.lastAgentDefinedFreezerState != MieleApplianceConstants.MA_STATE_SUPERFROST && this.lastAgentDefinedFreezerState != MieleApplianceConstants.MA_STATE_UNKNOWN)) {

			// Human intervention detected: user has switched on SuperFrost. Log
			// warning and perform no action.
			logWarning("Human intervention detected. Device " + getApplianceID() + " function SuperFrost switched on by user.");
		}

		// Perform the action
		if (action != null) {
			ActionResult result = performApplianceAction(action, null, null);
			if (result == ActionResult.SUCCESS) {

				// Action succeeded. Store the state initiated by the agent to
				// detect manual intervention.
				if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_ON)) {

					// New state is defined by agent
					this.lastAgentDefinedFreezerState = MieleApplianceConstants.MA_STATE_SUPERFROST;
					this.timeStampStartSuperFrost = new Date(getCurrentTimeMillis());
				} else {
					// Refrigerator SuperFrost function off; running in normal
					// mode
					this.lastAgentDefinedFreezerState = MieleApplianceConstants.MA_STATE_ON;
					this.timeStampStartSuperFrost = null;
				}
			} else if (result == ActionResult.UNKNOWN) {
				this.lastAgentDefinedFreezerState = MieleApplianceConstants.MA_STATE_UNKNOWN;
			}
		}
	}

	/**
	 * Perform actions on the refrigerator depending on the following data last
	 * bid price, current price, current refrigerator temperature, current
	 * refrigerator operational status (SuperCool on/off).
	 */
	private void performFridgeStateChange() {
		String action = null;
		Date currentTimeStamp = new Date(getCurrentTimeMillis());
		// TODO: Simplify this method
		logInfo("Fridge state:" + MieleApplianceUtil.getStateDescription(getLanguageCode(), getFridgeState())
				+ " temperature: " + getFridgeTemperature() + " agent target: " + getFridgeTargetTemperature());

		// Refrigerator
		PriceInfo lastPriceInfo = getLastPriceInfo();
		if (getFridgeTemperature() > getFridgeTargetTemperature()
				&& lastPriceInfo.getNormalizedPrice() <= this.lastFridgeBidPrice
				&& getFridgeState() != MieleApplianceConstants.MA_STATE_SUPERCOOL) {

			// Switch ON SuperCool: If current temperature is higher than target
			// temperature, current price is lower
			// than bid price and SuperCool is currently OFF.
			action = MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_ON;

			// Store temperature since start of SuperFrost
			this.temperatureAtStartSuperCool = getFridgeTemperature();

			// Log info
			logInfo("Switching ON SuperCool. Reason: price " + lastPriceInfo.getNormalizedPrice() + " <= "
					+ this.lastFridgeBidPrice + "(fridge bid) and current temp " + getFridgeTemperature() + " > target "
					+ getFridgeTargetTemperature());
		} else if (getFridgeTemperature() <= (getFridgeTargetTemperature() + MieleFridgeFreezerDeviceAgentConfiguration.SUPERCOOL_OFF_TARGET_MARGIN)
				&& getFridgeState() == MieleApplianceConstants.MA_STATE_SUPERCOOL
				&& (this.lastAgentDefinedRefrigeratorState == MieleApplianceConstants.MA_STATE_UNKNOWN || (this.lastAgentDefinedRefrigeratorState == MieleApplianceConstants.MA_STATE_SUPERCOOL
						&& this.timeStampStartSuperCool != null && (currentTimeStamp.getTime() - this.timeStampStartSuperCool
						.getTime()) / 1000 > getProperty(
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERCOOL,
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERCOOL_DEFAULT)))) {

			// Switch OFF SuperCool: if the target temperature plus a margin has
			// been reached and SuperCool
			// is currently ON and has not been switched on by human
			// intervention AND SuperCool has been
			// running at least for a defined time
			// (MIELE_MIN_DURATION_SUPERCOOL).
			action = MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_OFF;

			// Log info
			logInfo("Switching OFF SuperCool. Reason: temperature reached target + margin (" + getFridgeTargetTemperature()
					+ " + " + MieleFridgeFreezerDeviceAgentConfiguration.SUPERCOOL_OFF_TARGET_MARGIN + ")");
		} else if (getFridgeTemperature() - this.temperatureAtStartSuperCool <= MieleFridgeFreezerDeviceAgentConfiguration.SUPERCOOL_OFF_MINIMUM_CHANGE
				&& lastPriceInfo.getNormalizedPrice() > this.lastFridgeBidPrice
				&& getFridgeState() == MieleApplianceConstants.MA_STATE_SUPERCOOL
				&& (this.lastAgentDefinedRefrigeratorState == MieleApplianceConstants.MA_STATE_UNKNOWN || (this.lastAgentDefinedRefrigeratorState == MieleApplianceConstants.MA_STATE_SUPERCOOL
						&& this.timeStampStartSuperCool != null && (currentTimeStamp.getTime() - this.timeStampStartSuperCool
						.getTime()) / 1000 > getProperty(
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERCOOL,
						MieleFridgeFreezerDeviceAgentConfiguration.MIELE_MIN_DURATION_SUPERCOOL_DEFAULT)))) {

			// Switch OFF SuperCool: if the price is higher than the bid price,
			// the temperature
			// has been decreased by at least SUPERCOOL_OFF_MINIMUM_CHANGE and
			// SuperCool is currently ON.
			action = MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_OFF;

			logInfo("Switching OFF SuperCool. Reason: price " + lastPriceInfo.getNormalizedPrice() + " > bid "
					+ this.lastFridgeBidPrice + " (fridge bid) and temp decrease "
					+ (getFridgeTemperature() - this.temperatureAtStartSuperCool));
		} else if (((getFridgeTemperature() - this.temperatureAtStartSuperCool <= MieleFridgeFreezerDeviceAgentConfiguration.SUPERCOOL_OFF_MINIMUM_CHANGE && lastPriceInfo
				.getNormalizedPrice() > this.lastFridgeBidPrice) || (getFridgeTemperature() <= (getFridgeTargetTemperature() + MieleFridgeFreezerDeviceAgentConfiguration.SUPERCOOL_OFF_TARGET_MARGIN)))
				&& getFridgeState() == MieleApplianceConstants.MA_STATE_SUPERCOOL
				&& (this.lastAgentDefinedRefrigeratorState != MieleApplianceConstants.MA_STATE_SUPERCOOL && this.lastAgentDefinedRefrigeratorState != MieleApplianceConstants.MA_STATE_UNKNOWN)) {

			// Human intervention detected: user has switched on SuperCool. Log
			// warning and perform no action.
			logWarning("Human intervention detected. Device " + getApplianceID() + " function SuperCool switched on by user.");
		}

		// Perform the action
		if (action != null) {
			ActionResult result = performApplianceAction(action, null, null);
			if (result == ActionResult.SUCCESS) {

				// Action succeeded. Store the state initiated by the agent to
				// detect manual intervention.
				if (action.equals(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_ON)) {
					// Refrigerator switch to SuperCool by agent.
					this.lastAgentDefinedRefrigeratorState = MieleApplianceConstants.MA_STATE_SUPERCOOL;
					this.timeStampStartSuperCool = new Date(getCurrentTimeMillis());
				} else {
					// Refrigerator SuperCool function off; running in normal
					// mode
					this.lastAgentDefinedRefrigeratorState = MieleApplianceConstants.MA_STATE_ON;
					this.timeStampStartSuperCool = null;
				}
			} else if (result == ActionResult.UNKNOWN) {
				this.lastAgentDefinedRefrigeratorState = MieleApplianceConstants.MA_STATE_UNKNOWN;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.der.agent.miele.at.home.AbstractMieleDeviceAgent#publishStatistics()
	 */
	@Override
	protected void publishStatistics() {
		// Freezer statistics
		Date now = new Date(getCurrentTimeMillis());
		if (this.lastApplianceInfo != null) {
			if (this.lastApplianceInfo instanceof MieleFridgeFreezerInfoMessage) {
				Float freezerTemperature = getFreezerTemperature();
				if (freezerTemperature != null) {
					this.telemetryDataPublisher.publishMeasurementData("freezer_temperature", "C", freezerTemperature, null,
							now);
				}
				this.telemetryDataPublisher.publishStatusData("freezer_status",
						MieleApplianceUtil.getStateDescription(getLanguageCode(), getFreezerState()), now);

				// Refrigerator statistics
				Float fridgeTemperature = getFridgeTemperature();
				if (fridgeTemperature != null) {
					this.telemetryDataPublisher.publishMeasurementData("refrigerator_temperature", "C", fridgeTemperature, null,
							now);
				}
				this.telemetryDataPublisher.publishStatusData("refrigerator_status",
						MieleApplianceUtil.getStateDescription(getLanguageCode(), getFridgeState()), now);
			} else {
				// Only the generic appliance info is received. Publish just
				// overall fridge-freezer status.
				this.telemetryDataPublisher.publishStatusData("freezer_status",
						MieleApplianceUtil.getStateDescription(getLanguageCode(), this.lastApplianceInfo.getApplianceState()),
						now);
			}
		} else {
			logWarning("No appliance info available. Status unknown and measurements cannot be published.");
			String statusUnknown = MieleApplianceUtil.getStateDescription(getLanguageCode(),
					MieleApplianceConstants.MA_STATE_UNKNOWN);
			this.telemetryDataPublisher.publishStatusData("freezer_status", statusUnknown, now);
			this.telemetryDataPublisher.publishStatusData("refrigerator_status", statusUnknown, now);

		}
	}

	/**
	 * Set the appliance to a starting state from where the agent can influence
	 * the behaviour. This is required to distinguish human intervention from
	 * agent initiated transiations.
	 * 
	 * The effect is that when SuperCool or SuperFrost is on, they will be
	 * switched off to reach the initial state.
	 */
	private void resetApplianceState() {

		// Get initial refrigerator/freezer state and continue until we receive
		// it.
		// The check supercool/freeze and switch off when on.
		boolean infoReceived = false;
		MieleGatewayMessage response = null;
		while (!infoReceived) {

			// Get initial refrigerator state
			logInfo("Requesting intial appliance state.");
			response = getApplianceInfo();

			// In case we receive an appliance info response continue otherwise
			// wait and try again.
			if (response instanceof MieleApplianceInfoMessage) {
				// Indicator for appliance action call
				ActionResult fridgeResult = ActionResult.SUCCESS;
				ActionResult freezerResult = ActionResult.SUCCESS;

				// In case we receive only an appliance info message the
				// refrigerator-freezer may be
				// switched off. If we receive a MieleFridgeFreezerInfoMessage
				// we can check if we
				// need to reset the SuperFrost/SuperCool functions to off.
				if (response instanceof MieleFridgeFreezerInfoMessage) {
					MieleFridgeFreezerInfoMessage dwInfo = (MieleFridgeFreezerInfoMessage) response;

					// Switch off SuperCool when on
					logInfo("Initial refrigerator state : "
							+ MieleApplianceUtil.getStateDescription(getLanguageCode(), dwInfo.getRefrigeratorState()));
					this.lastAgentDefinedRefrigeratorState = dwInfo.getRefrigeratorState();
					if (dwInfo.getRefrigeratorState() == MieleApplianceConstants.MA_STATE_SUPERCOOL) {

						if ((fridgeResult = performApplianceAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_OFF,
								null, null)) == ActionResult.SUCCESS) {
							// Switch OFF SuperCool
							logInfo("SuperCool switched OFF.");

							// Initial state set by agent
							this.lastAgentDefinedRefrigeratorState = MieleApplianceConstants.MA_STATE_ON;
						}
					}

					// Switch off SuperFrost when on
					logInfo("Initial freezer state : "
							+ MieleApplianceUtil.getStateDescription(getLanguageCode(), dwInfo.getFreezerState()));
					this.lastAgentDefinedFreezerState = dwInfo.getFreezerState();
					if (dwInfo.getFreezerState() == MieleApplianceConstants.MA_STATE_SUPERFROST) {
						// Switch OFF SuperFrost
						if ((freezerResult = performApplianceAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_OFF,
								null, null)) == ActionResult.SUCCESS) {
							logInfo("SuperFrost switched OFF.");

							// Initial state set by agent
							this.lastAgentDefinedFreezerState = MieleApplianceConstants.MA_STATE_ON;
						}
					}
				}

				// Initialization complete when action call
				// did not fail. If it failed try again.
				infoReceived = (fridgeResult == ActionResult.SUCCESS && freezerResult == ActionResult.SUCCESS ? true : false);
			}

			// Repeat until init is complete
			if (!infoReceived) {
				// TODO Rewrite to do this from a TimerTask
				try {
					Thread.sleep(INIT_RETRY_INTERVAL * 1000l);
				} catch (InterruptedException e) {
				}
			}

		}

	}

	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
	}

	/**
	 * @param freezerMaxTemperature
	 *            the freezerMaxTemperature to set
	 */
	public void setFreezerMaxTemperature(final float freezerMaxTemperature) {
		this.freezerMaxTemperature = freezerMaxTemperature;
	}

	/**
	 * @param freezerMinTemperature
	 *            the freezerMinTemperature to set
	 */
	public void setFreezerMinTemperature(final float freezerMinTemperature) {
		this.freezerMinTemperature = freezerMinTemperature;
	}

	/**
	 * @param freezerTargetTemperature
	 */
	public void setFreezerTargetTemperature(final float freezerTargetTemperature) {
		this.freezerTargetTemperature = freezerTargetTemperature;
	}

	/**
	 * @param fridgeMaxTemperature
	 *            the fridgeMaxTemperature to set
	 */
	public void setFridgeMaxTemperature(final float fridgeMaxTemperature) {
		this.fridgeMaxTemperature = fridgeMaxTemperature;
	}

	/**
	 * @param fridgeMinTemperature
	 *            the fridgeMinTemperature to set
	 */
	public void setFridgeMinTemperature(final float fridgeMinTemperature) {
		this.fridgeMinTemperature = fridgeMinTemperature;
	}

	/**
	 * @param fridgeTargetTemperature
	 */
	public void setFridgeTargetTemperature(final float fridgeTargetTemperature) {
		this.fridgeTargetTemperature = fridgeTargetTemperature;
	}

	/**
	 * Applies the value boundary restrictions on the price.
	 * 
	 * @param price
	 *            The 'unbounded' price.
	 * @return The 'bounded' price.
	 */
	private int toBoundedPrice(final float price) {
		return Math.min(Math.max(Math.round(price), MIN_BID_PRICE), MAX_BID_PRICE);
	}

}
