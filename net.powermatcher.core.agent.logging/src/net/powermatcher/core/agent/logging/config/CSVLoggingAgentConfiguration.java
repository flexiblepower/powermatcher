package net.powermatcher.core.agent.logging.config;


import net.powermatcher.core.agent.framework.log.LogListenerConnectorService;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface CSVLoggingAgentConfiguration extends ActiveObjectConfiguration {
	/**
	 * Define the log listener adapter factory property (String) constant.
	 */
	public static final String LOG_LISTENER_ADAPTER_FACTORY_PROPERTY = LogListenerConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the log listener adapter factory description (String) constant.
	 */
	public static final String LOG_LISTENER_ADAPTER_FACTORY_DESCRIPTION = "The (list of) adapter factories for creating log listener adapters";

	/**
	 * Define the PowerMatcher bid logging pattern property (String) constant.
	 */
	public static final String POWERMATCHER_BID_LOGGING_PATTERN_PROPERTY = "powermatcher.bid.logging.pattern";
	/**
	 * Define the PowerMatcher bid logging pattern default (String) constant.
	 */
	public static final String POWERMATCHER_BID_LOGGING_PATTERN_DEFAULT = "'pwm_bid_log_'yyyyMMdd'.csv'";
	/**
	 * Define the PowerMatcher bid logging pattern property (String) constant.
	 */
	public static final String POWERMATCHER_PRICE_LOGGING_PATTERN_PROPERTY = "powermatcher.price.logging.pattern";
	/**
	 * Define the PowerMatcher bid logging pattern default (String) constant.
	 */
	public static final String POWERMATCHER_PRICE_LOGGING_PATTERN_DEFAULT = "'pwm_price_log_'yyyyMMdd'.csv'";
	/**
	 * Define the logging pattern property (String) constant.
	 */
	public static final String LOGGING_PATTERN_DESCRIPTION = "File name pattern for logging";

	/**
	 * Define the list separator property (String) constant.
	 */
	public static final String LIST_SEPARATOR_PROPERTY = "list.separator";
	/**
	 * Define the list separator default (String) constant.
	 */
	public static final String LIST_SEPARATOR_DEFAULT = ";";
	/**
	 * Define the list separator description (String) constant.
	 */
	public static final String LIST_SEPARATOR_DESCRIPTION = "Separator to use in csv output.";
	/**
	 * Define the date format property (String) constant.
	 */
	public static final String DATE_FORMAT_PROPERTY = "date.format";
	/**
	 * Define the date format default (String) constant.
	 */
	public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	/**
	 * Define the date format description (String) constant.
	 */
	public static final String DATE_FORMAT_DESCRIPTION = "Java date format to use in csv output";

	/**
	 * Log_listener_adapter_factory and return the String result.
	 * 
	 * @return Results of the log_listener_adapter_factory (<code>String</code>) value.
	 */
	public String log_listener_adapter_factory();

	/**
	 * Date_format and return the String result.
	 * 
	 * @return Results of the date_format (<code>String</code>) value.
	 */
	public String date_format();

	/**
	 * List_separator and return the String result.
	 * 
	 * @return Results of the list_separator (<code>String</code>) value.
	 */
	public String list_separator();

	/**
	 * Bid_logging_pattern and return the String result.
	 * 
	 * @return Results of the bid_logging_pattern (<code>String</code>) value.
	 */
	public String powermatcher_bid_logging_pattern();

	/**
	 * Price_logging_pattern and return the String result.
	 * 
	 * @return Results of the price_logging_pattern (<code>String</code>) value.
	 */
	public String powermatcher_price_logging_pattern();

}
