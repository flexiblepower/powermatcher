package net.powermatcher.core.scheduler.service;


import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.adapter.service.ConnectorService;


/**
 * The SchedulerConnectorService defines the interface to connect to an object that
 * uses the ScheduledExecutorService interface to schedule tasks in real or simulated
 * time on a thread pool.
 * Through this interface the scheduler binds to the object that needs to schedule tasks.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface SchedulerConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "scheduler.adapter.factory";

	/**
	 * Bind the specified scheduler for scheduling tasks in real or simulated time.
	 * 
	 * @param scheduler
	 *            The scheduler (<code>ScheduledExecutorService</code>)
	 *            to bind.
	 * @see #unbind(ScheduledExecutorService)
	 */
	public void bind(final ScheduledExecutorService scheduler);

	/**
	 * Unbind the specified scheduler for scheduling tasks in real or simulated time.
	 * 
	 * @param scheduler
	 *            The scheduler (<code>ScheduledExecutorService</code>)
	 *            to unbind.
	 *            
	 * @see #bind(ScheduledExecutorService)
	 */
	public void unbind(final ScheduledExecutorService scheduler);

}
