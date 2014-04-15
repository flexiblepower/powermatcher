package net.powermatcher.core.agent.objective.config;


import net.powermatcher.core.agent.framework.config.MatcherAgentConfiguration;
import net.powermatcher.core.agent.objective.service.ObjectiveConnectorService;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface ObjectiveAgentConfiguration extends MatcherAgentConfiguration {

	/**
	 * Define the objective adapter factory property (String) constant.
	 */
	public static final String OBJECTIVE_ADAPTER_FACTORY_PROPERTY = ObjectiveConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the objective adapter factory description (String) constant.
	 */
	public static final String OBJECTIVE_ADAPTER_FACTORY_DESCRIPTION = "The adapter factory for creating the objective adapter";

	/**
	 * Define the bid property (String) constant.
	 */
	public static final String BID_PROPERTY = "objective.bid";
	/**
	 * Define the bid property delimiter (String) constant.
	 */
	public static final String BID_PROPERTY_DELIMITER = ";";
	/**
	 * Define the bid property default (String[]) constant.
	 */
	public static final String BID_PROPERTY_DEFAULT_STR = "(0\\,0.0)";
	/**
	 * Define the bid property default (String[]) constant.
	 */
	public static final String[] BID_PROPERTY_DEFAULT = { "(0,0.0)" };
	/**
	 * Define the bid property (String) constant.
	 */
	public static final String BID_DESCRIPTION = "Default objective bid (without adapter)";

	/**
	 * Objective_adapter_factory and return the String result.
	 * 
	 * @return Results of the objective_adapter_factory (<code>String</code>) value.
	 */
	public String objective_adapter_factory();
	
	/**
	 * Objective_bid and return the String result.
	 * 
	 * @return Results of the objective_bid (<code>String</code>) value.
	 */
	public String objective_bid();

}
