package net.powermatcher.der.agent.miele.at.home.gateway.constants;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface MieleGatewayXmlConstants {

	/**
	 * The gateway sends the following message types
	 */
	public enum MieleGatewayMessageType {
		/**
		 * 
		 */
		APPLIANCE_LIST,
		/**
		 * 
		 */
		APPLIANCE_INFO,
		/**
		 * 
		 */
		ACTION_OK,
		/**
		 * 
		 */
		ACTION_ERROR
	}

	// Elements and attributes of Miele Gateway XML response messages
	/**
	 * 
	 */
	public static final String ELEMENT_DEVICE = "device";
	/**
	 * 
	 */
	public static final String ELEMENT_KEY = "key";
	/**
	 * 
	 */
	public static final String ATTR_KEY_VALUE = "value";
	/**
	 * 
	 */
	public static final String ATTR_KEY_NAME = "name";
	/**
	 * 
	 */
	public static final String ELEMENT_DEVICES = "devices";
	/**
	 * 
	 */
	public static final String ELEMENT_INFORMATION = "information";
	/**
	 * 
	 */
	public static final String ELEMENT_ACTIONS = "actions";
	/**
	 * 
	 */
	public static final String ELEMENT_ACTION = "action";
	/**
	 * 
	 */
	public static final String ATTR_ACTION_NAME = "name";
	/**
	 * 
	 */
	public static final String ATTR_ACTION_URL = "URL";
	/**
	 * 
	 */
	public static final String ELEMENT_OK = "ok";
	/**
	 * 
	 */
	public static final String ELEMENT_ERROR = "error";
	/**
	 * 
	 */
	public static final String ELEMENT_MESSAGE = "message";

	// Appliance information keys (German)
	// TODO: make this configurable using a property file
	/**
	 * 
	 */
	public static final String MA_APPLIANCE_STATUS = "Gerätestatus";
	/**
	 * 
	 */
	public static final String MA_APPLIANCE_TYPE = "Gerät";
	/**
	 * 
	 */
	public static final String MA_FRIDGE_STATUS = "Kühlstatus";
	/**
	 * 
	 */
	public static final String MA_FRIDGE_ACTUAL_TEMPERATURE = "Aktuelle Kühltemperatur";
	/**
	 * 
	 */
	public static final String MA_FRIDGE_TARGET_TEMPERATURE = "Ziel-Kühtemperatur";
	/**
	 * 
	 */
	public static final String MA_FREEZER_STATUS = "Gefrierstatus";
	/**
	 * 
	 */
	public static final String MA_FREEZER_ACTUAL_TEMPERATURE = "Aktuelle Gefriertemperatur";
	/**
	 * 
	 */
	public static final String MA_FREEZER_TARGET_TEMPERATURE = "Ziel-Gefriertemperatur";

}
