package net.powermatcher.simulator.prototype.pmcore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractConcentrator implements Agent, Matcher {
	private String id;
	private Matcher matcher;
	private Map<String, Agent> agents = new ConcurrentHashMap<String, Agent>();

	public AbstractConcentrator(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	protected Matcher getMatcher() {
		return matcher;
	}

	@Override
	public void addAgent(Agent agent) {
		agents.put(agent.getId(), agent);
	}

	protected Map<String, Agent> getAgents() {
		return agents;
	}

	protected void sendPriceToChildren(double price) {
		for (Agent agent : agents.values()) {
			agent.setPrice(price);
		}
	}
}
