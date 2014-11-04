package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;


public interface Session {
	String getAgentId();
	
	String getMatcherId();
	
	String getClusterId();
	
	String getSessionId();
	
	MarketBasis getMarketBasis();
	
	void setClusterId(String clusterId);
	
	void setMarketBasis(MarketBasis marketBasis);
	
	void updatePrice(Price newPrice);
	
	void updateBid(Bid newBid);
	
	void disconnect();
}
