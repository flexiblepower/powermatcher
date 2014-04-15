package net.powermatcher.simulator.prototype.pmcore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Auctioneer extends ActiveObject implements Matcher {
	private String id;

	private Map<String, Agent> agents = new ConcurrentHashMap<String, Agent>();
	private Map<String, Bid> bids = new ConcurrentHashMap<String, Bid>();
	private AggregatedBidListener aggregatedBidListener;

	public Auctioneer(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public void addAgent(Agent agent) {
		agents.put(agent.getId(), agent);
	}

	public void setObjectiveAgent(AggregatedBidListener aggregatedBidListener) {
		this.aggregatedBidListener = aggregatedBidListener;
	}

	@Override
	public void processBidUpdate(String agentId, Bid bid) {
		bids.put(agentId, bid);
	}

	@Override
	public void doUpdate() {
		Bid aggregated = new Bid();
		for (Bid bid : bids.values()) {
			aggregated = aggregated.add(bid);
		}

		double price = aggregated.getPrice(0);
		for (Agent child : agents.values()) {
			child.setPrice(price);
		}

		if (aggregatedBidListener != null) {
			aggregatedBidListener.aggregatedBidUpdated(aggregated);
		}
	}
}
