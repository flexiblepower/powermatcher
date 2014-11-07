package net.powermatcher.api.monitoring;

import java.util.Date;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;

/**
 * The base class for update events.
 * 
 * {@link AgentRole} subclasses implement the {@link Observable} interface and
 * can send an {@link UpdateEvent} to every {@link Observer} that observes this
 * class.
 * 
 * @author FAN
 * @version 1.0
 */

public abstract class UpdateEvent {
	// TODO do we want an agent type?

	/**
	 * The id of the {@link AgentRole} subclass sending the UpdateEvent.
	 */
	private final String agentId;

	/**
	 * The id of the {@link Session} of the {@link AgentRole} subclass sending
	 * the UpdateEvent
	 */
	private final String sessionId;

	/**
	 * The {@link Date} received from the {@link TimeService}
	 */
	private final Date timestamp;

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
	 */
	public UpdateEvent(String agentId, String sessionId, Date timestamp) {
		this.agentId = agentId;
		this.sessionId = sessionId;
		this.timestamp = timestamp;
	}

	public String getAgentId() {
		return agentId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public Date getTimestamp() {
		return timestamp;
	}
}
