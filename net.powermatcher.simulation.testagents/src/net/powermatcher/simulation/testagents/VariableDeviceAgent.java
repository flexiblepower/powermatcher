package net.powermatcher.simulation.testagents;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.configurable.service.ConfigurationService;

public class VariableDeviceAgent extends Agent {
	private static final int maxGeneration = -1000;
	private static final int minGeneration = 0;

	public VariableDeviceAgent() {
		super();
	}

	public VariableDeviceAgent(ConfigurationService configuration) {
		super(configuration);
	}

	@Override
	protected void doBidUpdate() {
		BidInfo bid = generateBid();
		getLogger().info("Generated bid: {}", bid);
		this.publishBidUpdate(bid);
	}

	protected BidInfo generateBid() {
		MarketBasis marketBasis = getCurrentMarketBasis();
		int minPrice = marketBasis.toNormalizedPrice(marketBasis.getMinimumPrice());
		int maxPrice = marketBasis.toNormalizedPrice(marketBasis.getMaximumPrice());

		return new BidInfo(marketBasis, new PricePoint[] { new PricePoint(minPrice, minGeneration),
				new PricePoint(maxPrice, maxGeneration) });
	}
}
