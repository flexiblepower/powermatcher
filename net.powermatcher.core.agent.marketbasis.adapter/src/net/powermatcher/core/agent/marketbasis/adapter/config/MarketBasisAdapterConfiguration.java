package net.powermatcher.core.agent.marketbasis.adapter.config;

import net.powermatcher.core.adapter.config.AdapterConfiguration;


/**
 * Defines the interface of a market basis adapter configuration, configuration properties
 * and constants.
 * 
 * <p>
 * A MarketBasisAdapterConfiguration object configures a MarketBasisAdapter instance. 
 * </p>
 * @author IBM
 * @version 0.9.0
 */
public interface MarketBasisAdapterConfiguration extends AdapterConfiguration {
	/**
	 * Define the market basis agent adapter factory default (String) constant.
	 */
	public static final String AGENT_ADAPTER_FACTORY_DEFAULT = "marketBasisAdapterFactory";
	/**
	 * Define the commodity property (String) constant.
	 */
	public static final String COMMODITY_PROPERTY = "commodity";
	/**
	 * Define the commodity default (String) constant.
	 */
	public static final String COMMODITY_DEFAULT = "electricity"; // Commodity.ELECTRICITY.name();
	/**
	 * Define the commodity description (String) constant.
	 */
	public static final String COMMODITY_DESCRIPTION = "PowerMatcher commodity";
	/**
	 * Define the currency property (String) constant.
	 */
	public static final String CURRENCY_PROPERTY = "currency";
	/**
	 * Define the currency default (String) constant.
	 */
	public static final String CURRENCY_DEFAULT = "EUR"; // Currency.EUR.name();
	/**
	 * Define the currency description (String) constant.
	 */
	public static final String CURRENCY_DESCRIPTION = "Currency symbol (3 char)";
	/**
	 * Define the minimum price property (String) constant.
	 */
	public static final String MINIMUM_PRICE_PROPERTY = "minimum.price";
	/**
	 * Define the minimum price default str (String) constant.
	 */
	public static final String MINIMUM_PRICE_DEFAULT_STR = "-127";
	/**
	 * Define the minimum price default (double) constant.
	 */
	public static final double MINIMUM_PRICE_DEFAULT = Double.parseDouble(MINIMUM_PRICE_DEFAULT_STR);
	/**
	 * Define the minimum price description (String) constant.
	 */
	public static final String MINIMUM_PRICE_DESCRIPTION = "Minimum price (floating point).";
	/**
	 * Define the maximum price property (String) constant.
	 */
	public static final String MAXIMUM_PRICE_PROPERTY = "maximum.price";
	/**
	 * Define the maximum price default str (String) constant.
	 */
	public static final String MAXIMUM_PRICE_DEFAULT_STR = "127";
	/**
	 * Define the maximum price default (double) constant.
	 */
	public static final double MAXIMUM_PRICE_DEFAULT = Double.parseDouble(MAXIMUM_PRICE_DEFAULT_STR);
	/**
	 * Define the maximum price description (String) constant.
	 */
	public static final String MAXIMUM_PRICE_DESCRIPTION = "Maximum price (floating point)";
	/**
	 * Define the price steps property (String) constant.
	 */
	public static final String PRICE_STEPS_PROPERTY = "price.steps";
	/**
	 * Define the price steps default str (String) constant.
	 */
	public static final String PRICE_STEPS_DEFAULT_STR = "255";
	/**
	 * Define the price steps default (int) constant.
	 */
	public static final int PRICE_STEPS_DEFAULT = Integer.parseInt(PRICE_STEPS_DEFAULT_STR);
	/**
	 * Define the price steps description (String) constant.
	 */
	public static final String PRICE_STEPS_DESCRIPTION = "Steps from minimum up to and including maximum price.";
	/**
	 * Define the significance property (String) constant.
	 */
	public static final String SIGNIFICANCE_PROPERTY = "significance";
	/**
	 * Define the significance default str (String) constant.
	 */
	public static final String SIGNIFICANCE_DEFAULT_STR = "0";
	/**
	 * Define the significance default (int) constant.
	 */
	public static final int SIGNIFICANCE_DEFAULT = Integer.parseInt(SIGNIFICANCE_DEFAULT_STR);
	/**
	 * Define the significance description (String) constant.
	 */
	public static final String SIGNIFICANCE_DESCRIPTION = "Numer of significant digits in price";
	/**
	 * Define the market ref property (String) constant.
	 */
	public static final String MARKET_REF_PROPERTY = "market.ref";
	/**
	 * Define the market ref default str (String) constant.
	 */
	public static final String MARKET_REF_DEFAULT_STR = "0";
	/**
	 * Define the market ref default (int) constant.
	 */
	public static final int MARKET_REF_DEFAULT = Integer.parseInt(MARKET_REF_DEFAULT_STR);
	/**
	 * Define the market ref description (String) constant.
	 */
	public static final String MARKET_REF_DESCRIPTION = "Market basis reference identifier";

	/**
	 * Commodity and return the String result.
	 * 
	 * @return Results of the commodity (<code>String</code>) value.
	 */
	public String commodity();

	/**
	 * Currency and return the String result.
	 * 
	 * @return Results of the currency (<code>String</code>) value.
	 */
	public String currency();

	/**
	 * Market ref and return the int result.
	 * 
	 * @return Results of the market ref (<code>int</code>) value.
	 */
	public int market_ref();

	/**
	 * Maximum_price and return the double result.
	 * 
	 * @return Results of the maximum_price (<code>double</code>) value.
	 */
	public double maximum_price();

	/**
	 * Minimum_price and return the double result.
	 * 
	 * @return Results of the minimum_price (<code>double</code>) value.
	 */
	public double minimum_price();

	/**
	 * Price_steps and return the int result.
	 * 
	 * @return Results of the price_steps (<code>int</code>) value.
	 */
	public int price_steps();

	/**
	 * Significance and return the int result.
	 * 
	 * @return Results of the significance (<code>int</code>) value.
	 */
	public int significance();

}
