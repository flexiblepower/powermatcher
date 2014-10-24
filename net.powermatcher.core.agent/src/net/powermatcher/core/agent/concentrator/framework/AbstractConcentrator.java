package net.powermatcher.core.agent.concentrator.framework;


import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractConcentrator extends MatcherAgent {
	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #AbstractConcentrator(Configurable)
	 */
	public AbstractConcentrator() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #AbstractConcentrator()
	 */
	public AbstractConcentrator(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Adjust the downstream price, for example for peak shaving. By default,
	 * there is no transformation and no price adjustment.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @return The adjusted price (by default there is no adjustment)
	 */
	protected PriceInfo adjustPrice(final PriceInfo newPriceInfo) {
		return newPriceInfo;
	}

	/**
	 * Handle aggregated bid update with the specified new bid parameter.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	@Override
	protected void handleAggregatedBidUpdate(final BidInfo newBidInfo) {
		super.handleAggregatedBidUpdate(newBidInfo);
		BidInfo transformedBid = transformAggregatedBid(newBidInfo);
		publishBidUpdate(transformedBid);
	}

	/**
	 * Determine if the new aggregated bid has changed significantly compared to
	 * the last bid. If the aggregated bid has changed significantly compared to
	 * the last bid sent, the new aggregated bid will be sent to the parent
	 * matcher immediately. Currently, true is returned only when no bid was
	 * sent to the parent matcher before.
	 * 
	 * @param newAggregatedBid
	 *            The new aggregated bid (<code>BidInfo</code>) parameter.
	 * @return True if the new aggregated bid should be sent to the parent
	 *         matcher immediately.
	 */
	@Override
	protected boolean isAggregatedBidChangeSignificant(final BidInfo newAggregatedBid) {
		// TODO Compare new equilibrium price against last published price.
		return getLastBid() == null || super.isAggregatedBidChangeSignificant(newAggregatedBid);
	}

	/**
	 * Transform the aggregated bid, for example for peak shaving. By default,
	 * there is no transformation and no price adjustment.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 * @return The transformed bid (by default there is no transformation)
	 */
	protected BidInfo transformAggregatedBid(final BidInfo newBidInfo) {
		return newBidInfo;
	}

	/**
	 * Update market basis with the specified new market basis parameter.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 */
	@Override
	public void updateMarketBasis(final MarketBasis newMarketBasis) {
		super.updateMarketBasis(newMarketBasis);
		publishMarketBasisUpdate(newMarketBasis);
	}

	/**
	 * Update price info with the specified new price info parameter.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 */
	@Override
	public void updatePriceInfo(final PriceInfo newPriceInfo) {
		super.updatePriceInfo(newPriceInfo);
		PriceInfo adjustedPriceInfo = adjustPrice(newPriceInfo);
		if (isInfoEnabled()) {
			logInfo("Publishing (adjusted) new price info " + adjustedPriceInfo);
		}
		publishPriceInfo(adjustedPriceInfo);
	}

}
