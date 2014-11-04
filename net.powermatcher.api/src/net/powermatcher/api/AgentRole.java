package net.powermatcher.api;

import net.powermatcher.api.data.Price;

public interface AgentRole {
	void connectToMatcher(Session session);
	
	void disconnectFromMatcher(Session session);

	void updatePrice(Price newPrice);
}
