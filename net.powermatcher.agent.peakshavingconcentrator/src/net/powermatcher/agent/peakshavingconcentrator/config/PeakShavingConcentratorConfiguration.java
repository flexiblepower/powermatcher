package net.powermatcher.agent.peakshavingconcentrator.config;

import net.powermatcher.core.agent.concentrator.config.ConcentratorConfiguration;
import net.powermatcher.core.agent.concentrator.service.PeakShavingConnectorService;

public interface PeakShavingConcentratorConfiguration extends ConcentratorConfiguration {

	/**
	 * Define the peak shaving adapter factory property (String) constant.
	 */
	public static final String PEAK_SHAVING_ADAPTER_FACTORY_PROPERTY = PeakShavingConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the peak shaving adapter factory description (String) constant.
	 */
	public static final String PEAK_SHAVING_ADAPTER_FACTORY_DESCRIPTION = "The adapter factories for creating the peak shaving adapter";

	/**
	 * Peak_shaving_adapter_factory and return the String result.
	 * 
	 * @return Results of the peak_shaving_adapter_factory (<code>String</code>) value.
	 */
	public String peak_shaving_adapter_factory();
	

}
