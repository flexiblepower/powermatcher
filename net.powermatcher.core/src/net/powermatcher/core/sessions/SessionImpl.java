package net.powermatcher.core.sessions;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
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
public class SessionImpl implements Session {

    /**
     * The sessionmanager collects the connections in the cluster.
     */
    private final SessionManager sessionManager;

    /**
     * Id of the agentEndpoint instance of the session.
     */
    private final String agentId;

    /**
     * Id of the matcherEndpoint instance of the session.
     */
    private final String matcherId;

    /**
     * Id of the session.
     */
    private final String sessionId;

    /**
     * The {@link AgentEndpoint} instance links to the {@link MatcherEndpoint} in this {@link Session}.
     */
    private final AgentEndpoint agentEndpoint;

    /**
     * The {@link MatcherEndpoint} instance links to the {@link AgentEndpoint} in this {@link Session}.
     */
    private final MatcherEndpoint matcherEndpoint;

    /**
     * Holds the clusterId
     */
    private String clusterId = null;

    /**
     * The {@link MarketBasis} set from the {@link Auctioneer}
     */
    private MarketBasis marketBasis = null;

    public SessionImpl(SessionManager sessionManager, AgentEndpoint agentEndpoint, String agentId,
            MatcherEndpoint matcherEndpoint, String matcherId, String sessionId) {
        this.sessionManager = sessionManager;
        this.agentId = agentId;
        this.matcherId = matcherId;
        this.agentEndpoint = agentEndpoint;
        this.matcherEndpoint = matcherEndpoint;
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
        return clusterId;
    }

    @Override
    public MarketBasis getMarketBasis() {
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
    public void updatePrice(PriceUpdate priceUpdate) {
        agentEndpoint.updatePrice(priceUpdate);
    }

    @Override
    public void updateBid(Bid newBid) {
        matcherEndpoint.updateBid(this, newBid);
    }

    @Override
    public void disconnect() {
        agentEndpoint.matcherEndpointDisconnected(this);
        matcherEndpoint.agentEndpointDisconnected(this);
        sessionManager.disconnected(this);
    }

    public AgentEndpoint getAgentEndpoint() {
        return agentEndpoint;
    }

    public MatcherEndpoint getMatcherEndpoint() {
        return matcherEndpoint;
    }

    @Override
    public boolean equals(Object obj) {
        SessionImpl that = (SessionImpl) ((obj instanceof SessionImpl) ? obj : null);
        if (that == null) {
            return false;
        }

        if (this == that) {
            return true;
        }

        return this.agentId.equals(that.agentId) && this.clusterId.equals(that.clusterId)
                && this.agentEndpoint.equals(that.agentEndpoint) && this.matcherId.equals(that.matcherId)
                && this.marketBasis.equals(that.marketBasis) && this.matcherEndpoint.equals(that.matcherEndpoint)
                && this.sessionId.equals(that.sessionId);
    }
}
