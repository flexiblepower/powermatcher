package net.powermatcher.simulator.prototype.pmcore;

public interface Matcher {
	void addAgent(Agent agent);

	void processBidUpdate(String agentId, Bid bid);
}
