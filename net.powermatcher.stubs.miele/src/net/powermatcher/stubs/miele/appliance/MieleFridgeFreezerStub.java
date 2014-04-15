package net.powermatcher.stubs.miele.appliance;


import java.util.Date;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleFridgeFreezerConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author IBM
 * @version 1.0.0
 */
public class MieleFridgeFreezerStub extends AbstractMieleAppliance implements IMieleFridgeFreezerStub,
		MieleFridgeFreezerConstants {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		MieleFridgeFreezerStub fridge = new MieleFridgeFreezerStub();
		fridge.setSuperFrost(true);
		fridge.setSuperCool(true);

		do {
			float temperatureFridge = fridge.calculateFridgeTemperature();
			float temperatureFreezer = fridge.calculateFreezerTemperature();

			System.out.println("Fridge(" + MieleApplianceUtil.getStateDescription(null, fridge.getFridgeStatus()) + ")" + "="
					+ temperatureFridge + " supercool=" + fridge.isSuperCool() + " Freezer("
					+ MieleApplianceUtil.getStateDescription(null, fridge.getFreezerStatus()) + ")" + "=" + temperatureFreezer
					+ " superfrost=" + fridge.isSuperFrost());

			// Sleep for 4 seconds
			Thread.sleep(4000);
		} while (true);

	}

	private Logger logger;
	// Default temperature in- or decrease per minute when fridge/freezer is
	// off/on
	private final static float DEFAULT_FRIDGE_COOLING_STEP_PMIN = 2.0f;
	private final static float DEFAULT_FRIDGE_DECOOLING_STEP_PMIN = 2.0f;
	private final static float DEFAULT_FREEZER_COOLING_STEP_PMIN = 5.0f;

	private final static float DEFAULT_FREEZER_DECOOLING_STEP_PMIN = 5.0f;
	// Multiplier for temperature decrease when super cool/frost is on
	private final static float DEFAULT_SUPER_FREEZE_MULTIPLIER = 2.0f;

	private final static float DEFAULT_SUPER_COOL_MULTIPLIER = 2.0f;
	// Refrigerator properties
	private float temperatureFridge;
	private float targetTemperatureFridge;
	private float fridgeTemperatureVariance;
	private int fridgeStatus;

	private boolean superCool;
	// Freezer properties
	private float temperatureFreezer;
	private float targetTemperatureFreezer;
	private float freezerTemperatureVariance;
	private int freezerStatus;

	private boolean superFrost;
	// Fields required for calculation
	private Date lastTemperatureUpdateFridge;
	private Date lastTemperatureUpdateFreezer;
	private float minimumTemperatureFridge;
	private float maximumTemperatureFridge;
	private float minimumTemperatureFreezer;

	private float maximumTemperatureFreezer;

	/**
	 * 
	 */
	public MieleFridgeFreezerStub() {
		super();

		// Initialize
		this.init();

		this.logger = LoggerFactory.getLogger(getClass().getName() + "-" + getId());
	}

	/**
	 * @return TODO
	 */
	public float calculateFreezerTemperature() {

		Date currentDate = new Date();

		// The full cooling and decooling time determine the freeze on/off
		// interval
		float totalCoolingTime = (2 * this.freezerTemperatureVariance) / DEFAULT_FREEZER_COOLING_STEP_PMIN * 60;
		float totalDecoolingTime = (2 * this.freezerTemperatureVariance) / DEFAULT_FREEZER_DECOOLING_STEP_PMIN * 60;

		// The time in the cooling interval is the elapsed time relative to the
		// previous calculation.
		// This is calculated by subtracting the last calculation time from the
		// current modulo the total cool-decool interval.
		float timeInCoolingInterval = ((currentDate.getTime() - this.lastTemperatureUpdateFreezer.getTime()) / 1000)
				% (totalCoolingTime + totalDecoolingTime);
		float numberOfFullCycles = ((currentDate.getTime() - this.lastTemperatureUpdateFreezer.getTime()) / 1000)
				/ (totalCoolingTime + totalDecoolingTime);

		// The new temperature initialize it first
		float temperature = -99;

		// Depending if the last status the freezer was on or off, the
		// calculation should
		// start with an interval starting in a descending or ascending
		// temperature respectively.
		if (getFreezerStatus() == MieleApplianceConstants.MA_STATE_ON
				|| getFreezerStatus() == MieleApplianceConstants.MA_STATE_SUPERFROST) {
			// The original state of the freezer was that it was on. Check if in
			// the
			// interval the cooling part has already been completed

			// The time it takes to reach the minimum temperature
			float sfMultiplier = (this.superFrost ? DEFAULT_SUPER_FREEZE_MULTIPLIER : 1.0f);
			float timeToMinTemp = (this.temperatureFreezer - this.getMinimumTemperatureFreezer())
					/ (DEFAULT_FREEZER_COOLING_STEP_PMIN * sfMultiplier) * 60;

			if (this.superFrost) {
				this.logger.info("SuperFreeze on. Last temperature " + this.temperatureFreezer);
				// SuperFrost will increase to the minimum temperature and stay
				// there
				// until SuperFrost is switched off.
				// timeToMinTemp = (temperatureFreezer -
				// this.getMinimumTemperatureFreezer()) /
				// (DEFAULT_FREEZER_COOLING_STEP_PMIN * sfMultiplier) * 60;
				if (numberOfFullCycles >= 1 || (timeToMinTemp <= timeInCoolingInterval)) {
					temperature = this.getMinimumTemperatureFreezer();
				} else {
					temperature = this.temperatureFreezer
							- (timeInCoolingInterval / 60 * DEFAULT_FREEZER_COOLING_STEP_PMIN * sfMultiplier);

					// Temperature can never go below minimum not even when the
					// variance allows this
					temperature = Math.max(temperature, getMinimumTemperatureFreezer());
				}

			} else if (timeToMinTemp > timeInCoolingInterval) {
				this.logger.info("SuperFreeze off. Cooling. Last temperature " + this.temperatureFreezer);
				// Current time is now in the cooling part of the interval
				temperature = this.temperatureFreezer - (timeInCoolingInterval / 60 * DEFAULT_FREEZER_COOLING_STEP_PMIN);

				// Temperature can never go below minimum not even when the
				// variance allows this
				temperature = Math.max(temperature, getMinimumTemperatureFreezer());
			} else {
				this.logger.info("SuperFreeze off. Decooling. Last temperature " + this.temperatureFreezer);
				// Current time is now in the 'decooling' part of the interval
				// temperature = minimumTemperatureFreezer +
				// (timeInCoolingInterval / 60 *
				// DEFAULT_FREEZER_DECOOLING_STEP_PMIN);
				temperature = this.temperatureFreezer + (timeInCoolingInterval / 60 * DEFAULT_FREEZER_DECOOLING_STEP_PMIN);
				this.freezerStatus = MieleApplianceConstants.MA_STATE_ON;
			}
		} else {
			// The time it takes to reach the max temperature
			float time_to_decool = ((this.maximumTemperatureFreezer) - this.temperatureFreezer)
					/ DEFAULT_FREEZER_DECOOLING_STEP_PMIN * 60;

			if (time_to_decool > timeInCoolingInterval) {
				// Current time is now in the 'decooling' part of the interval
				temperature = this.temperatureFreezer + (timeInCoolingInterval / 60 * DEFAULT_FREEZER_DECOOLING_STEP_PMIN);
			} else {
				// Current time is now in the cooling part of the interval
				temperature = this.maximumTemperatureFreezer + (timeInCoolingInterval / 60 * DEFAULT_FREEZER_COOLING_STEP_PMIN);

				// Temperature can never go below minimum not even when the
				// variance allows this
				temperature = Math.max(temperature, getMinimumTemperatureFreezer());

				this.freezerStatus = MieleApplianceConstants.MA_STATE_ON;
			}
		}

		// Round the temperature to one decimal
		temperature *= 10;
		temperature = (float) Math.round(temperature) / 10;

		// Only set temperature and update time when temperature changes
		if (this.temperatureFreezer != temperature) {
			this.temperatureFreezer = temperature;
			this.lastTemperatureUpdateFreezer = currentDate;
		}

		return temperature;
	}

	/**
	 * @return TODO
	 */
	public float calculateFridgeTemperature() {

		Date currentDate = new Date();

		// The full cooling and decooling time determine the interval for calcu
		float totalCoolingTime = (2 * this.fridgeTemperatureVariance) / DEFAULT_FRIDGE_COOLING_STEP_PMIN * 60;
		float totalDecoolingTime = (2 * this.fridgeTemperatureVariance) / DEFAULT_FRIDGE_DECOOLING_STEP_PMIN * 60;

		// The time in the cooling interval is the elapsed time relative to the
		// previous calculation.
		// This is calculated by subtracting the last calculation time from the
		// current modulo the total cool-decool interval.
		float timeInCoolingInterval = ((currentDate.getTime() - this.lastTemperatureUpdateFridge.getTime()) / 1000)
				% (totalCoolingTime + totalDecoolingTime);
		float numberOfFullCycles = ((currentDate.getTime() - this.lastTemperatureUpdateFridge.getTime()) / 1000)
				/ (totalCoolingTime + totalDecoolingTime);

		// The new temperature initialize it first
		float temperature = -99;

		// Depending if the last status the fridge was on or off, the
		// calculation should
		// start with an interval starting in a descending or ascending
		// temperature respectively.
		if (getFridgeStatus() == MieleApplianceConstants.MA_STATE_ON
				|| getFridgeStatus() == MieleApplianceConstants.MA_STATE_SUPERCOOL) {
			// The original state of the fridge was that it was on. Check if in
			// the
			// interval the cooling part has already been completed

			// The time it takes to reach the minimum temperature
			float scMultiplier = (this.superCool ? DEFAULT_SUPER_COOL_MULTIPLIER : 1.0f);
			float timeToMinTemp = (this.temperatureFridge - this.getMinimumTemperatureFridge())
					/ (DEFAULT_FRIDGE_COOLING_STEP_PMIN * scMultiplier) * 60;

			if (this.superCool) {
				// logger.info("SuperCool on. Last temperature " +
				// temperatureFridge );
				// SuperCool will increase to the minimum temperature and stay
				// there until supercool is switched off
				// timeToMinTemp = (this.temperatureFridge -
				// FRIDGE_MIN_TEMPERATURE) / (DEFAULT_FRIDGE_COOLING_STEP_PMIN *
				// scMultiplier) * 60;
				if (numberOfFullCycles >= 1 || (timeToMinTemp <= timeInCoolingInterval)) {
					// Completed a full cycle to cool down. Set to the minimum
					// temperature
					temperature = this.getMinimumTemperatureFridge();
				} else {
					temperature = this.temperatureFridge
							- (timeInCoolingInterval / 60 * DEFAULT_FREEZER_COOLING_STEP_PMIN * scMultiplier);

					// Temperature can never go below minimum not even when the
					// variance allows this
					temperature = Math.max(temperature, getMinimumTemperatureFridge());
				}

			} else if (timeToMinTemp > timeInCoolingInterval) {
				// Current time is now in the cooling part of the interval
				temperature = this.temperatureFridge - (timeInCoolingInterval / 60 * DEFAULT_FRIDGE_COOLING_STEP_PMIN);

				// Temperature can never go below minimum not even when the
				// variance allows this
				temperature = Math.max(temperature, getMinimumTemperatureFridge());
			} else {
				// Current time is now in the 'decooling' part of the interval
				// float timeToMaxTemp = timeInCoolingInterval - timeToMinTemp;
				// temperature = minimumTemperatureFridge +
				// (timeInCoolingInterval / 60 *
				// DEFAULT_FRIDGE_DECOOLING_STEP_PMIN);
				temperature = this.getMinimumTemperatureFridge()
						+ (timeInCoolingInterval / 60 * DEFAULT_FRIDGE_DECOOLING_STEP_PMIN);
				this.fridgeStatus = MieleApplianceConstants.MA_STATE_ON;
			}
		} else {
			// The time it takes to reach the max temperature
			float time_to_decool = ((this.maximumTemperatureFridge) - this.temperatureFridge)
					/ DEFAULT_FRIDGE_DECOOLING_STEP_PMIN * 60;

			if (time_to_decool > timeInCoolingInterval) {
				// Current time is now in the 'decooling' part of the interval
				temperature = this.temperatureFridge + (timeInCoolingInterval / 60 * DEFAULT_FRIDGE_DECOOLING_STEP_PMIN);
			} else {
				// Current time is now in the cooling part of the interval
				// float timeToMinTemp = timeInCoolingInterval - time_to_decool;
				temperature = this.maximumTemperatureFridge + (timeInCoolingInterval / 60 * DEFAULT_FRIDGE_COOLING_STEP_PMIN);

				// Temperature can never go below minimum not even when the
				// variance allows this
				temperature = Math.max(temperature, getMinimumTemperatureFridge());

				// Current state in cycle is ON
				this.fridgeStatus = MieleApplianceConstants.MA_STATE_ON;
			}
		}

		// Round the temperature to one decimal
		temperature *= 10;
		temperature = (float) Math.round(temperature) / 10;

		// Only set temperature and update time when temperature changes
		if (this.temperatureFridge != temperature) {
			this.temperatureFridge = temperature;
			this.lastTemperatureUpdateFridge = currentDate;
		}

		return temperature;
	}

	@Override
	public int getFreezerStatus() {
		return this.freezerStatus;
	}

	@Override
	public float getFreezerTemperatureVariance() {
		return this.freezerTemperatureVariance;
	}

	@Override
	public int getFridgeStatus() {
		return this.fridgeStatus;
	}

	@Override
	public float getFridgeTemperatureVariance() {
		return this.fridgeTemperatureVariance;
	}

	/**
	 * Returns the user defined target temperature for the freezer. When the
	 * SuperFrost function is on the lowest possible freezer temperature.
	 * 
	 * @return The minimum temperature for the refrigerator.
	 */
	private float getMinimumTemperatureFreezer() {
		if (isSuperFrost()) {
			return FREEZER_MIN_TEMPERATURE;
		} else {
			return this.minimumTemperatureFreezer;
		}
	}

	/**
	 * Returns the user defined target temperature for the refrigerator. When
	 * the SuperCool function is on the lowest possible temperature.
	 * 
	 * @return The minimum temperature for the refrigerator.
	 */
	private float getMinimumTemperatureFridge() {
		if (isSuperCool()) {
			return FRIDGE_MIN_TEMPERATURE;
		} else {
			return this.minimumTemperatureFridge;
		}
	}

	@Override
	public float getTargetTemperatureFreezer() {
		return this.targetTemperatureFreezer;
	}

	@Override
	public float getTargetTemperatureFridge() {
		return this.targetTemperatureFridge;
	}

	@Override
	synchronized public float getTemperatureFreezer() {
		this.calculateFreezerTemperature();
		return this.temperatureFreezer;
	}

	@Override
	synchronized public float getTemperatureFridge() {
		this.calculateFridgeTemperature();
		return this.temperatureFridge;
	}

	private void init() {
		this.targetTemperatureFridge = DEFAULT_FRIDGE_TARGET_TEMPERATURE;
		this.targetTemperatureFreezer = DEFAULT_FREEZER_TARGET_TEMPERATURE;
		setFreezerTemperatureVariance(DEFAULT_FREEZER_TEMPERATURE_VARIANCE);
		setFridgeTemperatureVariance(DEFAULT_FRIDGE_TEMPERATURE_VARIANCE);
		this.lastTemperatureUpdateFridge = new Date();
		this.lastTemperatureUpdateFreezer = new Date();
		this.setState(MieleApplianceConstants.MA_STATE_ON);
		this.fridgeStatus = MieleApplianceConstants.MA_STATE_ON;
		this.freezerStatus = MieleApplianceConstants.MA_STATE_ON;
		this.superFrost = false;
		this.temperatureFridge = MieleGatewayConstants.MA_INITIAL_TEMP_FRIDGE;
		this.temperatureFreezer = MieleGatewayConstants.MA_INITIAL_TEMP_FREEZER;

		updateActions();
	}

	@Override
	public boolean isSuperCool() {
		return this.superCool;
	}

	@Override
	public boolean isSuperFrost() {
		return this.superFrost;
	}

	@Override
	public void setFreezerTemperatureVariance(final float variance) {
		this.freezerTemperatureVariance = variance;

		this.setMinimumTemperatureFreezer(this.targetTemperatureFreezer - variance);
		this.maximumTemperatureFreezer = this.targetTemperatureFreezer + variance;
	}

	@Override
	public void setFridgeTemperatureVariance(final float variance) {
		this.fridgeTemperatureVariance = variance;

		this.setMinimumTemperatureFridge(this.targetTemperatureFridge - variance);
		this.maximumTemperatureFridge = this.targetTemperatureFridge + variance;
	}

	private void setMinimumTemperatureFreezer(final float minimumTemperatureFreezer) {
		this.minimumTemperatureFreezer = minimumTemperatureFreezer;
	}

	private void setMinimumTemperatureFridge(final float minimumTemperatureFridge) {
		this.minimumTemperatureFridge = minimumTemperatureFridge;
	}

	@Override
	public void setSuperCool(final boolean superCool) {
		this.superCool = superCool;

		if (superCool) {
			this.fridgeStatus = MieleApplianceConstants.MA_STATE_SUPERCOOL;

		} else {
			this.fridgeStatus = MieleApplianceConstants.MA_STATE_ON;
		}

		updateActions();
	}

	@Override
	synchronized public void setSuperFrost(final boolean superFrost) {
		this.superFrost = superFrost;

		if (superFrost) {
			this.freezerStatus = MieleApplianceConstants.MA_STATE_SUPERFROST;
		} else {
			this.freezerStatus = MieleApplianceConstants.MA_STATE_ON;
		}
		updateActions();
	}

	@Override
	public void setTargetTemperatureFreezer(final float temperature) {
		this.targetTemperatureFreezer = temperature;
	}

	@Override
	public void setTargetTemperatureFridge(final float temperature) {
		this.targetTemperatureFridge = temperature;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("current temperature = " + this.temperatureFridge);
		sb.append("target temperature = " + this.targetTemperatureFridge);

		return super.toString();
	}

	/**
	 * 
	 */
	public void updateActions() {

		if (getFridgeStatus() == MieleApplianceConstants.MA_STATE_SUPERCOOL) {
			// Update the available actions
			removeAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_ON);
			addAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_OFF);
		} else {
			// Update the available actions
			removeAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_OFF);
			addAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERCOOLING_ON);
		}

		if (getFreezerStatus() == MieleApplianceConstants.MA_STATE_SUPERFROST) {
			// Update the available actions
			removeAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_ON);
			addAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_OFF);
		} else {
			// Update the available actions
			removeAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_OFF);
			addAction(MieleApplianceConstants.APPLIANCE_ACTION_SUPERFROST_ON);
		}
	}

}
