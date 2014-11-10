package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Price;

/**
 * An {@link OutgoingPriceUpdateEvent} is sent when an {@link AgentRole} sends a
 * new {@link Price}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class OutgoingPriceUpdateEvent extends UpdateEvent {

	/**
	 * The new {@link Price} created by the {@link AgentRole} subclass.
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
	 *            The new {@link Price} created by the {@link AgentRole}
	 *            subclass.
	 */
	public OutgoingPriceUpdateEvent(String agentId, String sessionId,
			Date timestamp, Price price) {
		super(agentId, sessionId, timestamp);
		this.price = price;
	}

	public Price getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return OutgoingPriceUpdateEvent.class.getSimpleName() + " " +
				super.toString() + ", price = " + price.toString();
	}
}
