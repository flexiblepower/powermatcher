package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

/**
 * The {@link Session} interface defines the basic functionality needed to link an {@link AgentEndpoint} with a
 * {@link MatcherEndpoint} object.
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

    void updatePrice(PriceUpdate priceUpdate);

    void updateBid(Bid newBid);

    void disconnect();
}
