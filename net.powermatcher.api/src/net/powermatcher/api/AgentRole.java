package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * A {@link AgentRole} defines the inteface for classes that can receive a
 * {@link Price} and send a {@link Bid}, based on that {@link Price}. An
 * {@link AgentRole} can be linked with zero of one {@link MatcherRole}
 * instances. These are linked by a {@link Session}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public interface AgentRole {
	
	/**
	 * Connects this {@link AgentRole} instance a {@link MatcherRole}.
	 * 
	 * @param session
	 * 		the {@link Session} that will link this {@link AgentRole} with a {@link MatcherRole}.
	 */
	void connectToMatcher(Session session);
	
	/**
	 * Disconnects this {@link AgentRole} instance from the {@link MatcherRole}
	 * 
	 * @param session
	 * 		the {@link Session} that will couple this {@link AgentRole} with its {@link MatcherRole}.
	 */
	void disconnectFromMatcher(Session session);

	/**
	 * Used to update the {@link Price} used by this {@link AgentRole} instance.
	 * 
	 * @param newPrice
	 * 			The new {@link Price}
	 */
	void updatePrice(Price newPrice);
}
