package net.powermatcher.core.agent.framework.service;


import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;

/**
 * 
 * <p>
 * Defines the the services provided by an agent. 
 * <p>
 * <p>
 * The AgentService interface defines two methods that an agent should
 * provide:
 * <ul>
 * <li>update market basis</li>
 * <br>Handles the update of a market basis update.
 * <li>update price info</li>
 * <br>Handles the price info update.
 * </ul> 
 * </p> 
 * <p>
 * @author IBM
 * @version 0.9.0
 * 
 *  @see MarketBasis
 *  @see PriceInfo
 */
public interface AgentService {
	/**
	 * Update market basis with the specified new market basis parameter.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 */
	public void updateMarketBasis(final MarketBasis newMarketBasis);

	/**
	 * Update price info with the specified new price info parameter.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 */
	public void updatePriceInfo(final PriceInfo newPriceInfo);

}
