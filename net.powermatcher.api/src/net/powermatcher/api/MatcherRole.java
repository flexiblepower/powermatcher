package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * A {@link MatcherRole} defines the inteface for classes that can receive a {@link Bid} and send a {@link Price}, based
 * on that {@link Bid}. A {@link MatcherRole} can be linked with zero of more {@link AgentRole} instances. These are
 * linked by a {@link Session}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public interface MatcherRole {

    /**
     * Connects this {@link MatcherRole} instance as {@link AgentRole}.
     * 
     * @param session
     *            the {@link Session} that will link this {@link AgentRole} with a {@link MatcherRole}.
     * 
     * @return <code>true</code> if the {@link AgentRole} instance could be linked to this {@link MatcherRole} instance.
     */
    boolean connectToAgent(Session session);

    /**
     * Disconnects this {@link MatcherRole} instance as {@link AgentRole}.
     * 
     * @param session
     *            the {@link Session} that will link this {@link MatcherRole} with a {@link AgentRole}.
     */
    void agentRoleDisconnected(Session session);

    /**
     * Used to update the {@link Bid} used by this {@link AgentRole} instance.
     * 
     * @param session
     *            The session linking this {@link MatcherRole} with a specific {@link AgentRole} instance.
     * @param newBid
     *            The new {@link Bid}
     */
    void updateBid(Session session, Bid newBid);
}
