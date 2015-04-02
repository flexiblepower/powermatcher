package net.powermatcher.runtime;

import java.util.UUID;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionImpl
    implements Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionImpl.class);

    final Object lock = new Object();

    private final String sessionId;
    private final AgentEndpoint agentEndpoint;
    private final MatcherEndpoint matcherEndpoint;
    private final PotentialSession potentialSession;
    private final String clusterId;
    private MarketBasis marketBasis;

    private volatile boolean connected;

    public SessionImpl(AgentEndpoint agentEndpoint, MatcherEndpoint matcherEndpoint, PotentialSession potentialSession) {
        sessionId = UUID.randomUUID().toString();
        this.agentEndpoint = agentEndpoint;
        this.matcherEndpoint = matcherEndpoint;
        this.potentialSession = potentialSession;
        clusterId = matcherEndpoint.getClusterId();
        connected = false;
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

    void setConnected() {
        if (marketBasis == null) {
            throw new IllegalStateException("No MarketBasis has been set by the matcher ["
                                            + matcherEndpoint.getAgentId()
                                            + "]");
        }
        connected = true;
    }

    @Override
    public void updatePrice(PriceUpdate priceUpdate) {
        synchronized (lock) {
            if (connected) {
                agentEndpoint.handlePriceUpdate(priceUpdate);
            } else {
                LOGGER.warn("Sending a price update before it is connected from agent ["
                            + matcherEndpoint.getAgentId()
                            + "]");
            }
        }
    }

    @Override
    public void updateBid(BidUpdate bidUpdate) {
        synchronized (lock) {
            if (connected) {
                matcherEndpoint.handleBidUpdate(this, bidUpdate);
            } else {
                LOGGER.warn("Sending a bid update before it is connected from agent ["
                            + agentEndpoint.getAgentId()
                            + "]");
            }
        }
    }

    @Override
    public void disconnect() {
        synchronized (lock) {
            connected = false;
            agentEndpoint.matcherEndpointDisconnected(this);
            matcherEndpoint.agentEndpointDisconnected(this);
            potentialSession.disconnected();
        }
    }
}
