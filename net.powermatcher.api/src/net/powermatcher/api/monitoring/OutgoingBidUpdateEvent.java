package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * An {@link OutgoingBidUpdateEvent} is sent when an {@link AgentRole} sends a
 * new {@link Bid}.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
public class OutgoingBidUpdateEvent extends UpdateEvent {

	/**
	 * The new {@link Bid} created by the {@link AgentRole} subclass.
	 */
	private final Bid bid;

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
	 * @param bid
	 *            The new {@link Price} created by the {@link AgentRole}
	 *            subclass.
	 */
	public OutgoingBidUpdateEvent(String agentId, String sessionId,
			Date timestamp, Bid bid) {
		super(agentId, sessionId, timestamp);
		this.bid = bid;
	}

	public Bid getBid() {
		return bid;
	}
}
