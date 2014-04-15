package net.powermatcher.core.object.config;

import net.powermatcher.core.scheduler.service.SchedulerConnectorService;



/**
 *
 * <p>
 * Defines the interface, configuration property names and default values
 * for the configuration of a ConnectableObject instance.
 * <p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 */
public interface ActiveObjectConfiguration extends ConnectableObjectConfiguration {

	/**
	 * Define the scheduler adapter factory property (String) constant.
	 */
	public static final String SCHEDULER_ADAPTER_FACTORY_PROPERTY = SchedulerConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the scheduler adapter factory default (String) constant.
	 */
	public static final String SCHEDULER_ADAPTER_FACTORY_DEFAULT = "schedulerAdapterFactory";
	/**
	 * Define the scheduler adapter factory description (String) constant.
	 */
	public static final String SCHEDULER_ADAPTER_FACTORY_DESCRIPTION = "The adapter factory for creating the scheduling service adapter";

	/**
	 * Define the update interval property (String) name constant.
	 */
	public static final String UPDATE_INTERVAL_PROPERTY = "update.interval";
	/**
	 * Define the update interval default (int) constant.
	 */
	public static final String UPDATE_INTERVAL_DEFAULT_STR = "30";
	/**
	 * Define the update interval default (int) constant.
	 */
	public static final int UPDATE_INTERVAL_DEFAULT = Integer.valueOf(UPDATE_INTERVAL_DEFAULT_STR).intValue();
	/**
	 * Define the update interval description (int) constant.
	 */
	public static final String UPDATE_INTERVAL_DESCRIPTION = "Default update processing interval in seconds";

	/**
	 * Scheduler_adapter_factory and return the String result.
	 * 
	 * @return Results of the scheduler_adapter_factory (<code>String</code>) value.
	 */
	public String scheduler_adapter_factory();
	
	/**
	 * Update_interval and return the int result.
	 * 
	 * @return Results of the update_interval (<code>int</code>) value.
	 */
	public int update_interval();

}
