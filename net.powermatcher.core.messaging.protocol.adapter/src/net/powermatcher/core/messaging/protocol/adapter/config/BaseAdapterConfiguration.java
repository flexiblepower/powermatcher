package net.powermatcher.core.messaging.protocol.adapter.config;


import net.powermatcher.core.messaging.framework.config.MessagingAdapterConfiguration;

/**
 * Defines the configuration properties, default values and constants for BaseAdapter object.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface BaseAdapterConfiguration extends MessagingAdapterConfiguration {

	/**
	 * Define the bid topic suffix property (String) constant.
	 */
	public static final String BID_TOPIC_SUFFIX_PROPERTY = "bid.topic.suffix";
	/**
	 * Define the bid topic suffix default (String) constant.
	 */
	public static final String BID_TOPIC_SUFFIX_DEFAULT = "UpdateBid";
	/**
	 * Define the price info topic suffix property (String) constant.
	 */
	public static final String PRICE_INFO_TOPIC_SUFFIX_PROPERTY = "price.info.topic.suffix";
	/**
	 * Define the price info topic suffix default (String) constant.
	 */
	public static final String PRICE_INFO_TOPIC_SUFFIX_DEFAULT = "UpdatePriceInfo";
	/**
	 * Define the log topic suffix property (String) constant.
	 */
	public static final String LOG_TOPIC_SUFFIX_PROPERTY = "log.topic.suffix";
	/**
	 * Define the log topic suffix default (String) constant.
	 */
	public static final String LOG_TOPIC_SUFFIX_DEFAULT = "Log";

	/**
	 * Bid_topic_suffix and return the String result.
	 * 
	 * @return Results of the bid_topic_suffix (<code>String</code>) value.
	 */
	public String bid_topic_suffix();

	/**
	 * Price_info_topic_suffix and return the String result.
	 * 
	 * @return Results of the price_info_topic_suffix (<code>String</code>)
	 *         value.
	 */
	public String price_info_topic_suffix();

	/**
	 * Log_topic_suffix and return the String result.
	 * 
	 * @return Results of the log_topic_suffix (<code>String</code>)
	 *         value.
	 */
	public String log_topic_suffix();

}
