package net.powermatcher.core.agent.framework.service;

import net.powermatcher.core.adapter.service.ConnectorService;


/**
 * 
 * The AgentConnectorService defines the interface to connect an agent
 * to another object or component. The interface defines the agent services
 * available for the connector users.
 * 
 * The interface defines methods to bind and unbind components for:
 * <ul>
 * <li>matcher connection (MatcherService)</li>
 * </ul>  
 * @author IBM
 * @version 0.9.0
 * 
 * @see MatcherService
 * @see AgentService
 */
public interface AgentConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "agent.adapter.factory";
	
	/**
	 * Bind.
	 * 
	 * @param parentMatcherAdapter
	 *            The parent matcher adapter (<code>MatcherService</code>)
	 *            parameter.
	 * @throws Exception
	 *             Exception.
	 */
	public void bind(final MatcherService parentMatcherAdapter) throws Exception;

	/**
	 * Gets the agent (AgentService) value.
	 * 
	 * @return The agent (<code>AgentService</code>) value.
	 */
	public AgentService getAgent();

	/**
	 * Unbind.
	 * 
	 * @param parentMatcherAdapter
	 *            The parent matcher adapter (<code>MatcherService</code>)
	 *            parameter.
	 */
	public void unbind(final MatcherService parentMatcherAdapter);

}
