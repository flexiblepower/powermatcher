package net.powermatcher.core.agent.test.config;


import net.powermatcher.core.agent.framework.config.AgentConfiguration;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface TestAgentConfiguration extends AgentConfiguration {
	/**
	 * Define the minimum price property (String) constant.
	 */
	public static final String MINIMUM_PRICE_PROPERTY = "minimum.price";
	/**
	 * Define the minimum price default (int) constant.
	 */
	public static final int MINIMUM_PRICE_DEFAULT = 0;
	/**
	 * Define the minimum price default str (String) constant.
	 */
	public static final String MINIMUM_PRICE_DEFAULT_STR = "0";
	/**
	 * Define the maximum price property (String) constant.
	 */
	public static final String MAXIMUM_PRICE_PROPERTY = "maximum.price";
	/**
	 * Define the maximum price default (int) constant.
	 */
	public static final int MAXIMUM_PRICE_DEFAULT = 120;
	/**
	 * Define the maximum price default str (String) constant.
	 */
	public static final String MAXIMUM_PRICE_DEFAULT_STR = "120";
	/**
	 * Define the minimum power property (String) constant.
	 */
	public static final String MINIMUM_POWER_PROPERTY = "minimum.power";
	/**
	 * Define the minimum power default (double) constant.
	 */
	public static final double MINIMUM_POWER_DEFAULT = 1000.0;
	/**
	 * Define the minimum power default str (String) constant.
	 */
	public static final String MINIMUM_POWER_DEFAULT_STR = "1000";
	/**
	 * Define the maximum power property (String) constant.
	 */
	public static final String MAXIMUM_POWER_PROPERTY = "maximum.power";
	/**
	 * Define the maximum power default (double) constant.
	 */
	public static final double MAXIMUM_POWER_DEFAULT = 1000.0;
	/**
	 * Define the maximum power default str (String) constant.
	 */
	public static final String MAXIMUM_POWER_DEFAULT_STR = "1000";
	/**
	 * Define the steps property (String) constant.
	 */
	public static final String STEPS_PROPERTY = "steps";
	/**
	 * Define the steps default (int) constant.
	 */
	public static final int STEPS_DEFAULT = 12;
	/**
	 * Define the steps default str (String) constant.
	 */
	public static final String STEPS_DEFAULT_STR = "12";

	/**
	 * Maximum_power and return the double result.
	 * 
	 * @return Results of the maximum_power (<code>double</code>) value.
	 */
	public double maximum_power();

	/**
	 * Maximum_price and return the int result.
	 * 
	 * @return Results of the maximum_price (<code>int</code>) value.
	 */
	public int maximum_price();

	/**
	 * Minimum_power and return the double result.
	 * 
	 * @return Results of the minimum_power (<code>double</code>) value.
	 */
	public double minimum_power();

	/**
	 * Minimum_price and return the int result.
	 * 
	 * @return Results of the minimum_price (<code>int</code>) value.
	 */
	public int minimum_price();

	/**
	 * Steps and return the int result.
	 * 
	 * @return Results of the steps (<code>int</code>) value.
	 */
	public int steps();

}
