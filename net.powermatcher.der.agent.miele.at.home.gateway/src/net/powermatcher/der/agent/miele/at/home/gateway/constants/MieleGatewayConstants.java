package net.powermatcher.der.agent.miele.at.home.gateway.constants;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface MieleGatewayConstants {

	// Miele Gateway property name definitions
	/**
	 * 
	 */
	public final static String MG_PROPERTY_PORT_NUMBER = "miele.gateway.portnumber";
	/**
	 * 
	 */
	public final static String MG_PROPERTY_ID = "miele.gateway.id";

	// Miele Gateway defaults
	/**
	 * 
	 */
	public final static String MG_DEFAULT_PORT_NUMBER = "8080";
	/**
	 * 
	 */
	public final static String MG_DEFAULT_ID = "MIELE_GW_2000";
	/**
	 * 
	 */
	public final static String MG_DEFAULT_ENCODING = "UTF-8";

	// Miele Gateway message constants
	/**
	 * 
	 */
	public final static String ERROR_APPLIANCE_MISSING = "appliance_missing";
	/**
	 * 
	 */
	public final static String ERROR_ACTION_MISSING = "action_missing";
	/**
	 * 
	 */
	public final static String ERROR_ACTION_INCORRECT_PARAMS = "action_incorrect_params";
	/**
	 * 
	 */
	public final static String ERROR_ACTION_EXECUTE = "action_execute";

	/**
	 * 
	 */
	public final static String ERROR_MSG_APPLIANCE_MISSING = "Fehler: Apparat unbekannt.";
	/**
	 * 
	 */
	public final static String ERROR_MSG_ACTION_MISSING = "Fehler: Aktion unbekannt.";
	/**
	 * 
	 */
	public final static String ERROR_MSG_ACTION_INCORRECT_PARAMS = "Fehler: Parameters nicht korrekt.";
	/**
	 * 
	 */
	public final static String ERROR_MSG_ACTION_EXECUTE = "Fehler: start Aktion kann derzeit nicht ausgeführt werden";
	/**
	 * 
	 */
	public final static String ERROR_MSG_NOT_SUPPORTED_BY_STUB = "Currently not supported by stub.";

	// Miele constants for the URL definitions
	/**
	 * 
	 */
	public final static String MG_URL_HOMEBUS_TARGET = "/homebus";
	/**
	 * 
	 */
	public final static String MG_URL_DEVICE_TARGET = MG_URL_HOMEBUS_TARGET + "/device";
	/**
	 * 
	 */
	public final static String MG_URL_PARAM_LANGUAGE = "language";
	/**
	 * 
	 */
	public static final String MG_URL_PARAM_APPLIANCE_ID = "id";
	/**
	 * 
	 */
	public static final String MG_URL_PARAM_APPLIANCE_ACTION = "action";
	/**
	 * 
	 */
	public static final String MG_URL_PARAM_APPLIANCE_TYPE = "type";
	/**
	 * 
	 */
	public static final String MG_URL_PARAM_APPLIANCE_P1 = "p1";
	/**
	 * 
	 */
	public static final String MG_APPLIANCE_ACTION_ON = "on";
	/**
	 * 
	 */
	public final static String MG_APPLIANCE_ACTION_OFF = "off";

	/**
	 * 
	 */
	public static final float MA_INITIAL_TEMP_FRIDGE = 6;
	/**
	 * 
	 */
	public static final float MA_INITIAL_TEMP_FREEZER = -16f;

	/**
	 * 
	 */
	public static final String LANGUAGE_GERMAN = "de_DE";
	/**
	 * 
	 */
	public static final String LANGUAGE_ENGLISH = "en_EN";
	/**
	 * 
	 */
	public static final String LANGUAGE_DUTCH = "nl_NL";

}
