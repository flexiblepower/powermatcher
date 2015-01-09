package net.powermatcher.mock;

import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class MockObserver implements AgentObserver {

	private boolean hasReceivedEvent;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(AgentEvent event) {
		this.hasReceivedEvent = true;
	}

	public boolean hasReceivedEvent() {
		return hasReceivedEvent;
	}
}
