package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.data.Price;

public class OutgoingPriceUpdateEvent extends UpdateEvent {
	private final Price price;

	public OutgoingPriceUpdateEvent(String agentId, String sessionId, Date timestamp,
			Price price) {
		super(agentId, sessionId, timestamp);
		this.price = price;
	}

	public Price getPrice() {
		return price;
	}
}
