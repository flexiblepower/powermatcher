package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.data.Price;

public class IncomingPriceUpdateEvent extends UpdateEvent {
	private final Price price;

	public IncomingPriceUpdateEvent(String agentId, String sessionId, Date timestamp,
			Price price) {
		super(agentId, sessionId, timestamp);
		this.price = price;
	}

	public Price getPrice() {
		return price;
	}
}
