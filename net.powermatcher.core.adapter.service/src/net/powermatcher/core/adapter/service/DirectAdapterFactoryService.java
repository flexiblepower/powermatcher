package net.powermatcher.core.adapter.service;


import net.powermatcher.core.configurable.service.ConfigurationService;

/**
 * 
 * The DirectAdapterFactoryService defines a generic interface for instantiating
 * direct adapters for connections from a connector of type T to a connector of type F.
 * 
 *  @author IBM
 * @version 0.9.0
 */
public interface DirectAdapterFactoryService<T extends ConnectorService, F extends ConnectorService> extends AdapterFactoryService<T> {

	/**
	 * Create adapter with the specified configuration and agent connector
	 * parameters and return the DirectLoggingAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param sourceConnector
	 *            The source connector (<code>T</code>)
	 *            parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>AdapterService</code>)
	 *         value.
	 */
	public AdapterService createAdapter(final ConfigurationService configuration,
			final T sourceConnector, final F targetConnector);

	/**
	 * Get the target connector ids configured by the connector for this factory.
	 * @param connector The connector to get the target connector ids from
	 * @return The target connector ids configured by the connector.
	 */
	public String[] getTargetConnectorIds(final T connector);

}
