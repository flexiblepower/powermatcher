package net.powermatcher.core.object.config;

import net.powermatcher.core.object.ConnectableObject;
import net.powermatcher.core.scheduler.service.TimeConnectorService;


/**
 * 
 * <p>
 * Defines the property names of the configuration object
 * that sets the properties of the ConnectableObject instance.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ConnectableObject
 */
public interface ConnectableObjectConfiguration extends IdentifiableObjectConfiguration {
	/**
	 * Define the connector id property (String) name.
	 */
	public static final String CONNECTOR_ID_PROPERTY = "connector.id";

	/**
	 * Define the connector ID description (String) constant.
	 */
	public static final String CONNECTOR_ID_DESCRIPTION = "ID to use for adapter or connection, by default the same as the id";

	/**
	 * Define the time adapter factory property (String) constant.
	 */
	public static final String TIME_ADAPTER_FACTORY_PROPERTY = TimeConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the time adapter factory default (String) constant.
	 */
	public static final String TIME_ADAPTER_FACTORY_DEFAULT = "timeAdapterFactory";
	/**
	 * Define the time adapter factory description (String) constant.
	 */
	public static final String TIME_ADAPTER_FACTORY_DESCRIPTION = "The adapter factory for creating the time service adapter";

	/**
	 * Connector ID and return the String result.
	 * 
	 * @return Results of the connector ID (<code>String</code>) value.
	 */
	public String connector_id();

	/**
	 * Time_adapter_factory and return the String result.
	 * 
	 * @return Results of the time_adapter_factory (<code>String</code>) value.
	 */
	public String time_adapter_factory();
	
}
