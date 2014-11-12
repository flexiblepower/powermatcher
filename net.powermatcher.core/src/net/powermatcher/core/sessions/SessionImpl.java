package net.powermatcher.core.sessions;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;

/**
 * <p>
 * This class represents an implementation of the {@link Session} object.
 * </p>
 * 
 * <p>
 * It is responsible to hold serveral properties like sessionId, agentId, matcherId. There are two important methods.
 * UpdateBid() and updatePrice(). UpdateBid() is called by {@link Concentrator} and agents. UpdatePrice() is called by
 * the {@link Auctioneer} and {@link Concentrator}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
class SessionImpl implements Session {

    /**
     * The sessionmanager collects the connections in the cluster.
     */
    private final SessionManager sessionManager;

    /**
     * Id of the agentRole instance of the session.
     */
    private final String agentId;

    /**
     * Id of the matcherRole instance of the session.
     */
    private final String matcherId;

    /**
     * Id of the session.
     */
    private final String sessionId;

    /**
     * The {@link AgentRole} instance links to the {@link MatcherRole} in this {@link Session}.
     */
    private final AgentRole agentRole;

    /**
     * The {@link MatcherRole} instance links to the {@link AgentRole} in this {@link Session}.
     */
    private final MatcherRole matcherRole;

    /**
     * Holds the clusterId
     */
    private String clusterId = null;

    /**
     * The {@link MarketBasis} set from the {@link Auctioneer}
     */
    private MarketBasis marketBasis = null;

    public SessionImpl(SessionManager sessionManager, AgentRole agentRole, String agentId, MatcherRole matcherRole,
            String matcherId, String sessionId) {
        this.sessionManager = sessionManager;
        this.agentId = agentId;
        this.matcherId = matcherId;
        this.agentRole = agentRole;
        this.matcherRole = matcherRole;
        this.sessionId = sessionId;
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
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getClusterId() {
        if (clusterId == null) {
            throw new IllegalStateException("No clusterId has been defined");
        }
        return clusterId;
    }

    @Override
    public MarketBasis getMarketBasis() {
        if (marketBasis == null) {
            throw new IllegalStateException("No marketBasis has been defined");
        }
        return marketBasis;
    }

    @Override
    public void setClusterId(String clusterId) {
        if (this.clusterId != null) {
            throw new IllegalStateException("clusterId can only be set once");
        }
        this.clusterId = clusterId;
    }

    @Override
    public void setMarketBasis(MarketBasis marketBasis) {
        if (this.marketBasis != null) {
            throw new IllegalStateException("marketBasis can only be set once");
        }
        this.marketBasis = marketBasis;
    }

    @Override
    public void updatePrice(Price newPrice) {
        agentRole.updatePrice(newPrice);
    }

    @Override
    public void updateBid(Bid newBid) {
        matcherRole.updateBid(this, newBid);
    }

    @Override
    public void disconnect() {
        agentRole.disconnectFromMatcher(this);
        matcherRole.disconnectFromAgent(this);
        sessionManager.disconnected(this);
    }
}
