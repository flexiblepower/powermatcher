package net.powermatcher.mock;

import java.util.UUID;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

public class SimpleSession
    implements Session {
    private final AgentEndpoint agent;
    private final MatcherEndpoint matcher;
    private final String sessionId;
    private MarketBasis marketBasis;

    private volatile boolean isConnected;

    public SimpleSession(AgentEndpoint agent, MatcherEndpoint matcher) {
        this.agent = agent;
        this.matcher = matcher;
        sessionId = UUID.randomUUID().toString();
        isConnected = false;
    }

    public synchronized void connect() {
        if (!isConnected) {
            matcher.connectToAgent(this);
            agent.connectToMatcher(this);
            isConnected = true;
        }
    }

    @Override
    public synchronized void disconnect() {
        if (isConnected) {
            matcher.agentEndpointDisconnected(this);
            agent.matcherEndpointDisconnected(this);
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public String getAgentId() {
        return agent.getAgentId();
    }

    @Override
    public String getMatcherId() {
        return matcher.getAgentId();
    }

    @Override
    public String getClusterId() {
        return matcher.getClusterId();
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
        this.marketBasis = marketBasis;
    }

    @Override
    public void updatePrice(PriceUpdate priceUpdate) {
        agent.handlePriceUpdate(priceUpdate);
    }

    @Override
    public void updateBid(Bid newBid) {
        matcher.handleBidUpdate(this, newBid);
    }

}
