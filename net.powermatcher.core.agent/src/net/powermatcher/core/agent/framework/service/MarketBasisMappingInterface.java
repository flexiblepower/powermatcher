package net.powermatcher.core.agent.framework.service;


import net.powermatcher.core.agent.framework.data.MarketBasis;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface MarketBasisMappingInterface {
	/**
	 * Map to external market basis with the specified internal market basis
	 * parameter and return the MarketBasis result.
	 * 
	 * @param internalMarketBasis
	 *            The internal market basis (<code>MarketBasis</code>)
	 *            parameter.
	 * @return Results of the map to internal market basis (
	 *         <code>MarketBasis</code>) value.
	 */
	public MarketBasis toExternalMarketBasis(final MarketBasis internalMarketBasis);

	/**
	 * Map to internal market basis with the specified external market basis
	 * parameter and return the MarketBasis result.
	 * 
	 * @param externalMarketBasis
	 *            The external market basis (<code>MarketBasis</code>)
	 *            parameter.
	 * @return Results of the map to internal market basis (
	 *         <code>MarketBasis</code>) value.
	 */
	public MarketBasis toInternalMarketBasis(final MarketBasis externalMarketBasis);

}
