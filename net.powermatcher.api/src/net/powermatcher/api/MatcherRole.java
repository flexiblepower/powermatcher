package net.powermatcher.api;

import net.powermatcher.api.data.Bid;

public interface MatcherRole {
	boolean connectToAgent(Session session);

	void disconnectFromAgent(Session session);
	
	void updateBid(Session session, Bid newBid);
}
