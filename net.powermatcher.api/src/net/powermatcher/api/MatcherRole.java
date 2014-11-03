package net.powermatcher.api;

import net.powermatcher.api.data.Bid;

public interface MatcherRole {
	Session connect(AgentRole agentRole);

	void disconnect(Session session);
	
	void updateBid(Session session, Bid newBid);
}
