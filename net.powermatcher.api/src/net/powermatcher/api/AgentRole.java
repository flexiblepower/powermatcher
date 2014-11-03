package net.powermatcher.api;

import net.powermatcher.api.data.Price;

public interface AgentRole {
	 void updatePrice(Price newPrice);

	// TODO: move to session
	// public void updateMarketBasis(final MarketBasis newMarketBasis);
}
