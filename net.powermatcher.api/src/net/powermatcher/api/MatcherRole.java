package net.powermatcher.api;

import net.powermatcher.api.data.Bid;

public interface MatcherRole {
	void connect(Session session);

	void disconnect(Session session);
	
	void updateBid(Session session, Bid newBid);
}
