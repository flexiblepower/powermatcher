package net.powermatcher.simulator.prototype.pmcore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Concentrator extends ActiveObject implements Agent, Matcher {
	private Map<String, Agent> agents = new ConcurrentHashMap<String, Agent>();
	private Map<String, Bid> bids = new ConcurrentHashMap<String, Bid>();
	private Matcher matcher;

	public Concentrator(String name) {
		super(name);
	}

	@Override
	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public String getId() {
		return this.name;
	}

	@Override
	public void addAgent(Agent agent) {
		agents.put(agent.getId(), agent);
	}

	@Override
	public void processBidUpdate(String agentId, Bid bid) {
		this.bids.put(agentId, bid);
	}

	@Override
	public void doUpdate() {
		// System.out.out.println("doUpdate " + this.name + " at " + new
		// Date(this.getCurrentTimeMillis()) + "(" + new Date() + ")");

		Bid aggregated = new Bid();
		for (Bid bid : bids.values()) {
			aggregated = aggregated.add(bid);
		}

		matcher.processBidUpdate(this.getId(), aggregated);
	}

	@Override
	public void setPrice(double price) {
		// System.out.out.println("setPrice " + this.name + " at " + new
		// Date(this.getCurrentTimeMillis()) + "(" + new Date()+ ")");

		for (Agent agent : agents.values()) {
			agent.setPrice(price);
		}
	}
}
