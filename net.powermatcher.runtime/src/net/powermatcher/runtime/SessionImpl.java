package net.powermatcher.runtime;

import java.util.UUID;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

public class SessionImpl
    implements Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionImpl.class);

    private final String sessionId;
    private final AgentEndpoint agentEndpoint;
    private final MatcherEndpoint matcherEndpoint;
    private final PotentialSession potentialSession;
    private final String agentId, matcherId, clusterId;
    private MarketBasis marketBasis;
    private final FlexiblePowerContext context;

    private volatile boolean connected;

    public SessionImpl(AgentEndpoint agentEndpoint,
                       MatcherEndpoint matcherEndpoint,
                       PotentialSession potentialSession,
                       FlexiblePowerContext context) {
        sessionId = UUID.randomUUID().toString();
        this.agentEndpoint = agentEndpoint;
        this.matcherEndpoint = matcherEndpoint;
        this.potentialSession = potentialSession;
        this.context = context;

        agentId = agentEndpoint.getAgentId();
        matcherId = matcherEndpoint.getAgentId();
        clusterId = matcherEndpoint.getStatus().getClusterId();

        connected = false;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public String getMatcherId() {
        return matcherId;
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
            throw new IllegalStateException("No MarketBasis has been set by the matcher [" + matcherId + "]");
        }
        connected = true;
    }

    @Override
    public synchronized void updatePrice(final PriceUpdate priceUpdate) {
        if (connected) {
            // PriceUpdate is handled in a separate runnable to avoid deadlocks
            context.submit(new Runnable() {
                @Override
                public void run() {
                    agentEndpoint.handlePriceUpdate(priceUpdate);
                }
            });
        } else {
            LOGGER.debug("Sending a price update while not connected from agent [" + agentId + "]");
        }
    }

    @Override
    public synchronized void updateBid(final BidUpdate bidUpdate) {
        if (connected) {
            // BidUpdate is handled in a separate runnable to avoid deadlocks
            context.submit(new Runnable() {
                @Override
                public void run() {
                    matcherEndpoint.handleBidUpdate(SessionImpl.this, bidUpdate);
                }
            });
        } else {
            LOGGER.debug("Sending a bid update while not connected from agent [" + agentId + "]");
        }
    }

    @Override
    public synchronized void disconnect() {
        connected = false;
        agentEndpoint.matcherEndpointDisconnected(this);
        matcherEndpoint.agentEndpointDisconnected(this);
        potentialSession.disconnected();
    }
}
