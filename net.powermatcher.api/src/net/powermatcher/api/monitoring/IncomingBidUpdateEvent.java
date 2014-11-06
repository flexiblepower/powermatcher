package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.data.Bid;

public class IncomingBidUpdateEvent extends UpdateEvent {
	private String fromAgentId;
	
	private Bid bid;

	public IncomingBidUpdateEvent(String agentId, String sessionId,
			Date timestamp, String fromAgentId, Bid bid) {
		super(agentId, sessionId, timestamp);
		this.fromAgentId = fromAgentId;
		this.bid = bid;
	}

	public String getFromAgentId() {
		return fromAgentId;
	}

	public Bid getBid() {
		return bid;
	}
}
