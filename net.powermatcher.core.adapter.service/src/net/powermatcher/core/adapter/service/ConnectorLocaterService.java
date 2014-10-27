package net.powermatcher.core.adapter.service;


/**
 * Service interface for locating the connector of a specific
 * type by object identifier in a connector registry.
 * The locater service is used by adapters that directly connect to
 * the adapter of another identifyable object.
 * 
 * @author IBM
 * @version 0.9.0
 */

public interface ConnectorLocaterService {
	
	/**
	 * Locate a connector in the service registry.
	 * @param connectorClass The connector interface type to locate.
	 * @param clusterId The cluster in which to locate the connector.
	 * @param connectorId The id of the connector.
	 * @return The connector.
	 * @throws Exception if the required connector was not found in the registry.
	 */
	public <T extends Connectable> T locateConnector(Class<T> connectorClass, String clusterId, String connectorId) throws Exception;

}
