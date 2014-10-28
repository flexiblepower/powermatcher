package net.powermatcher.der.agent.miele.at.home;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.der.agent.miele.at.home.config.MieleApplianceConfiguration;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;
import net.powermatcher.der.agent.miele.at.home.msg.MieleDishWasherInfoMessage;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleDishWasherDeviceAgent extends AbstractMieleDeviceAgent {

	private Calendar initialBidTime;
	private Calendar startTime;

	/**
	 * 
	 */
	public MieleDishWasherDeviceAgent() {

	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public MieleDishWasherDeviceAgent(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Calculate the bid price the dishwasher will turn on.
	 * 
	 * @return The bid price
	 */
	@Override
	protected int calculateBidPrice() {
		// TODO Do not assume price ranges from 0 to 127 with step 1. The actual
		// market basis received should be used.

		// Set to minimum (no power required).
		int newPrice = MIN_BID_PRICE;

		// No calculation when appliance info missing. When machine is running
		// send the max price.
		// When machine is programmed calculate the price. In all other cases no
		// power is required and send 0.
		if (this.lastApplianceInfo == null) {
			logWarning("Price cannot be calculated without appliance info. ");
		} else if (this.lastApplianceInfo.getApplianceState() != MieleApplianceConstants.MA_STATE_WAITING) {

			// If dishwasher is running; power is always required. Otherwise
			// minumum bid price is used.
			if (this.lastApplianceInfo.getApplianceState() == MieleApplianceConstants.MA_STATE_ON) {
				newPrice = MAX_BID_PRICE;
			}

			// Reset the initial bid time to start a new linear bid curve when
			// Dishwasher is ON (running) or when it is other than waiting
			// (programmed) to start.
			this.initialBidTime = null;
		} else {

			// Dishwasher is in state 'WAITING', it is programmed and waiting
			// for
			// a start signal which will be triggered if the delayed start time
			// has
			// been completed or if this agent will send a 'Start' command. The
			// start
			// by the agent will depend on the bid price and the market price.
			// Bid
			// price calculation.

			// Determine price by calculation

			// Set next StartTime
			Calendar current = Calendar.getInstance();

			// Set the moment of the first bidding
			if (this.initialBidTime == null) {
				this.initialBidTime = (Calendar) current.clone();
				return 0;
			}

			// Check if start time is set or has been passed
			Date start = ((MieleDishWasherInfoMessage) this.lastApplianceInfo).getStartTime();

			// Exit and return minimum price when no start time is set.
			if (start == null) {
				return MIN_BID_PRICE;
			}

			this.startTime = Calendar.getInstance();
			this.startTime.setTime(start);

			// Calculate the price
			float price = (float) MAX_BID_PRICE * (float) ((current.getTimeInMillis() - this.initialBidTime.getTimeInMillis()))
					/ (this.startTime.getTimeInMillis() - this.initialBidTime.getTimeInMillis());

			if (isDebugEnabled()) {
				// Print debug info
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				logDebug("StartTime:" + df.format(this.startTime.getTime()));
				logDebug("Current:" + df.format(current.getTime()));
				logDebug("Initial:" + df.format(this.initialBidTime.getTime()));

				logDebug("Price calculation >>> Price " + price + " = " + MAX_BID_PRICE + " * "
						+ (float) (current.getTimeInMillis() - this.initialBidTime.getTimeInMillis()) + " / "
						+ (float) (this.startTime.getTimeInMillis() - this.initialBidTime.getTimeInMillis()));

			}
			newPrice = Math.min(Math.max(Math.round(price), 0), MAX_BID_PRICE);
		}

		// Log the the price
		logInfo("Dishwasher calculated overall price=" + newPrice);

		return newPrice;
	}

	@Override
	protected synchronized void performApplianceStateChange() {
		// Do nothing when no price info has been received or
		// last bid been sent.
		int state = getApplianceState();
		PriceInfo lastPriceInfo = getLastPriceInfo();

		if (state == MieleApplianceConstants.MA_STATE_OFF || state == MieleApplianceConstants.MA_STATE_END) {
			// Log message and perform no action
			logInfo("No action. Dishwasher state is : " + MieleApplianceUtil.getStateDescription(getLanguageCode(), state));
		} else if (state != MieleApplianceConstants.MA_STATE_UNKNOWN && lastPriceInfo != null
				&& lastPriceInfo.getCurrentPrice() > 0 && getLastBid() != null && this.lastApplianceInfo != null) {

			logInfo("DW last received price=" + lastPriceInfo.getCurrentPrice() + " last bid=" + getLastBidPrice());

			if (lastPriceInfo.getNormalizedPrice() <= getLastBidPrice() && ( // lastApplianceInfo.getApplianceState()
																				// ==
																				// MieleApplianceConstants.MA_STATE_PROGRAM
																				// ||
					this.lastApplianceInfo.getApplianceState() == MieleApplianceConstants.MA_STATE_WAITING)) {

				logInfo("Sending request to start the dishwasher program");
				performApplianceAction(MieleApplianceConstants.APPLIANCE_ACTION_START, null, null);
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
			if (lastPriceInfo != null && lastPriceInfo.getCurrentPrice() == 0) {
				msg += " Market price is 0. ";
			}
			logWarning(msg);
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

		Date now = new Date(getCurrentTimeMillis());
		if (this.lastApplianceInfo != null) {

			this.telemetryDataPublisher.publishStatusData("dishwasher_status", MieleApplianceUtil.getStateDescription(
					MieleApplianceConfiguration.MIELE_AGENT_LANGUAGE_CODE_DEFAULT, this.lastApplianceInfo.getApplianceState()),
					now);

			if (this.lastApplianceInfo instanceof MieleDishWasherInfoMessage) {
				MieleDishWasherInfoMessage info = (MieleDishWasherInfoMessage) this.lastApplianceInfo;
				if (info.getApplianceState() == MieleApplianceConstants.MA_STATE_ON) {
					if (info.getProgram() != null) {
						this.telemetryDataPublisher.publishStatusData("dishwasher_program", info.getProgram(), now);
						this.telemetryDataPublisher.publishStatusData("dishwasher_remaining_time",
								new Integer(info.getRemainingTime()).toString(), now);
						if (info.getPhase() != null) {
							this.telemetryDataPublisher.publishStatusData("dishwasher_phase", info.getPhase(), now);
						}
					}
				} else if (info.getApplianceState() == MieleApplianceConstants.MA_STATE_PROGRAM) {
					if (info.getProgram() != null) {
						this.telemetryDataPublisher.publishStatusData("dishwasher_program", info.getProgram(), now);
						this.telemetryDataPublisher.publishStatusData("dishwasher_duration",
								new Integer(info.getDuration()).toString(), now);
					}
				} else if (info.getApplianceState() == MieleApplianceConstants.MA_STATE_WAITING) {
					if (info.getProgram() != null) {
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						this.telemetryDataPublisher.publishStatusData("dishwasher_program", info.getProgram(), now);
						this.telemetryDataPublisher.publishStatusData("dishwasher_start_time", df.format(info.getStartTime()),
								now);
						this.telemetryDataPublisher.publishStatusData("dishwasher_duration",
								new Integer(info.getDuration()).toString(), now);
					}
				}

			}
		} else {
			// No appliance info log that status is unknown
			logWarning("No appliance info available. Status unknown and measurements cannot be published.");
			String statusUnknown = MieleApplianceUtil.getStateDescription(getLanguageCode(),
					MieleApplianceConstants.MA_STATE_UNKNOWN);
			this.telemetryDataPublisher.publishStatusData("dishwasher_status", statusUnknown, now);
		}
	}

}
