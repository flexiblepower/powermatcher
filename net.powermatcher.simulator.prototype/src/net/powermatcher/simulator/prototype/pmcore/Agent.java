package net.powermatcher.simulator.prototype.pmcore;

public interface Agent {
	String getId();

	void setMatcher(Matcher matcher);

	void setPrice(double price);
}
