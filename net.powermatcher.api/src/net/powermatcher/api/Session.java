package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

/**
 * The {@link Session} interface defines the basic functionality needed to link an {@link AgentRole} with a
 * {@link MatcherRole} object.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
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
