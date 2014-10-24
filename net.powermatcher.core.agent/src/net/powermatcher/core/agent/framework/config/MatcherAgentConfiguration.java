package net.powermatcher.core.agent.framework.config;

import net.powermatcher.core.agent.framework.service.ParentConnectable;


/**
 * <p>
 * Defines the interface, configuration property names and default values
 * for the configuration of a MatcherAgent instance.
 * <p>
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface MatcherAgentConfiguration extends AgentConfiguration {
	/**
	 * Define the matcher listener ID property (String) constant.
	 */
	public static final String MATCHER_LISTENER_ID_PROPERTY = "matcher.listener.id";
	/**
	 * Define the matcher listener ID description (String) constant.
	 */
	public static final String MATCHER_LISTENER_ID_DESCRIPTION = "The id to subscribe for bid updates (if applicable)";

	/**
	 * Define the matcher adapter factory property (String) constant.
	 */
	public static final String MATCHER_ADAPTER_FACTORY_PROPERTY = ParentConnectable.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the matcher adapter factory description (String) constant.
	 */
	public static final String MATCHER_ADAPTER_FACTORY_DESCRIPTION = "The (list of) adapter factories for creating matcher adapters";

	/**
	 * Define the bid expiration property (String) constant.
	 */
	public static final String BID_EXPIRATION_TIME_PROPERTY = "bid.expiration.time";
	/**
	 * Define the bid expiration default (int) constant.
	 */
	public static final String BID_EXPIRATION_TIME_DEFAULT_STR = "300";
	/**
	 * Define the bid expiration default (int) constant.
	 */
	public static final int BID_EXPIRATION_TIME_DEFAULT = Integer.valueOf(BID_EXPIRATION_TIME_DEFAULT_STR).intValue();
	/**
	 * Define the bid expiration description (String) constant.
	 */
	public static final String BID_EXPIRATION_TIME_DESCRIPTION = "Bid expiration time in seconds";

	/**
	 * Define the matcher agent bid log level property (String) constant.
	 */
	public static final String MATCHER_AGENT_BID_LOG_LEVEL_PROPERTY = "matcher.agent.bid.log.level";
	/**
	 * Define the matcher agent bid log level default (String) constant.
	 */
	public static final String MATCHER_AGENT_BID_LOG_LEVEL_DEFAULT = FULL_LOGGING;
	/**
	 * Define the matcher bid log level description (String) constant.
	 */
	public static final String MATCHER_AGENT_BID_LOG_LEVEL_DESCRIPTION = "The logging level of PowerMatcher bid updates received by the matcher.";
	/**
	 * Define the matcher aggregated bid log level property (String) constant.
	 */
	public static final String MATCHER_AGGREGATED_BID_LOG_LEVEL_PROPERTY = "matcher.aggregated.bid.log.level";
	/**
	 * Define the matcher aggregated bid log level default (String) constant.
	 */
	public static final String MATCHER_AGGREGATED_BID_LOG_LEVEL_DEFAULT = NO_LOGGING;
	/**
	 * Define the matcher aggregated bid log level description (String) constant.
	 */
	public static final String MATCHER_AGGREGATED_BID_LOG_LEVEL_DESCRIPTION = "The logging level of PowerMatcher aggregated bid of the matcher";
	/**
	 * Define the matcher price log level property (String) constant.
	 */
	public static final String MATCHER_PRICE_LOG_LEVEL_PROPERTY = "matcher.price.log.level";
	/**
	 * Define the matcher price log level default (String) constant.
	 */
	public static final String MATCHER_PRICE_LOG_LEVEL_DEFAULT = FULL_LOGGING;
	/**
	 * Define the matcher price log level description (String) constant.
	 */
	public static final String MATCHER_PRICE_LOG_LEVEL_DESCRIPTION = "The logging level of published PowerMatcher price updates";

	/**
	 * Matcher_listener_id and return the String result.
	 * 
	 * @return Results of the matcher_listener_id (<code>String</code>) value.
	 */
	public String matcher_listener_id();

	/**
	 * Matcher_adapter_factory and return the String result.
	 * 
	 * @return Results of the matcher_adapter_factory (<code>String</code>) value.
	 */
	public String matcher_adapter_factory();
	
	/**
	 * Bid_expiration_time and return the int result.
	 * 
	 * @return Results of the bid_expiration_time (<code>int</code>) value.
	 */
	public int bid_expiration_time();

	/**
	 * Matcher_agent_bid_log_level and return the String result.
	 * 
	 * @return Results of the matcher_agent_bid_log_level (<code>String</code>) value.
	 */
	public String matcher_agent_bid_log_level();

	/**
	 * Matcher_aggregated_bid_log_level and return the String result.
	 * 
	 * @return Results of the matcher_aggregated_bid_log_level (<code>String</code>) value.
	 */
	public String matcher_aggregated_bid_log_level();

	/**
	 * Matcher_price_log_level and return the String result.
	 * 
	 * @return Results of the matcher_price_log_level (<code>String</code>) value.
	 */
	public String matcher_price_log_level();

}
