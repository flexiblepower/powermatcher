package net.powermatcher.core.adapter.service;


import net.powermatcher.core.configurable.service.ConfigurationService;

/**
 * 
 * The SourceAdapterFactoryService defines a generic interface for instantiating
 * indirect adapters for connections from a connector of type T to some identified target connector.
 * 
 *  @author IBM
 * @version 0.9.0
 */
public interface SourceAdapterFactoryService<T extends ConnectorService> extends AdapterFactoryService<T> {

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
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>DirectLoggingAdapter</code>)
	 *         value.
	 */
	public AdapterService createAdapter(final ConfigurationService configuration,
			final T sourceConnector, final String targetConnectorId);

	/**
	 * Get the target connector ids configured by the connector for this factory.
	 * @param connector The connector to get the target connector ids from
	 * @return The target connector ids configured by the connector.
	 */
	public String[] getTargetConnectorIds(final T connector);

}
