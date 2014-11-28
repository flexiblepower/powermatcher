package net.powermatcher.api.connectivity;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

public interface AgentEndpointProxy extends AgentEndpoint {
	boolean isLocalConnected();

	boolean isRemoteConnected();

	void updateLocalBid(Bid newBid);
	
	void updateRemotePrice(Price newPrice);
}
