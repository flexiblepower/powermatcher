package net.powermatcher.core.agent.framework.service;

import net.powermatcher.core.adapter.service.Connectable;


/**
 * 
 * The MatcherConnectorService defines the interface to connect a matcher agent
 * to another object or component. The interface defines the matcher agent services
 * available for the connector users.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ParentConnectable extends Connectable {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "matcher.adapter.factory";

	/**
	 * Bind with the agent adapter of the child agent.
	 * 
	 * @param childAgentAdapter
	 *            The child agent adapter (<code>AgentService</code>) parameter.
	 * @throws Exception
	 *             Exception.
	 */
	public void bind(final DownMessagable childAgentAdapter) throws Exception;

	/**
	 * Gets the matcher (MatcherService) value.
	 * 
	 * @return The matcher (<code>MatcherService</code>) value.
	 */
	public UpMessagable getMatcher();

	/**
	 * Unbind.
	 * 
	 * @param childAgentAdapter
	 *            The child agent adapter (<code>AgentService</code>) parameter.
	 */
	public void unbind(final DownMessagable childAgentAdapter);

}
