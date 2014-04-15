package net.powermatcher.agent.peakshavingconcentrator.config;

import net.powermatcher.core.agent.concentrator.config.ConcentratorConfiguration;


/**
 * @author IBM
 * @version 0.9.0
 */
public interface ClippingConcentratorConfiguration extends ConcentratorConfiguration {
	/**
	 * Property keyword for enabling peak shaving.
	 */
	public static final String PEAK_SHAVING_ENABLED_PROPERTY = "peak.shaving.enabled";
	/**
	 * Default value for minimum power applied in 'peak shaving'
	 */
	public static final boolean PEAK_SHAVING_ENABLED_DEFAULT = false;
	/**
	 * Define the peak shaving enabled default str (String) constant.
	 */
	public static final String PEAK_SHAVING_ENABLED_DEFAULT_STR = "false";
	/**
	 * Property keyword for the maximum power applied in 'peak shaving'
	 */
	public static final String POWER_UPPER_LIMIT_PROPERTY = "power.upper.limit";
	/**
	 * Property keyword for the minimum power applied in 'peak shaving'
	 */
	public static final String POWER_LOWER_LIMIT_PROPERTY = "power.lower.limit";

	/**
	 * Peak_shaving_enabled and return the boolean result.
	 * 
	 * @return Results of the peak_shaving_enabled (<code>boolean</code>) value.
	 */
	public boolean peak_shaving_enabled();

	/**
	 * Power_lower_limit and return the double result.
	 * 
	 * @return Results of the power_lower_limit (<code>double</code>) value.
	 */
	public double power_lower_limit();

	/**
	 * Power_upper_limit and return the double result.
	 * 
	 * @return Results of the power_upper_limit (<code>double</code>) value.
	 */
	public double power_upper_limit();

}
