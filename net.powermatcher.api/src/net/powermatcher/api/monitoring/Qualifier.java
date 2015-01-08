package net.powermatcher.api.monitoring;

import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * This <code>enum</code> contains the possible qualifiers of an
 * {@link AgentEvent}.
 * 
 * @author FAN
 * @version 2.0
 */
public enum Qualifier {

	MATCHER("matcher"), AGENT("agent");

	/**
	 * The description of the qualifier.
	 */
	private String description;

	/**
	 * A private constructor to create an instance of this enum.
	 * 
	 * @param description
	 *            the description of the enum value.
	 */
	private Qualifier(String description) {
		this.description = description;
	}

	/**
	 * @return the current value of description.
	 */
	public String getDescription() {
		return description;
	}

}
