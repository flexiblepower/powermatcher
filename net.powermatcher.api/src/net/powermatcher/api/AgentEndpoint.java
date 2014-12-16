package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;

/**
 * A {@link AgentEndpoint} defines the interface for classes that can receive a {@link Price} and send a {@link Bid}, based
 * on that {@link Price}. An {@link AgentEndpoint} can be linked with zero of one {@link MatcherEndpoint} instances. These are
 * linked by a {@link Session}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public interface AgentEndpoint extends Agent {

    /**
     * Connects this {@link AgentEndpoint} instance a {@link MatcherEndpoint}.
     * 
     * @param session
     *            the {@link Session} that will link this {@link AgentEndpoint} with a {@link MatcherEndpoint}.
     */
    void connectToMatcher(Session session);

    /**
     * Disconnects this {@link AgentEndpoint} instance from the {@link MatcherEndpoint}
     * 
     * @param session
     *            the {@link Session} that will couple this {@link AgentEndpoint} with its {@link MatcherEndpoint}.
     */
    void matcherEndpointDisconnected(Session session);

    /**
     * Used to update the {@link Price} used by this {@link AgentEndpoint} instance.
     * 
     * @param priceUpdate
     *            The new {@link Price}
     */
    void updatePrice(PriceUpdate priceUpdate);
}
