package net.powermatcher.simulator.prototype.pmcore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransformingConcentrator implements Matcher, Agent {
	private String id;
	private Matcher matcher;
	private Map<String, Agent> agents = new ConcurrentHashMap<String, Agent>();

	public TransformingConcentrator(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public void addAgent(Agent agent) {
		agents.put(agent.getId(), agent);
	}

	@Override
	public void processBidUpdate(String agentId, Bid bid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPrice(double price) {
		// TODO Auto-generated method stub
	
	}

}
