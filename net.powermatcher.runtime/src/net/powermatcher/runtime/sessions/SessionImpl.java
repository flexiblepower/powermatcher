package net.powermatcher.runtime.sessions;

import java.util.UUID;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

public class SessionImpl
    implements Session {

    private final String sessionId;
    private final AgentEndpoint agentEndpoint;
    private final MatcherEndpoint matcherEndpoint;
    private final PotentialSession potentialSession;
    private final String clusterId;
    private MarketBasis marketBasis;

    public SessionImpl(AgentEndpoint agentEndpoint, MatcherEndpoint matcherEndpoint, PotentialSession potentialSession) {
        sessionId = UUID.randomUUID().toString();
        this.agentEndpoint = agentEndpoint;
        this.matcherEndpoint = matcherEndpoint;
        this.potentialSession = potentialSession;
        clusterId = matcherEndpoint.getClusterId();
    }

    @Override
    public String getAgentId() {
        return agentEndpoint.getAgentId();
    }

    @Override
    public String getMatcherId() {
        return matcherEndpoint.getAgentId();
    }

    @Override
    public String getClusterId() {
        return clusterId;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    @Override
    public void setMarketBasis(MarketBasis marketBasis) {
        if (this.marketBasis == null) {
            this.marketBasis = marketBasis;
        } else {
            throw new IllegalStateException("Received new MarketBasis for session; MarketBasis cannot be changed");
        }
    }

    @Override
    public void updatePrice(PriceUpdate priceUpdate) {
        agentEndpoint.handlePriceUpdate(priceUpdate);
    }

    @Override
    public void updateBid(BidUpdate bidUpdate) {
        matcherEndpoint.handleBidUpdate(this, bidUpdate);
    }

    @Override
    public void disconnect() {
        agentEndpoint.matcherEndpointDisconnected(this);
        matcherEndpoint.agentEndpointDisconnected(this);
        potentialSession.disconnected();
    }
}
