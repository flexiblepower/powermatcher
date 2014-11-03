package net.powermatcher.api;

import net.powermatcher.api.data.Price;

public interface AgentRole {
	void disconnect(Session session);

	void updatePrice(Price newPrice);
}
