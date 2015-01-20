package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

/**
 * {@link Session} defines the interface for a link between an {@link AgentEndpoint} with a {@link MatcherEndpoint} in a
 * Powermatcher cluster.
 * 
 * @author FAN
 * @version 2.0
 */
public interface Session {

    /**
     * @return the agentId of the {@link AgentEndpoint} of this {@link Session}.
     */
    String getAgentId();

    /**
     * @return the agentId of the {@link MatcherEndpoint} of this {@link Session}.
     */
    String getMatcherId();

    /**
     * @return the id of the cluster this {@link Session} is active in.
     */
    String getClusterId();

    /**
     * @return the id of this {@link Session} instance.
     */
    String getSessionId();

    /**
     * @return the {@link MarketBasis} used in this {@link Session}.
     */
    MarketBasis getMarketBasis();

    /**
     * @param clusterId
     *            the new <code>String</code> id of the cluster.
     */
    void setClusterId(String clusterId);

    /**
     * @param marketBasis
     *            the new marketBasis used in this {@link Session}.
     */
    void setMarketBasis(MarketBasis marketBasis);

    /**
     * Passes the {@link PriceUpdate} sent by the {@link MatcherEndpoint} to the {@link AgentEndpoint} of this
     * {@link Session}. It calls {@link AgentEndpoint#updatePrice(PriceUpdate)}.
     * 
     * @param priceUpdate
     *            The {@link PriceUpdate} passed by the {@link MatcherEndpoint} of this {@link Session}.
     */
    void updatePrice(PriceUpdate priceUpdate);

    /**
     * Passes the {@link Bid} sent by the {@link AgentEndpoint} to the {@link MatcherEndpoint} of this session. It calls @see
     * {@link AgentEndpoint#updateBid(Bid)}.
     * 
     * @param newBid
     *            The {@link Bid} passed by the {@link AgentEndpoint} of this {@link Session}.
     */
    void updateBid(Bid newBid);

    /**
     * Disconnect the {@link AgentEndpoint} from the {@link MatcherEndpoint} and calls the SessionManager to sign off.
     */
    void disconnect();
}
