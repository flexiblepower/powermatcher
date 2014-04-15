package net.powermatcher.der.agent.miele.at.home.gateway.utils;


import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayConstants;

/**
 * Utility class for interpreting property and values of the xml messages from
 * the Miele Gateway.
 * 
 * @author IBM
 * @version 0.9.0
 */
public class MieleApplianceUtil {

	/**
	 * Returns the appliance class id (int value) corresponding to the class
	 * name (description). The language parameter indicates the class name
	 * description language.
	 * 
	 * @param language
	 *            The language used for the class name.
	 * @param classname
	 *            The class name.
	 * @return The appliance class id.
	 */
	public static int getApplianceClass(final String language, final String classname) {
		if (language != null && language.equals(MieleGatewayConstants.LANGUAGE_ENGLISH) && classname != null) {

			if (classname.equals(MieleApplianceConstants.DISHWASHER_CLASS_NAME_EN)) {
				return MieleApplianceConstants.DISHWASHER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.DRYER_CLASS_NAME_EN)) {
				return MieleApplianceConstants.DRYER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.WASHING_MACHINE_CLASS_NAME_EN)) {
				return MieleApplianceConstants.WASHING_MACHINE_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.CERAMIC_HOB_CLASS_NAME_EN)) {
				return MieleApplianceConstants.CERAMIC_HOB_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.HOOD_CLASS_NAME_EN)) {
				return MieleApplianceConstants.HOOD_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.OVEN_CLASS_NAME_EN)) {
				return MieleApplianceConstants.OVEN_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.STOVE_CLASS_NAME_EN)) {
				return MieleApplianceConstants.STOVE_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.STEAMER_CLASS_NAME_EN)) {
				return MieleApplianceConstants.STEAMER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.INDUCTION_HOB_CLASS_NAME_EN)) {
				return MieleApplianceConstants.INDUCTION_HOB_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.COFFEE_MACHINE_CLASS_NAME_EN)) {
				return MieleApplianceConstants.COFFEE_MACHINE_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_EN)) {
				return MieleApplianceConstants.REFRIGERATOR_FREEZER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.FREEZER_CLASS_NAME_EN)) {
				return MieleApplianceConstants.FREEZER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_EN)) {
				return MieleApplianceConstants.REFRIGERATOR_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_NAME_EN)) {
				return MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_ID;
			}
		} else if (classname != null) {
			if (classname.equals(MieleApplianceConstants.DISHWASHER_CLASS_NAME_DE)) {
				return MieleApplianceConstants.DISHWASHER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.DRYER_CLASS_NAME_DE)) {
				return MieleApplianceConstants.DRYER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.WASHING_MACHINE_CLASS_NAME_DE)) {
				return MieleApplianceConstants.WASHING_MACHINE_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.CERAMIC_HOB_CLASS_NAME_DE)) {
				return MieleApplianceConstants.CERAMIC_HOB_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.HOOD_CLASS_NAME_DE)) {
				return MieleApplianceConstants.HOOD_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.OVEN_CLASS_NAME_DE)) {
				return MieleApplianceConstants.OVEN_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.STOVE_CLASS_NAME_DE)) {
				return MieleApplianceConstants.STOVE_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.STEAMER_CLASS_NAME_DE)) {
				return MieleApplianceConstants.STEAMER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.INDUCTION_HOB_CLASS_NAME_DE)) {
				return MieleApplianceConstants.INDUCTION_HOB_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.COFFEE_MACHINE_CLASS_NAME_DE)) {
				return MieleApplianceConstants.COFFEE_MACHINE_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_DE)) {
				return MieleApplianceConstants.REFRIGERATOR_FREEZER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.FREEZER_CLASS_NAME_DE)) {
				return MieleApplianceConstants.FREEZER_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_DE)) {
				return MieleApplianceConstants.REFRIGERATOR_CLASS_ID;
			}
			if (classname.equals(MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_NAME_DE)) {
				return MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_ID;
			}
		}

		return -1;
	}

	/**
	 * Returns the appliance class name (description) corresponding to the class
	 * id in the specified language.
	 * 
	 * @param language
	 *            The language used for the class name.
	 * @param classId
	 *            The appliance class id.
	 * @return The class name (description of the class id).
	 */
	public static String getApplianceClassName(final String language, final int classId) {
		String name = null;

		if (language != null && language.equals(MieleGatewayConstants.LANGUAGE_ENGLISH)) {
			// Select English class name
			switch (classId) {
			case MieleApplianceConstants.DISHWASHER_CLASS_ID:
				name = MieleApplianceConstants.DISHWASHER_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.DRYER_CLASS_ID:
				name = MieleApplianceConstants.DRYER_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.WASHING_MACHINE_CLASS_ID:
				name = MieleApplianceConstants.WASHING_MACHINE_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.CERAMIC_HOB_CLASS_ID:
				name = MieleApplianceConstants.CERAMIC_HOB_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.HOOD_CLASS_ID:
				name = MieleApplianceConstants.HOOD_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.OVEN_CLASS_ID:
				name = MieleApplianceConstants.OVEN_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.STOVE_CLASS_ID:
				name = MieleApplianceConstants.STOVE_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.STEAMER_CLASS_ID:
				name = MieleApplianceConstants.STEAMER_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.INDUCTION_HOB_CLASS_ID:
				name = MieleApplianceConstants.INDUCTION_HOB_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.COFFEE_MACHINE_CLASS_ID:
				name = MieleApplianceConstants.COFFEE_MACHINE_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.REFRIGERATOR_FREEZER_CLASS_ID:
				name = MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.FREEZER_CLASS_ID:
				name = MieleApplianceConstants.FREEZER_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.REFRIGERATOR_CLASS_ID:
				name = MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_EN;
				break;
			case MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_ID:
				name = MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_NAME_EN;
				break;

			default:
				name = "UNKNOWN";
			}
		} else {
			// Select default (German) class name
			switch (classId) {
			case MieleApplianceConstants.DISHWASHER_CLASS_ID:
				name = MieleApplianceConstants.DISHWASHER_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.DRYER_CLASS_ID:
				name = MieleApplianceConstants.DRYER_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.WASHING_MACHINE_CLASS_ID:
				name = MieleApplianceConstants.WASHING_MACHINE_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.CERAMIC_HOB_CLASS_ID:
				name = MieleApplianceConstants.CERAMIC_HOB_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.HOOD_CLASS_ID:
				name = MieleApplianceConstants.HOOD_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.OVEN_CLASS_ID:
				name = MieleApplianceConstants.OVEN_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.STOVE_CLASS_ID:
				name = MieleApplianceConstants.STOVE_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.STEAMER_CLASS_ID:
				name = MieleApplianceConstants.STEAMER_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.INDUCTION_HOB_CLASS_ID:
				name = MieleApplianceConstants.INDUCTION_HOB_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.COFFEE_MACHINE_CLASS_ID:
				name = MieleApplianceConstants.COFFEE_MACHINE_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.REFRIGERATOR_FREEZER_CLASS_ID:
				name = MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.FREEZER_CLASS_ID:
				name = MieleApplianceConstants.FREEZER_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.REFRIGERATOR_CLASS_ID:
				name = MieleApplianceConstants.REFRIGERATOR_CLASS_NAME_DE;
				break;
			case MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_ID:
				name = MieleApplianceConstants.WINE_REFRIGERATOR_CLASS_NAME_DE;
				break;
			default:
				name = "UNKNOWN";
			}
		}

		//
		return name;
	}

	/**
	 * Returns the property code corresponding with the specified key. The
	 * language specifies the key language.
	 * 
	 * @param key
	 *            The property key.
	 * @param language
	 *            The language the key is in.
	 * @return The property code (int value).
	 */
	public static int getInformationProperty(final String key, final String language) {

		String l = (language == null) ? MieleGatewayConstants.LANGUAGE_GERMAN : language;

		if (l.equals(MieleGatewayConstants.LANGUAGE_GERMAN)) {
			if (key.equals(MieleApplianceConstants.APPLIANCE_CLASS_DE)) {
				return MieleApplianceConstants.APPLIANCE_CLASS;
			}
			if (key.equals(MieleApplianceConstants.APPLIANCE_STATE_DE)) {
				return MieleApplianceConstants.APPLIANCE_STATE;
			}
			if (key.equals(MieleApplianceConstants.REFRIGERATOR_STATE_DE)) {
				return MieleApplianceConstants.REFRIGERATOR_STATE;
			}
			if (key.equals(MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE_DE)) {
				return MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE_DE)) {
				return MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.FREEZER_STATE_DE)) {
				return MieleApplianceConstants.FREEZER_STATE;
			}
			if (key.equals(MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE_DE)) {
				return MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE_DE)) {
				return MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_START_TIME_DE)) {
				return MieleApplianceConstants.DISHWASHER_START_TIME;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_REMAINING_TIME_DE)) {
				return MieleApplianceConstants.DISHWASHER_REMAINING_TIME;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_PROGRAM_DE)) {
				return MieleApplianceConstants.DISHWASHER_PROGRAM;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_PHASE_DE)) {
				return MieleApplianceConstants.DISHWASHER_PHASE;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_DURATION_DE)) {
				return MieleApplianceConstants.DISHWASHER_DURATION;
			}
		} else if (l.equals(MieleGatewayConstants.LANGUAGE_ENGLISH)) {
			if (key.equals(MieleApplianceConstants.APPLIANCE_CLASS_EN)) {
				return MieleApplianceConstants.APPLIANCE_CLASS;
			}
			if (key.equals(MieleApplianceConstants.APPLIANCE_STATE_EN)) {
				return MieleApplianceConstants.APPLIANCE_STATE;
			}
			if (key.equals(MieleApplianceConstants.REFRIGERATOR_STATE_EN)) {
				return MieleApplianceConstants.REFRIGERATOR_STATE;
			}
			if (key.equals(MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE_EN)) {
				return MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE_EN)) {
				return MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.FREEZER_STATE_EN)) {
				return MieleApplianceConstants.FREEZER_STATE;
			}
			if (key.equals(MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE_EN)) {
				return MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE_EN)) {
				return MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_START_TIME_EN)) {
				return MieleApplianceConstants.DISHWASHER_START_TIME;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_REMAINING_TIME_EN)) {
				return MieleApplianceConstants.DISHWASHER_REMAINING_TIME;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_PROGRAM_EN)) {
				return MieleApplianceConstants.DISHWASHER_PROGRAM;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_PHASE_EN)) {
				return MieleApplianceConstants.DISHWASHER_PHASE;
			}
			if (key.equals(MieleApplianceConstants.DISHWASHER_DURATION_EN)) {
				return MieleApplianceConstants.DISHWASHER_DURATION;
			}
		}

		return MieleApplianceConstants.UNKNOWN_INFORMATION_KEY;
	}

	/**
	 * Returns the property code corresponding with the specified key. The
	 * language specifies the key language.
	 * @param property 
	 *            The property key.
	 * @param language
	 *            The language the key is in.
	 * @return The property code (int value).
	 */
	public static String getInformationPropertyName(final int property, final String language) {

		String l = (language == null) ? MieleGatewayConstants.LANGUAGE_GERMAN : language;

		if (l.equals(MieleGatewayConstants.LANGUAGE_GERMAN)) {
			switch (property) {
			case MieleApplianceConstants.APPLIANCE_CLASS:
				return MieleApplianceConstants.APPLIANCE_CLASS_DE;
			case MieleApplianceConstants.APPLIANCE_STATE:
				return MieleApplianceConstants.APPLIANCE_STATE_DE;
			case MieleApplianceConstants.REFRIGERATOR_STATE:
				return MieleApplianceConstants.REFRIGERATOR_STATE_DE;
			case MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE:
				return MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE_DE;
			case MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE:
				return MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE_DE;
			case MieleApplianceConstants.FREEZER_STATE:
				return MieleApplianceConstants.FREEZER_STATE_DE;
			case MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE:
				return MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE_DE;
			case MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE:
				return MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE_DE;
			case MieleApplianceConstants.DISHWASHER_START_TIME:
				return MieleApplianceConstants.DISHWASHER_START_TIME_DE;
			case MieleApplianceConstants.DISHWASHER_REMAINING_TIME:
				return MieleApplianceConstants.DISHWASHER_REMAINING_TIME_DE;
			case MieleApplianceConstants.DISHWASHER_PROGRAM:
				return MieleApplianceConstants.DISHWASHER_PROGRAM_DE;
			case MieleApplianceConstants.DISHWASHER_PHASE:
				return MieleApplianceConstants.DISHWASHER_PHASE_DE;
			case MieleApplianceConstants.DISHWASHER_DURATION:
				return MieleApplianceConstants.DISHWASHER_DURATION_DE;
			}
		} else if (l.equals(MieleGatewayConstants.LANGUAGE_ENGLISH)) {
			switch (property) {
			case MieleApplianceConstants.APPLIANCE_CLASS:
				return MieleApplianceConstants.APPLIANCE_CLASS_EN;
			case MieleApplianceConstants.APPLIANCE_STATE:
				return MieleApplianceConstants.APPLIANCE_STATE_EN;
			case MieleApplianceConstants.REFRIGERATOR_STATE:
				return MieleApplianceConstants.REFRIGERATOR_STATE_EN;
			case MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE:
				return MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE_EN;
			case MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE:
				return MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE_EN;
			case MieleApplianceConstants.FREEZER_STATE:
				return MieleApplianceConstants.FREEZER_STATE_EN;
			case MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE:
				return MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE_EN;
			case MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE:
				return MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE_EN;
			case MieleApplianceConstants.DISHWASHER_START_TIME:
				return MieleApplianceConstants.DISHWASHER_START_TIME_EN;
			case MieleApplianceConstants.DISHWASHER_REMAINING_TIME:
				return MieleApplianceConstants.DISHWASHER_REMAINING_TIME_EN;
			case MieleApplianceConstants.DISHWASHER_PROGRAM:
				return MieleApplianceConstants.DISHWASHER_PROGRAM_EN;
			case MieleApplianceConstants.DISHWASHER_PHASE:
				return MieleApplianceConstants.DISHWASHER_PHASE_EN;
			case MieleApplianceConstants.DISHWASHER_DURATION:
				return MieleApplianceConstants.DISHWASHER_DURATION_EN;
			}
		}

		return null;
	}

	/**
	 * Returns the state code (int value) corresponding to the state
	 * description. The language of the description is specified by a parameter.
	 * If no language is specified the default language is considered to be
	 * German.
	 * 
	 * @param language
	 *            The description language.
	 * @param stateString
	 *            The appliance state description.
	 * @return The appliance state code.
	 */
	/**
	 * @param language
	 * @param stateString
	 * @return TODO
	 */
	public static int getState(final String language, final String stateString) {

		if (language != null && language.equals(MieleGatewayConstants.LANGUAGE_ENGLISH) && stateString != null) {
			// English state definitions
			if (stateString.equals(MieleApplianceConstants.STATE_OFF_EN)) {
				return MieleApplianceConstants.MA_STATE_OFF;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_READY_EN)) {
				return MieleApplianceConstants.MA_STATE_READY;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_PROGRAM_EN)) {
				return MieleApplianceConstants.MA_STATE_PROGRAM;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_WAITING_EN)) {
				return MieleApplianceConstants.MA_STATE_WAITING;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_ON_EN)) {
				return MieleApplianceConstants.MA_STATE_ON;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_PAUSED_EN)) {
				return MieleApplianceConstants.MA_STATE_PAUSED;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_END_EN)) {
				return MieleApplianceConstants.MA_STATE_END;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_ERROR_EN)) {
				return MieleApplianceConstants.MA_STATE_ERROR;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_HALTED_EN)) {
				return MieleApplianceConstants.MA_STATE_HALTED;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_SERVICE_EN)) {
				return MieleApplianceConstants.MA_STATE_SERVICE;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_SUPERFROST_EN)) {
				return MieleApplianceConstants.MA_STATE_SUPERFROST;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_SUPERCOOL_EN)) {
				return MieleApplianceConstants.MA_STATE_SUPERCOOL;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_DEFAULT_EN)) {
				return MieleApplianceConstants.MA_STATE_DEFAULT;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_LOCKED_EN)) {
				return MieleApplianceConstants.MA_STATE_LOCKED;
			}
		} else if (stateString != null) {
			// Default states (German)
			if (stateString.equals(MieleApplianceConstants.STATE_OFF_DE)) {
				return MieleApplianceConstants.MA_STATE_OFF;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_READY_DE)) {
				return MieleApplianceConstants.MA_STATE_READY;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_PROGRAM_DE)) {
				return MieleApplianceConstants.MA_STATE_PROGRAM;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_WAITING_DE)) {
				return MieleApplianceConstants.MA_STATE_WAITING;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_ON_DE)) {
				return MieleApplianceConstants.MA_STATE_ON;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_PAUSED_DE)) {
				return MieleApplianceConstants.MA_STATE_PAUSED;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_END_DE)) {
				return MieleApplianceConstants.MA_STATE_END;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_ERROR_DE)) {
				return MieleApplianceConstants.MA_STATE_ERROR;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_HALTED_DE)) {
				return MieleApplianceConstants.MA_STATE_HALTED;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_SERVICE_DE)) {
				return MieleApplianceConstants.MA_STATE_SERVICE;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_SUPERFROST_DE)) {
				return MieleApplianceConstants.MA_STATE_SUPERFROST;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_SUPERCOOL_DE)) {
				return MieleApplianceConstants.MA_STATE_SUPERCOOL;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_DEFAULT_DE)) {
				return MieleApplianceConstants.MA_STATE_DEFAULT;
			}
			if (stateString.equals(MieleApplianceConstants.STATE_VALUE_LOCKED_DE)) {
				return MieleApplianceConstants.MA_STATE_LOCKED;
			}
		}
		// State unknown
		return MieleApplianceConstants.MA_STATE_UNKNOWN;
	}

	/**
	 * Returns the state description (value) corresponding to the state in the
	 * language specified by the parameter. If no language is specified the
	 * default language is German.
	 * 
	 * @param language
	 *            The description language.
	 * @param state
	 *            The appliance state.
	 * @return The appliance state description in the specified language.
	 */
	public static String getStateDescription(final String language, final int state) {
		String description = null;

		if (language != null && language.equals(MieleGatewayConstants.LANGUAGE_ENGLISH)) {
			switch (state) {
			case 1:
				description = MieleApplianceConstants.STATE_OFF_EN;
				break;
			case 2:
				description = MieleApplianceConstants.STATE_VALUE_READY_EN;
				break;
			case 3:
				description = MieleApplianceConstants.STATE_VALUE_PROGRAM_EN;
				break;
			case 4:
				description = MieleApplianceConstants.STATE_VALUE_WAITING_EN;
				break;
			case 5:
				description = MieleApplianceConstants.STATE_VALUE_ON_EN;
				break;
			case 6:
				description = MieleApplianceConstants.STATE_VALUE_PAUSED_EN;
				break;
			case 7:
				description = MieleApplianceConstants.STATE_VALUE_END_EN;
				break;
			case 8:
				description = MieleApplianceConstants.STATE_VALUE_ERROR_EN;
				break;
			case 9:
				description = MieleApplianceConstants.STATE_VALUE_HALTED_EN;
				break;
			case 12:
				description = MieleApplianceConstants.STATE_VALUE_SERVICE_EN;
				break;
			case 13:
				description = MieleApplianceConstants.STATE_VALUE_SUPERFROST_EN;
				break;
			case 14:
				description = MieleApplianceConstants.STATE_VALUE_SUPERCOOL_EN;
				break;
			case 144:
				description = MieleApplianceConstants.STATE_VALUE_DEFAULT_EN;
				break;
			case 145:
				description = MieleApplianceConstants.STATE_VALUE_LOCKED_EN;
				break;
			default:
				description = "UNKNOWN";
			}
		} else {
			// Default language (German)
			switch (state) {
			case 1:
				description = MieleApplianceConstants.STATE_OFF_DE;
				break;
			case 2:
				description = MieleApplianceConstants.STATE_VALUE_READY_DE;
				break;
			case 3:
				description = MieleApplianceConstants.STATE_VALUE_PROGRAM_DE;
				break;
			case 4:
				description = MieleApplianceConstants.STATE_VALUE_WAITING_DE;
				break;
			case 5:
				description = MieleApplianceConstants.STATE_VALUE_ON_DE;
				break;
			case 6:
				description = MieleApplianceConstants.STATE_VALUE_PAUSED_DE;
				break;
			case 7:
				description = MieleApplianceConstants.STATE_VALUE_END_DE;
				break;
			case 8:
				description = MieleApplianceConstants.STATE_VALUE_ERROR_DE;
				break;
			case 9:
				description = MieleApplianceConstants.STATE_VALUE_HALTED_DE;
				break;
			case 12:
				description = MieleApplianceConstants.STATE_VALUE_SERVICE_DE;
				break;
			case 13:
				description = MieleApplianceConstants.STATE_VALUE_SUPERFROST_DE;
				break;
			case 14:
				description = MieleApplianceConstants.STATE_VALUE_SUPERCOOL_DE;
				break;
			case 144:
				description = MieleApplianceConstants.STATE_VALUE_DEFAULT_DE;
				break;
			case 145:
				description = MieleApplianceConstants.STATE_VALUE_LOCKED_DE;
				break;
			default:
				description = "UNKNOWN";
			}
		}
		return description;
	}
}
