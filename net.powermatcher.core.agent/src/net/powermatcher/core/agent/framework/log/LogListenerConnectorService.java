package net.powermatcher.core.agent.framework.log;


import net.powermatcher.core.adapter.service.ConnectorService;

/**
 * The LogListenerConnectorService defines the interface to connect a log listener
 * to another object or component. The interface defines the log listener services
 * available for the connector users.
 *  
 * @author IBM
 * @version 0.9.0
 */
public interface LogListenerConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "log.listener.adapter.factory";

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	public void bind() throws Exception;

	/**
	 * Gets the log listener (LogListenerService) value.
	 * 
	 * @return The log listener (<code>LogListenerService</code>) value.
	 */
	public LogListenerService getLogListener();

	/**
	 * Unbind.
	 */
	public void unbind();

}
