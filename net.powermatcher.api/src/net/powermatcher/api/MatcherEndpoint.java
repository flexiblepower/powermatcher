package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * A {@link MatcherEndpoint} defines the interface for classes that can receive a {@link Bid} and send a {@link Price},
 * based on that {@link Bid}. A {@link MatcherEndpoint} can be linked with zero of more {@link AgentEndpoint} instances.
 * These are linked by a {@link Session}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public interface MatcherEndpoint extends Agent {

    /**
     * Connects this {@link MatcherEndpoint} instance as {@link AgentEndpoint}.
     * 
     * @param session
     *            the {@link Session} that will link this {@link AgentEndpoint} with a {@link MatcherEndpoint}.
     * 
     * @return <code>true</code> if the {@link AgentEndpoint} instance could be linked to this {@link MatcherEndpoint}
     *         instance.
     */
    boolean connectToAgent(Session session);

    /**
     * Disconnects this {@link MatcherEndpoint} instance as {@link AgentEndpoint}.
     * 
     * @param session
     *            the {@link Session} that will link this {@link MatcherEndpoint} with a {@link AgentEndpoint}.
     */
    void agentEndpointDisconnected(Session session);

    /**
     * Used to update the {@link Bid} used by this {@link AgentEndpoint} instance.
     * 
     * @param session
     *            The session linking this {@link MatcherEndpoint} with a specific {@link AgentEndpoint} instance.
     * @param newBid
     *            The new {@link Bid}
     */
    void updateBid(Session session, Bid newBid);
}
