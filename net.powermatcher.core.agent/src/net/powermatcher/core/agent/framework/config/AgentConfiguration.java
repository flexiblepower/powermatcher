package net.powermatcher.core.agent.framework.config;


import net.powermatcher.core.agent.framework.log.LogPublishable;
import net.powermatcher.core.agent.framework.service.ChildConnectable;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

/**
 * Defines the configuration values for an agent instance.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface AgentConfiguration extends ActiveObjectConfiguration {

	/**
	 * Enumeration of the logging levels.
	 */
	public enum LoggingLevel {
		/**
		 * Ordinal 0
		 */
		NO_LOGGING,
		/**
		 * Ordinal 1
		 */
		PARTIAL_LOGGING,
		/**
		 * Ordinal 2
		 */
		FULL_LOGGING
	}

	/**
	 * Define the parent matcher ID property (String) constant.
	 */
	public static final String PARENT_MATCHER_ID_PROPERTY = "matcher.id";
	/**
	 * Define the parent matcher ID default (String) constant.
	 */
	public static final String PARENT_MATCHER_ID_DEFAULT_STR = "root";
	/**
	 * Define the parent matcher ID default (String[]) constant.
	 */
	public static final String[] PARENT_MATCHER_ID_DEFAULT = new String[] { PARENT_MATCHER_ID_DEFAULT_STR };
	/**
	 * Define the parent matcher ID description (String) constant.
	 */
	public static final String PARENT_MATCHER_ID_DESCRIPTION = "The (list of) matcher connector ids to connect to";

	/**
	 * Define the agent adapter factory property (String) constant.
	 */
	public static final String AGENT_ADAPTER_FACTORY_PROPERTY = ChildConnectable.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the agent adapter factory default (String) constant.
	 */
	public static final String AGENT_ADAPTER_FACTORY_DEFAULT = "directProtocolAdapterFactory";
	/**
	 * Define the agent adapter factory description (String) constant.
	 */
	public static final String AGENT_ADAPTER_FACTORY_DESCRIPTION = "The (list of) adapter factories for creating agent adapters";

	/**
	 * Define the logging agent ID property (String) constant.
	 */
	public static final String LOG_LISTENER_ID_PROPERTY = "log.listener.id";
	/**
	 * Define the logging agent ID default (String) constant.
	 */
	public static final String LOG_LISTENER_ID_DEFAULT_STR = "csvlogging";
	/**
	 * Define the logging agent ID default (String[]) constant.
	 */
	public static final String[] LOG_LISTENER_ID_DEFAULT = new String[] { LOG_LISTENER_ID_DEFAULT_STR };
	/**
	 * Define the logging agent ID description (String) constant.
	 */
	public static final String LOG_LISTENER_ID_DESCRIPTION = "The (list of) log listener ids to connect to";

	/**
	 * Define the logging adapter factory property (String) constant.
	 */
	public static final String LOGGING_ADAPTER_FACTORY_PROPERTY = LogPublishable.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the logging adapter factory default (String) constant.
	 */
	public static final String LOGGING_ADAPTER_FACTORY_DEFAULT = "directLoggingAdapterFactory";
	/**
	 * Define the logging adapter factory description (String) constant.
	 */
	public static final String LOGGING_ADAPTER_FACTORY_DESCRIPTION = "The (list of) adapter factories for creating logging adapters";

	/**
	 * Define the agent log qualifier (String) constant.
	 */
	public static final String AGENT_LOG_QUALIFIER = "agent";

	/**
	 * Define the no logging property value  (String) constant.
	 */
	public static final String NO_LOGGING = "NO_LOGGING";
	/**
	 * Define the full logging property value (String) constant.
	 */
	public static final String PARTIAL_LOGGING = "PARTIAL_LOGGING";
	/**
	 * Define the full logging property value (String) constant.
	 */
	public static final String FULL_LOGGING = "FULL_LOGGING";
	/**
	 * Define the no logging property label  (String) constant.
	 */
	public static final String NO_LOGGING_LABEL = "No logging";
	/**
	 * Define the full logging property label (String) constant.
	 */
	public static final String PARTIAL_LOGGING_LABEL = "Partial logging";
	/**
	 * Define the full logging property label (String) constant.
	 */
	public static final String FULL_LOGGING_LABEL = "Full logging";
	/**
	 * Define the agent bid log level property (String) constant.
	 */
	public static final String AGENT_BID_LOG_LEVEL_PROPERTY = "agent.bid.log.level";
	/**
	 * Define the agent bid log level default (String) constant.
	 */
	public static final String AGENT_BID_LOG_LEVEL_DEFAULT = NO_LOGGING;
	/**
	 * Define the agent bid log level description (String) constant.
	 */
	public static final String AGENT_BID_LOG_LEVEL_DESCRIPTION = "The logging level of the published PowerMatcher bid updates";
	/**
	 * Define the agent price log level property (String) constant.
	 */
	public static final String AGENT_PRICE_LOG_LEVEL_PROPERTY = "agent.price.log.level";
	/**
	 * Define the agent price log level default (String) constant.
	 */
	public static final String AGENT_PRICE_LOG_LEVEL_DEFAULT = NO_LOGGING;
	/**
	 * Define the agent price log level description (String) constant.
	 */
	public static final String AGENT_PRICE_LOG_LEVEL_DESCRIPTION = "The logging level of received PowerMatcher price updates";

	/**
	 * Matcher_id and return the String result.
	 * 
	 * @return Results of the matcher_id (<code>String</code>) value.
	 */
	public String matcher_id();

	/**
	 * Agent_adapter_factory and return the String result.
	 * 
	 * @return Results of the agent_adapter_factory (<code>String</code>) value.
	 */
	public String agent_adapter_factory();
	
	/**
	 * Logging_adapter_factory and return the String result.
	 * 
	 * @return Results of the logging_adapter_factory (<code>String</code>) value.
	 */
	public String logging_adapter_factory();

	/**
	 * Log_listener_id and return the String result.
	 * 
	 * @return Results of the log_listener_id (<code>String</code>) value.
	 */
	public String log_listener_id();
	/**
	 * Agent_bid_log_level and return the String result.
	 * 
	 * @return Results of the agent_bid_log_level (<code>String</code>) value.
	 */
	public String agent_bid_log_level();
	/**
	 * Agent_price_log_level and return the String result.
	 * 
	 * @return Results of the agent_price_log_level (<code>String</code>) value.
	 */
	public String agent_price_log_level();

}
