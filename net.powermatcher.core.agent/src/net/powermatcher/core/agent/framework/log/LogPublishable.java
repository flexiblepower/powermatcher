package net.powermatcher.core.agent.framework.log;

import net.powermatcher.core.adapter.service.Connectable;


/**
 * 
 * The LoggingConnectorService defines the interface to connect to an object that
 * uses the LogListenerService interface to log PowerMatcher events. Through
 * this interface the adapter binds the logging interface to be used by the object.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface LogPublishable extends Connectable {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "logging.adapter.factory";

	/**
	 * Bind the specified PowerMatcher event logging publisher.
	 * 
	 * @param eventPublisher
	 *            The PowerMatcher event publisher (<code>LogListenerService</code>)
	 *            to bind.
	 * @see #unbind(Logable)
	 */
	public void bind(final Logable eventPublisher);

	/**
	 * Unbind the specified PowerMatcher event logging publisher.
	 * 
	 * @param eventPublisher
	 *            The PowerMatcher event publisher (<code>LogListenerService</code>)
	 *            to unbind.
	 *            
	 * @see #bind(Logable)
	 */
	public void unbind(final Logable eventPublisher);

}
