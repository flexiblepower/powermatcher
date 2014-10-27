package net.powermatcher.core.adapter.service;

import net.powermatcher.core.configurable.service.Configurable;


/**
 * 
 * The AdapterFactoryService defines a generic interface for instantiating
 * adapters.
 * 
 *  @author IBM
 * @version 0.9.0
 */
public interface AdapterFactoryService<T extends Connectable> {

	/**
	 * Create a new adapter instance for a connector and optional auxiliary adapters.
	 * @param configuration The configuration for the adapter.
	 * @param connector The primary connector for this adapter.
	 * @param factoryIndex The index in the list of factories configured for the connector for type T.
	 * @param connectorLocater The locater for optional auxiliary adapters.
	 * @return The new adapter instance.
	 */
	public Adaptable createAdapter(final Configurable configuration,
			final T connector, final ConnectorLocaterService connectorLocater, final int factoryIndex);

	public String getAdapterName();
	
}
