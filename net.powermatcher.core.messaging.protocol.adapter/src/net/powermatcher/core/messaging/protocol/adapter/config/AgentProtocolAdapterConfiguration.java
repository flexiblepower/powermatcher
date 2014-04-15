package net.powermatcher.core.messaging.protocol.adapter.config;

import net.powermatcher.core.messaging.protocol.adapter.AgentProtocolAdapter;


/**
 * Defines the configuration properties, default values and constants for a AgentProtocolAdapter object.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see AgentProtocolAdapter
 */
public interface AgentProtocolAdapterConfiguration extends ProtocolAdapterConfiguration {
	/**
	 * Define the agent protocol property (String) constant.
	 */
	public static final String AGENT_PROTOCOL_PROPERTY = "agent.messaging.protocol";

}
