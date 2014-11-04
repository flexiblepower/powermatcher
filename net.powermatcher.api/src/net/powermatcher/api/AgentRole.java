package net.powermatcher.api;

import net.powermatcher.api.data.Price;

public interface AgentRole {
	void connect(Session session);
	
	void disconnect(Session session);

	void updatePrice(Price newPrice);
}
