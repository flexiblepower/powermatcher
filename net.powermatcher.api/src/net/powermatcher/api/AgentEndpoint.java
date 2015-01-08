package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;

/**
 * {@link AgentEndpoint} defines the interface for classes that can receive a
 * {@link PriceUpdate} and send a {@link Bid} , based on the {@link Price} of
 * that {@link PriceUpdate}. An {@link AgentEndpoint} can be linked with zero or
 * one {@link MatcherEndpoint} instances. These are linked by a {@link Session}.
 * 
 * @author FAN
 * @version 2.0
 */
public interface AgentEndpoint extends Agent {

	/**
	 * Connects this {@link AgentEndpoint} instance to a {@link MatcherEndpoint}
	 * .
	 * 
	 * @param session
	 *            the {@link Session} that will link this {@link AgentEndpoint}
	 *            with a {@link MatcherEndpoint}.
	 */
	void connectToMatcher(Session session);

	/**
	 * Disconnects this {@link AgentEndpoint} instance from a
	 * {@link MatcherEndpoint}.
	 * 
	 * @param session
	 *            the {@link Session} that links this {@link AgentEndpoint} with
	 *            its {@link MatcherEndpoint}.
	 */
	void matcherEndpointDisconnected(Session session);

	/**
	 * Called by {@link MatcherEndpoint} to update the {@link Price} used by
	 * this {@link AgentEndpoint} instance.
	 * 
	 * @param priceUpdate
	 *            The new {@link Price}, wrapped in a {@link PriceUpdate}, along
	 *            with the id of the {@link Bid} it was based on.
	 */
	void updatePrice(PriceUpdate priceUpdate);
}
