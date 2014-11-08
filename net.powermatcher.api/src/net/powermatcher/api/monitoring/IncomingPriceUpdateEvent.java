package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Price;

/**
 * An {@link IncomingPriceUpdateEvent} is sent when an {@link AgentRole}
 * receives a new {@link Price}.
 * 
 * @author FAN
 * @version 1.0
 */
public class IncomingPriceUpdateEvent extends UpdateEvent {

	/**
	 * The received {@link Price}
	 */
	private final Price price;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @param agentId
	 *            The id of the {@link AgentRole} subclass sending the
	 *            UpdateEvent.
	 * @param sessionId
	 *            The id of the {@link Session} of the {@link AgentRole}
	 *            subclass sending the UpdateEvent
	 * @param timestamp
	 *            The {@link Date} received from the {@link TimeService}
	 * @param price
	 *            The received {@link Price}.
	 */
	public IncomingPriceUpdateEvent(String agentId, String sessionId,
			Date timestamp, Price price) {
		super(agentId, sessionId, timestamp);
		this.price = price;
	}

	public Price getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return IncomingPriceUpdateEvent.class.getSimpleName() + " " +
				super.toString() + ", price = " + price.toString();
	}
}
