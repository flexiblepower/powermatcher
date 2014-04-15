package net.powermatcher.der.agent.miele.at.home.gateway.constants;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface MieleApplianceConstants {

	// Action names (keys) German
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_START = "Start";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_STOP = "Stop";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERFROST_ON = "SuperFrost Ein";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERFROST_OFF = "SuperFrost Aus";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERCOOLING_ON = "SuperKühlen Ein";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERCOOLING_OFF = "SuperKühlen Aus";

	// Action names (key) English
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_START_EN = "Start";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_STOP_EN = "Stop";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERFROST_ON_EN = "SuperFrost On";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERFROST_OFF_EN = "SuperFrost Off";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERCOOLING_ON_EN = "SuperCooling On";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_SUPERCOOLING_OFF_EN = "SuperCooling Off";

	// Action parameter name in URL
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_PARAM_START = "start";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_PARAM_STOP = "stop";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_PARAM_SUPERFROST_ON = "startSuperFreezing";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_PARAM_SUPERFROST_OFF = "stopSuperFreezing";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_PARAM_SUPERCOOLING_ON = "startSuperCooling";
	/**
	 * 
	 */
	public static final String APPLIANCE_ACTION_PARAM_SUPERCOOLING_OFF = "stopSuperCooling";

	/*
	 * Appliance keys used in the xml information of the gateway appliance info
	 * message.
	 */

	// Appliance information key (make Enum?)
	/**
	 * 
	 */
	public static final int APPLIANCE_CLASS = 1;
	/**
	 * 
	 */
	public static final int APPLIANCE_STATE = 2;
	/**
	 * 
	 */
	public static final int REFRIGERATOR_STATE = 3;
	/**
	 * 
	 */
	public static final int REFRIGERATOR_TARGET_TEMPERATURE = 4;
	/**
	 * 
	 */
	public static final int REFRIGERATOR_CURRENT_TEMPERATURE = 5;
	/**
	 * 
	 */
	public static final int FREEZER_STATE = 6;
	/**
	 * 
	 */
	public static final int FREEZER_TARGET_TEMPERATURE = 7;
	/**
	 * 
	 */
	public static final int FREEZER_CURRENT_TEMPERATURE = 8;
	/**
	 * 
	 */
	public static final int DISHWASHER_REMAINING_TIME = 9;
	/**
	 * 
	 */
	public static final int DISHWASHER_PROGRAM = 10;
	/**
	 * 
	 */
	public static final int DISHWASHER_PHASE = 11;
	/**
	 * 
	 */
	public static final int DISHWASHER_DURATION = 12;
	/**
	 * 
	 */
	public static final int DISHWASHER_START_TIME = 13;

	/**
	 * 
	 */
	public static final int UNKNOWN_INFORMATION_KEY = 0;

	// Appliance info keys (German)
	/**
	 * 
	 */
	public static final String APPLIANCE_CLASS_DE = "Gerät";
	/**
	 * 
	 */
	public static final String APPLIANCE_STATE_DE = "Gerätestatus";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_STATE_DE = "Kühlstatus";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_TARGET_TEMPERATURE_DE = "Ziel-Kühltemperatur";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_CURRENT_TEMPERATURE_DE = "Aktuelle Kühltemperatur";
	/**
	 * 
	 */
	public static final String FREEZER_STATE_DE = "Gefrierstatus";
	/**
	 * 
	 */
	public static final String FREEZER_TARGET_TEMPERATURE_DE = "Ziel-Gefriertemperatur";
	/**
	 * 
	 */
	public static final String FREEZER_CURRENT_TEMPERATURE_DE = "Aktuelle Gefriertemperatur";
	/**
	 * 
	 */
	public static final String DISHWASHER_START_TIME_DE = "Startzeit";
	/**
	 * 
	 */
	public static final String DISHWASHER_REMAINING_TIME_DE = "Restzeit";
	/**
	 * 
	 */
	public static final String DISHWASHER_PROGRAM_DE = "Programm";
	/**
	 * 
	 */
	public static final String DISHWASHER_PHASE_DE = "Phase";
	/**
	 * 
	 */
	public static final String DISHWASHER_DURATION_DE = "Dauer";

	// Appliance info keys (English)
	/**
	 * 
	 */
	public static final String APPLIANCE_CLASS_EN = "Appliance Type";
	/**
	 * 
	 */
	public static final String APPLIANCE_STATE_EN = "State";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_STATE_EN = "Fridge State";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_TARGET_TEMPERATURE_EN = "Fridge Target Temperature";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_CURRENT_TEMPERATURE_EN = "Fridge Current Temperature";
	/**
	 * 
	 */
	public static final String FREEZER_STATE_EN = "Freezer State";
	/**
	 * 
	 */
	public static final String FREEZER_TARGET_TEMPERATURE_EN = "Freezer Target Temperature";
	/**
	 * 
	 */
	public static final String FREEZER_CURRENT_TEMPERATURE_EN = "Freezer Current Temperature";
	/**
	 * 
	 */
	public static final String DISHWASHER_START_TIME_EN = "Start Time";
	/**
	 * 
	 */
	public static final String DISHWASHER_REMAINING_TIME_EN = "Remaining Time";
	/**
	 * 
	 */
	public static final String DISHWASHER_PROGRAM_EN = "Program";
	/**
	 * 
	 */
	public static final String DISHWASHER_PHASE_EN = "Phase";
	/**
	 * 
	 */
	public static final String DISHWASHER_DURATION_EN = "Duration";

	/*
	 * Appliance states
	 */

	// Appliance states
	/**
	 * 
	 */
	public final static int MA_STATE_OFF = 1; // Aus
	/**
	 * 
	 */
	public final static int MA_STATE_READY = 2; // Bereit
	/**
	 * 
	 */
	public final static int MA_STATE_PROGRAM = 3; // Programm gewählt
	/**
	 * 
	 */
	public final static int MA_STATE_WAITING = 4; // Start verzögert
	/**
	 * 
	 */
	public final static int MA_STATE_ON = 5; // In Betrieb
	/**
	 * 
	 */
	public final static int MA_STATE_PAUSED = 6; // Pause
	/**
	 * 
	 */
	public final static int MA_STATE_END = 7; // Ende
	/**
	 * 
	 */
	public final static int MA_STATE_ERROR = 8; // Fehler
	/**
	 * 
	 */
	public final static int MA_STATE_HALTED = 9; // Programm unterbrochen
	/**
	 * 
	 */
	public final static int MA_STATE_SERVICE = 12; // Service
	/**
	 * 
	 */
	public final static int MA_STATE_SUPERFROST = 13; // Superfrost
	/**
	 * 
	 */
	public final static int MA_STATE_SUPERCOOL = 14; // Superk�hlen
	/**
	 * 
	 */
	public final static int MA_STATE_DEFAULT = 144; // Default
	/**
	 * 
	 */
	public final static int MA_STATE_LOCKED = 145; // Verriegelt
	/**
	 * 
	 */
	public final static int MA_STATE_UNKNOWN = 0; // Unknown state. Not an
													// official state according
													// to Miele documentation.

	// State values (description) German
	/**
	 * 
	 */
	public static final String STATE_OFF_DE = "Aus";
	/**
	 * 
	 */
	public static final String STATE_VALUE_READY_DE = "Bereit";
	/**
	 * 
	 */
	public static final String STATE_VALUE_PROGRAM_DE = "Programm gewählt";
	/**
	 * 
	 */
	public static final String STATE_VALUE_WAITING_DE = "Start verzögert";
	/**
	 * 
	 */
	public static final String STATE_VALUE_ON_DE = "In Betrieb";
	/**
	 * 
	 */
	public static final String STATE_VALUE_PAUSED_DE = "Pause";
	/**
	 * 
	 */
	public static final String STATE_VALUE_END_DE = "Ende";
	/**
	 * 
	 */
	public static final String STATE_VALUE_ERROR_DE = "Fehler";
	/**
	 * 
	 */
	public static final String STATE_VALUE_HALTED_DE = "Abbruch";
	/**
	 * 
	 */
	public static final String STATE_VALUE_SERVICE_DE = "Service";
	/**
	 * 
	 */
	public static final String STATE_VALUE_SUPERFROST_DE = "Superfrost";
	/**
	 * 
	 */
	public static final String STATE_VALUE_SUPERCOOL_DE = "Superkühlen";
	/**
	 * 
	 */
	public static final String STATE_VALUE_DEFAULT_DE = "Default";
	/**
	 * 
	 */
	public static final String STATE_VALUE_LOCKED_DE = "Verriegelt";

	// State values (description) English
	/**
	 * 
	 */
	public static final String STATE_OFF_EN = "Off";;
	/**
	 * 
	 */
	public static final String STATE_VALUE_READY_EN = "On";
	/**
	 * 
	 */
	public static final String STATE_VALUE_PROGRAM_EN = "Programmed";
	/**
	 * 
	 */
	public static final String STATE_VALUE_WAITING_EN = "Waiting to Start";
	/**
	 * 
	 */
	public static final String STATE_VALUE_ON_EN = "Running";
	/**
	 * 
	 */
	public static final String STATE_VALUE_PAUSED_EN = "Paused";
	/**
	 * 
	 */
	public static final String STATE_VALUE_END_EN = "End";
	/**
	 * 
	 */
	public static final String STATE_VALUE_ERROR_EN = "Error";
	/**
	 * 
	 */
	public static final String STATE_VALUE_HALTED_EN = "Abort";
	/**
	 * 
	 */
	public static final String STATE_VALUE_SERVICE_EN = "Service";
	/**
	 * 
	 */
	public static final String STATE_VALUE_SUPERFROST_EN = "Super Freezing";
	/**
	 * 
	 */
	public static final String STATE_VALUE_SUPERCOOL_EN = "Super Cooling";
	/**
	 * 
	 */
	public static final String STATE_VALUE_DEFAULT_EN = "Default";
	/**
	 * 
	 */
	public static final String STATE_VALUE_LOCKED_EN = "Locked";

	/*
	 * Appliance class id and descriptions
	 */

	// Appliance class ID
	/**
	 * 
	 */
	public static final int DISHWASHER_CLASS_ID = 22017;
	/**
	 * 
	 */
	public static final int DRYER_CLASS_ID = 22018;
	/**
	 * 
	 */
	public static final int WASHING_MACHINE_CLASS_ID = 22019;
	/**
	 * 
	 */
	public static final int CERAMIC_HOB_CLASS_ID = 24067;
	/**
	 * 
	 */
	public static final int HOOD_CLASS_ID = 24068;
	/**
	 * 
	 */
	public static final int OVEN_CLASS_ID = 24070;
	/**
	 * 
	 */
	public static final int STOVE_CLASS_ID = 24071;
	/**
	 * 
	 */
	public static final int STEAMER_CLASS_ID = 24072;
	/**
	 * 
	 */
	public static final int INDUCTION_HOB_CLASS_ID = 24073;
	/**
	 * 
	 */
	public static final int COFFEE_MACHINE_CLASS_ID = 24074;
	/**
	 * 
	 */
	public static final int REFRIGERATOR_FREEZER_CLASS_ID = 26113;
	/**
	 * 
	 */
	public static final int FREEZER_CLASS_ID = 26114;
	/**
	 * 
	 */
	public static final int REFRIGERATOR_CLASS_ID = 26115;
	/**
	 * 
	 */
	public static final int WINE_REFRIGERATOR_CLASS_ID = 26116;

	// Appliance class name (German description)
	/**
	 * 
	 */
	public static final String DISHWASHER_CLASS_NAME_DE = "Geschirrspüler";
	/**
	 * 
	 */
	public static final String DRYER_CLASS_NAME_DE = "Trockenautomat";
	/**
	 * 
	 */
	public static final String WASHING_MACHINE_CLASS_NAME_DE = "Waschvollautomat";
	/**
	 * 
	 */
	public static final String CERAMIC_HOB_CLASS_NAME_DE = "Kochfeld (Highlight)";
	/**
	 * 
	 */
	public static final String HOOD_CLASS_NAME_DE = "Haube";
	/**
	 * 
	 */
	public static final String OVEN_CLASS_NAME_DE = "Oven";
	/**
	 * 
	 */
	public static final String STOVE_CLASS_NAME_DE = "Herd";
	/**
	 * 
	 */
	public static final String STEAMER_CLASS_NAME_DE = "Dampfgarer";
	/**
	 * 
	 */
	public static final String INDUCTION_HOB_CLASS_NAME_DE = "Kochfeld (Induktion)";
	/**
	 * 
	 */
	public static final String COFFEE_MACHINE_CLASS_NAME_DE = "Kaffeevollautomat";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_FREEZER_CLASS_NAME_DE = "Kühl-Gefrierkombi";
	/**
	 * 
	 */
	public static final String FREEZER_CLASS_NAME_DE = "Gefrierschrank";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_CLASS_NAME_DE = "Kühlschrank";
	/**
	 * 
	 */
	public static final String WINE_REFRIGERATOR_CLASS_NAME_DE = "Weinlager";

	// Appliance class name (English description)
	/**
	 * 
	 */
	public static final String DISHWASHER_CLASS_NAME_EN = "Dishwasher";
	/**
	 * 
	 */
	public static final String DRYER_CLASS_NAME_EN = "Dryer";
	/**
	 * 
	 */
	public static final String WASHING_MACHINE_CLASS_NAME_EN = "Washing machine";
	/**
	 * 
	 */
	public static final String CERAMIC_HOB_CLASS_NAME_EN = "Ceramic hob";
	/**
	 * 
	 */
	public static final String HOOD_CLASS_NAME_EN = "Hood";
	/**
	 * 
	 */
	public static final String OVEN_CLASS_NAME_EN = "Oven";
	/**
	 * 
	 */
	public static final String STOVE_CLASS_NAME_EN = "Stove";
	/**
	 * 
	 */
	public static final String STEAMER_CLASS_NAME_EN = "Streamer";
	/**
	 * 
	 */
	public static final String INDUCTION_HOB_CLASS_NAME_EN = "Induction hob";
	/**
	 * 
	 */
	public static final String COFFEE_MACHINE_CLASS_NAME_EN = "Coffee machine";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_FREEZER_CLASS_NAME_EN = "Refrigerator Freezer";
	/**
	 * 
	 */
	public static final String FREEZER_CLASS_NAME_EN = "Freezer";
	/**
	 * 
	 */
	public static final String REFRIGERATOR_CLASS_NAME_EN = "Refrigerator";
	/**
	 * 
	 */
	public static final String WINE_REFRIGERATOR_CLASS_NAME_EN = "Wine refrigerator";

}
