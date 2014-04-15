package net.powermatcher.core.messaging.protocol.adapter.internal;


import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.service.MarketBasisMappingInterface;


/**
 * @author IBM
 * @version 0.9.0
 */
public class InternalMarketBasisMapper implements MarketBasisMappingInterface {
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
	@Override
	public MarketBasis toExternalMarketBasis(final MarketBasis internalMarketBasis) {
		return internalMarketBasis;
	}

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
	@Override
	public MarketBasis toInternalMarketBasis(final MarketBasis externalMarketBasis) {
		return externalMarketBasis;
	}

}
