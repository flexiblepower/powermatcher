package net.powermatcher.der.agent.miele.at.home.config;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface MieleFridgeFreezerDeviceAgentConfiguration extends MieleApplianceConfiguration {

	/** Configuration property: minimal duration SuperCool (in seconds) */
	public static final String MIELE_MIN_DURATION_SUPERCOOL = "min.duration.supercool";

	/** Configuration property: minimal duration SuperFrost (in seconds) */
	public static final String MIELE_MIN_DURATION_SUPERFROST = "min.duration.superfrost";

	/** Configuration property: Refrigerator target temperature property */
	public static final String MIELE_FRIDGE_TARGET_TEMPERATURE = "fridge.temperature";

	/** Configuration property: Refrigerator target temperature property */
	public static final String MIELE_FREEZER_TARGET_TEMPERATURE = "freezer.temperature";

	// Default values
	/**
	 * 
	 */
	public static final float MIELE_FREEZER_TARGET_TEMPERATURE_DEFAULT = -20f;
	/**
	 * 
	 */
	public static final String MIELE_FREEZER_TARGET_TEMPERATURE_DEFAULT_STR = "-20";
	/**
	 * 
	 */
	public static final float MIELE_FREEZER_TARGET_TEMPERATURE_MARGIN = -0.5f;

	/**
	 * 
	 */
	public static final float MIELE_FRIDGE_TARGET_TEMPERATURE_DEFAULT = 5f;
	/**
	 * 
	 */
	public static final String MIELE_FRIDGE_TARGET_TEMPERATURE_DEFAULT_STR = "5";
	/**
	 * 
	 */
	public static final float MIELE_FRIDGE_TARGET_TEMPERATURE_MARGIN = 0.5f;

	/**
	 * Margin for switching off SuperFrost when current temperature is lower
	 * then target temperature.
	 */
	public static final float SUPERFROST_OFF_TARGET_MARGIN = -0.5f;

	/**
	 * Margin for switching off SuperCool when current temperature is lower then
	 * target temperature.
	 */
	public static final float SUPERCOOL_OFF_TARGET_MARGIN = -0.5f;

	/**
	 * Threshold for switching off SuperFrost when current price is higher than
	 * bid price
	 */
	public static final float SUPERFROST_OFF_MINIMUM_CHANGE = -2.0f;

	/**
	 * Threshold for switching off SuperFrost when current price is higher than
	 * bid price
	 */
	public static final float SUPERCOOL_OFF_MINIMUM_CHANGE = -0.5f;

	/** Default value for minimal duration SuperFrost (in seconds) */
	public static final int MIELE_MIN_DURATION_SUPERFROST_DEFAULT = 300;
	/**
	 * 
	 */
	public static final String MIELE_MIN_DURATION_SUPERFROST_DEFAULT_STR = "300";

	/** Default value for minimal duration SuperCool (in seconds) */
	public static final int MIELE_MIN_DURATION_SUPERCOOL_DEFAULT = 300;
	/**
	 * 
	 */
	public static final String MIELE_MIN_DURATION_SUPERCOOL_DEFAULT_STR = "300";

	/**
	 * @return TODO
	 */
	public float freezer_temperature();

	/**
	 * @return TODO
	 */
	public float fridge_temperature();

	/**
	 * @return TODO
	 */
	public int min_duration_supercool();

	/**
	 * @return TODO
	 */
	public int min_duration_superfrost();
}
