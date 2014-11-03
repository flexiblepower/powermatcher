package net.powermatcher.api.monitoring;

import java.util.Date;

public abstract class UpdateEvent {
	// TODO do we want an agent type?
	
	private final String agentId, sessionId;
	
	private final Date timestamp;

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
