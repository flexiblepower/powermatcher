package net.powermatcher.core.agent.auctioneer.config;


import net.powermatcher.core.agent.framework.config.MatcherAgentConfiguration;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface AuctioneerConfiguration extends MatcherAgentConfiguration {

	/**
	 * Define the market basis agent adapter factory description (String) constant.
	 */
	public static final String AGENT_ADAPTER_FACTORY_DESCRIPTION = "The adapter factory for creating the market basis adapter";
	/**
	 * Define the market basis agent adapter factory default (String) constant.
	 */
	public static final String AGENT_ADAPTER_FACTORY_DEFAULT = "marketBasisAdapterFactory";

	/**
	 * Define the pricing adapter factory property (String) constant.
	 */
	public static final String PRICING_ADAPTER_FACTORY_PROPERTY = AgentConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the pricing adapter factory description (String) constant.
	 */
	public static final String PRICING_ADAPTER_FACTORY_DESCRIPTION = "The adapter factories for creating a pricing adapter";

	/**
	 * Pricing_adapter_factory and return the String result.
	 * 
	 * @return Results of the pricing_adapter_factory (<code>String</code>) value.
	 */
	public String pricing_adapter_factory();
	
}
