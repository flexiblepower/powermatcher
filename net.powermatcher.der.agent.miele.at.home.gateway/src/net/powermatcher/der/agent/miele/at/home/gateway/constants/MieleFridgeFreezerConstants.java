package net.powermatcher.der.agent.miele.at.home.gateway.constants;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface MieleFridgeFreezerConstants {

	// Actions
	// public static final String FRIDGE_ACTION_SUPERCOOL = "supercool";
	// public static final String FREEZER_ACTION_SUPERFROST = "superfrost";

	//
	// Freezer constants
	//
	/**
	 * 
	 */
	public static final float FREEZER_MAX_TEMPERATURE = -16f;
	/**
	 * 
	 */
	public static final float FREEZER_MIN_TEMPERATURE = -26f;
	/**
	 * 
	 */
	public static final int FREEZER_POWER_CONSUMPTION = 136; // type KFN 9758 iD
																// 3

	// Default target temperature freezer
	/**
	 * 
	 */
	public static final float DEFAULT_FREEZER_TARGET_TEMPERATURE = -16.0f;

	// Default freezer temperature variance for switching on/off
	/**
	 * 
	 */
	public static final float DEFAULT_FREEZER_TEMPERATURE_VARIANCE = 2f;

	//
	// Refrigerator constants
	//
	/**
	 * 
	 */
	public static final float FRIDGE_MAX_TEMPERATURE = 7f;
	/**
	 * 
	 */
	public static final float FRIDGE_MIN_TEMPERATURE = 4f;
	/**
	 * 
	 */
	public static final int FRIDGE_POWER_CONSUMPTION = 200; // Assumption

	// Default target temperature refrigerator
	/**
	 * 
	 */
	public static final float DEFAULT_FRIDGE_TARGET_TEMPERATURE = 5.0f;

	// Default freezer temperature variance for switching on/off
	/**
	 * 
	 */
	public static final float DEFAULT_FRIDGE_TEMPERATURE_VARIANCE = 2f;
}
