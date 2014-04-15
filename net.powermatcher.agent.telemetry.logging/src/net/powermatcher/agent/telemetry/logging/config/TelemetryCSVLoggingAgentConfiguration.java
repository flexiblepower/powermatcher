package net.powermatcher.agent.telemetry.logging.config;


import net.powermatcher.core.agent.logging.config.CSVLoggingAgentConfiguration;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface TelemetryCSVLoggingAgentConfiguration extends CSVLoggingAgentConfiguration {
	/**
	 * Define the telemetry listener adapter factory property (String) constant.
	 */
	public static final String TELEMETRY_LISTENER_ADAPTER_FACTORY_PROPERTY = TelemetryListenerConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the telemetry listener adapter factory description (String) constant.
	 */
	public static final String TELEMETRY_LISTENER_ADAPTER_FACTORY_DESCRIPTION = "The (list of) adapter factories for creating telemetry listener adapters";
	/**
	 * Define the measurement logging pattern property (String) constant.
	 */
	public static final String MEASUREMENT_LOGGING_PATTERN_PROPERTY = "measurement.logging.pattern";
	/**
	 * Define the measurement logging pattern default (String) constant.
	 */
	public static final String MEASUREMENT_LOGGING_PATTERN_DEFAULT = "'measurement_log_'yyyyMMdd'.csv'";
	/**
	 * Define the status logging pattern property (String) constant.
	 */
	public static final String STATUS_LOGGING_PATTERN_PROPERTY = "status.logging.pattern";
	/**
	 * Define the status logging pattern default (String) constant.
	 */
	public static final String STATUS_LOGGING_PATTERN_DEFAULT = "'status_log_'yyyyMMdd'.csv'";

	/**
	 * Telemetry_listener_adapter_factory and return the String result.
	 * 
	 * @return Results of the telemetry_listener_adapter_factory (<code>String</code>) value.
	 */
	public String telemetry_listener_adapter_factory();

	/**
	 * Measurement_logging_pattern and return the String result.
	 * 
	 * @return Results of the measurement_logging_pattern (<code>String</code>)
	 *         value.
	 */
	public String measurement_logging_pattern();


	/**
	 * Status_logging_pattern and return the String result.
	 * 
	 * @return Results of the status_logging_pattern (<code>String</code>)
	 *         value.
	 */
	public String status_logging_pattern();

}
