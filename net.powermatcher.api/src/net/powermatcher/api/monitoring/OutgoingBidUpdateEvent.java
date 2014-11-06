package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.data.Bid;

public class OutgoingBidUpdateEvent extends UpdateEvent {
	private final Bid bid;

	public OutgoingBidUpdateEvent(String agentId, String sessionId,
			Date timestamp, Bid bid) {
		super(agentId, sessionId, timestamp);
		this.bid = bid;
	}
	
	public Bid getBid() {
		return bid;
	}
}
