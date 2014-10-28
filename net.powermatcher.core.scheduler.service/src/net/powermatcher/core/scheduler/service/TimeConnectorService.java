package net.powermatcher.core.scheduler.service;



import net.powermatcher.core.adapter.service.ConnectorService;


/**
 * The TimeConnectorService defines the interface to connect to an object that
 * uses the TimeService to obtain the associated real or simulated time.
 * Through this interface the scheduler's time source binds to the object that needs to get timestamps.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface TimeConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "time.adapter.factory";

	/**
	 * Bind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>)
	 *            to bind.
	 * @see #unbind(TimeService)
	 */
	public void bind(final TimeService timeSource);

	/**
	 * Unbind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>)
	 *            to unbind.
	 *            
	 * @see #bind(TimeService)
	 */
	public void unbind(final TimeService timeSource);


}
