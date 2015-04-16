package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

/**
 * {@link MatcherEndpoint} defines the interface for classes that can receive a {@link Bid} and send a
 * {@link PriceUpdate}, containing a {@link Price} based on that {@link Bid}. A {@link MatcherEndpoint} can be linked
 * with zero or more {@link AgentEndpoint} instances. These are linked by a {@link Session}.
 *
 * @author FAN
 * @version 2.0
 */
public interface MatcherEndpoint
    extends Agent {

    /**
     * Connects this {@link MatcherEndpoint} instance to an {@link AgentEndpoint}.
     *
     * @param session
     *            the {@link Session} that will link this {@link AgentEndpoint} with a {@link MatcherEndpoint}.
     *
     * @throws IllegalArgumentException
     *             when this matcher is not connected to the cluster
     */
    void connectToAgent(Session session);

    /**
     * Notifies the {@link Agent} that this {@link MatcherEndpoint} instance is disconnected from the
     * {@link AgentEndpoint}.
     *
     * @param session
     *            the {@link Session} that used to link this {@link MatcherEndpoint} with an {@link AgentEndpoint}.
     */
    void agentEndpointDisconnected(Session session);

    /**
     * Called by the {@link AgentEndpoint} via the {@link Session} to update the {@link Bid} used by this
     * {@link AgentEndpoint} instance.
     *
     * @param session
     *            The session linking this {@link MatcherEndpoint} with a specific {@link AgentEndpoint} instance.
     * @param bidUpdate
     *            The new {@link BidUpdate}.
     */
    void handleBidUpdate(Session session, BidUpdate bidUpdate);
}
