package net.powermatcher.core.adapter.service;


import net.powermatcher.core.configurable.service.ConfigurationService;

/**
 * 
 * The TargetAdapterFactoryService defines a generic interface for instantiating
 * generic adapters.
 * 
 *  @author IBM
 * @version 0.9.0
 */
public interface TargetAdapterFactoryService<T extends ConnectorService> extends AdapterFactoryService<T> {

	/**
	 * Create adapter with the specified configuration and agent connector
	 * parameters and return the DirectLoggingAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param connector
	 *            The connector (<code>T</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>AdapterService</code>)
	 *         value.
	 */
	public AdapterService createAdapter(final ConfigurationService configuration,
			final T connector);

}
