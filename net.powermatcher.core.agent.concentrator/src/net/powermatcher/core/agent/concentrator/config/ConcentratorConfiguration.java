package net.powermatcher.core.agent.concentrator.config;


import net.powermatcher.core.agent.framework.config.MatcherAgentConfiguration;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface ConcentratorConfiguration extends MatcherAgentConfiguration {

	/**
	 * Define the immediate update property (String) constant.
	 */
	public static final String IMMEDIATE_UPDATE_PROPERTY = "immediate.update";
	/**
	 * Define the immediate update default (int) constant.
	 */
	public static final String IMMEDIATE_UPDATE_DEFAULT_STR = "true";
	/**
	 * Define the immediate update default (int) constant.
	 */
	public static final boolean IMMEDIATE_UPDATE_DEFAULT = Boolean.valueOf(IMMEDIATE_UPDATE_DEFAULT_STR).booleanValue();
	/**
	 * Define the immediate update description (String) constant.
	 */
	public static final String IMMEDIATE_UPDATE_DESCRIPTION = "Update bid and price immediately for change in constraints";

	/**
	 * Immediate_update and return the int result.
	 * 
	 * @return Results of the immediate_update (<code>boolean</code>) value.
	 */
	public boolean immediate_update();

}
