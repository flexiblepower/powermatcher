package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;

/**
 * An {@link IncomingBidUpdateEvent} is sent when an {@link AgentRole} receives
 * a new {@link Bid}.
 * 
 * @author FAN
 * @version 1.0
 */
public class IncomingBidUpdateEvent extends UpdateEvent {

	/**
	 * The id of the Agent that sent the {@link Bid}
	 */
	private String fromAgentId;

	/**
	 * The received {@link Bid}
	 */
	private Bid bid;

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
	 * @param fromAgentId
	 *            The id of the Agent that sent the {@link Bid}.
	 * @param bid
	 *            The received {@link Bid}.
	 */
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
