package net.powermatcher.core.messaging.protocol.adapter.han;


import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.service.MarketBasisMappingInterface;


/**
 * @author IBM
 * @version 0.9.0
 */
public class HANMarketBasisMapper implements MarketBasisMappingInterface {
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
		double minimumPrice = internalMarketBasis.getMinimumPrice();
		double maximumPrice = internalMarketBasis.getMaximumPrice();
		int priceSteps = internalMarketBasis.getPriceSteps();
		if (priceSteps != AbstractHANMessage.PRICE_STEPS || -minimumPrice != maximumPrice) {
			maximumPrice = Math.max(-minimumPrice, maximumPrice);
			return new MarketBasis(internalMarketBasis.getCommodity(), internalMarketBasis.getCurrency(),
					AbstractHANMessage.PRICE_STEPS, -maximumPrice, maximumPrice, 0, internalMarketBasis.getMarketRef());
		}
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
