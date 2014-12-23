package net.powermatcher.api.connectivity;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

public interface MatcherEndpointProxy extends MatcherEndpoint {
    boolean connectRemote();

    boolean disconnectRemote();

    boolean isLocalConnected();

    boolean isRemoteConnected();

    void updateBidRemote(Bid newBid);

    void updateLocalPrice(PriceUpdate priceUpdate);

    void updateRemoteMarketBasis(MarketBasis marketBasis);

    void updateRemoteClusterId(String clusterId);
}
