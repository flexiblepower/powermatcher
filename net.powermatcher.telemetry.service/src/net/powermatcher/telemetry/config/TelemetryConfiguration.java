package net.powermatcher.telemetry.config;

import net.powermatcher.telemetry.service.TelemetryConnectorService;



/**
 * Defines the interface of telemetry adapter configuration, configuration properties
 * and constants.
 * 
 * <p>
 * A TelemetryConfiguration object configures a TelemetryAdapter instance. 
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 */
public interface TelemetryConfiguration {
	/**
	 * Define the telemetry agent ID property (String) constant.
	 */
	public static final String TELEMETRY_LISTENER_ID_PROPERTY = "telemetry.listener.id";
	/**
	 * Define the telemetry agent ID default (String) constant.
	 */
	public static final String TELEMETRY_LISTENER_ID_DEFAULT_STR = "telemetrycsvlogging";
	/**
	 * Define the telemetry agent ID default (String[]) constant.
	 */
	public static final String[] TELEMETRY_LISTENER_ID_DEFAULT = new String[] { TELEMETRY_LISTENER_ID_DEFAULT_STR };
	/**
	 * Define the telemetry agent ID description (String) constant.
	 */
	public static final String TELEMETRY_LISTENER_ID_DESCRIPTION = "The (list of) telemetry listener ids to connect to";

	/**
	 * Define the telemetry adapter factory property (String) constant.
	 */
	public static final String TELEMETRY_ADAPTER_FACTORY_PROPERTY = TelemetryConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the telemetry adapter factory default (String) constant.
	 */
	public static final String TELEMETRY_ADAPTER_FACTORY_DEFAULT = "directTelemetryAdapter";
	/**
	 * Define the telemetry adapter factory description (String) constant.
	 */
	public static final String TELEMETRY_ADAPTER_FACTORY_DESCRIPTION = "The (list of) adapter factories for creating telemetry publishing adapters";

	/**
	 * Telemetry_listener_id and return the String result.
	 * 
	 * @return Results of the telemetry_listener_id (<code>String</code>) value.
	 */
	public String telemetry_listener_id();

	/**
	 * Telemetry_adapter_factory and return the String result.
	 * 
	 * @return Results of the telemetry_adapter_factory (<code>String</code>) value.
	 */
	public String telemetry_adapter_factory();
	
}
