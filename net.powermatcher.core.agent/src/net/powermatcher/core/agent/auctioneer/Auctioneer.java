package net.powermatcher.core.agent.auctioneer;


import net.powermatcher.core.agent.auctioneer.service.PricingConnectorService;
import net.powermatcher.core.agent.auctioneer.service.PricingControlService;
import net.powermatcher.core.agent.auctioneer.service.PricingService;
import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * @author IBM
 * @version 0.9.0
 */
public class Auctioneer extends MatcherAgent implements PricingConnectorService {

	private PricingService pricingService;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #Auctioneer(Configurable)
	 */
	public Auctioneer() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #Auctioneer()
	 */
	public Auctioneer(final Configurable configuration) {
		super(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.core.agent.auctioneer.service.PricingConnectorService
	 * #
	 * bind(net.powermatcher.core.agent.auctioneer.service.PricingService
	 * )
	 */
	@Override
	public void bind(final PricingService pricingService) {
		assert this.pricingService == null;
		this.pricingService = pricingService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.core.agent.auctioneer.service.PricingConnectorService
	 * #getPricingControlService()
	 */
	@Override
	public PricingControlService getPricingControlService() {
		return new PricingControlService() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see net.powermatcher.core.agent.auctioneer.service.
			 * PricingControlService#updatePrice()
			 */
			@Override
			public void updatePrice() {
				doBidUpdate();
			}

		};
	}

	/**
	 * Handle aggregated bid update with the specified new bid parameter.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	@Override
	protected void handleAggregatedBidUpdate(final BidInfo newBidInfo) {
		PriceInfo newPriceInfo;
		PricingService pricingService = this.pricingService;
		if (this.pricingService == null) {
			newPriceInfo = newBidInfo.calculateIntersection(0);
		} else {
			newPriceInfo = pricingService.determinePrice(newBidInfo);
		}
		publishPriceInfo(newPriceInfo);
		// Call after price update so that the aggregated bid is logged with the new price info.
		super.handleAggregatedBidUpdate(newBidInfo);
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
		return super.isAggregatedBidChangeSignificant(newAggregatedBid);
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
	 * Publish price info with the specified new price info parameter.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 */
	@Override
	public void publishPriceInfo(final PriceInfo newPriceInfo) {
		if (isInfoEnabled()) {
			logInfo("Publishing new price info " + newPriceInfo);
		}
		super.publishPriceInfo(newPriceInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.core.agent.auctioneer.service.PricingConnectorService
	 * #
	 * unbind(net.powermatcher.core.agent.auctioneer.service.PricingService
	 * )
	 */
	@Override
	public void unbind(final PricingService pricingService) {
		this.pricingService = null;
	}

}
