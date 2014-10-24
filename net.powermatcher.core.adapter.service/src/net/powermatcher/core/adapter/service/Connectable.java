package net.powermatcher.core.adapter.service;



import net.powermatcher.core.configurable.service.Configurable;

/**
 * <p>
 * Defines the generic interface for an object or component to 
 * connect it to other objects or components.
 * </p>
 * <p>
 * Objects are linked via a connector interface which
 * should be implemented by one of the objects and used by the other.
 * The interface describes methods to identify the object connector
 * using an connector id, a cluster id and a name.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface Connectable {
	
	/**
	 * Each subtype of ConnectorService must declare a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME_FIELD_IDENTIFIER = "ADAPTER_FACTORY_PROPERTY_NAME";

	/**
	 * The default adapter factory id.
	 */
	public static final String EMPTY_ID = "";
	/**
	 * The default value if the adapter factory property is undefined.
	 */
	public static final String[] ADAPTER_FACTORY_PROPERTY_DEFAULT = new String[] { EMPTY_ID };

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return The Cluster ID (<code>String</code>) value.
	 */
	public String getClusterId();

	/**
	 * Gets the list of configured adapter factory (String[]) ids.
	 * The adapter factory ids are configured using the adapter factory property name of the ConnectorService
	 * with a comma-separated list of adapter ids.
	 * 
	 * @param connectorType
	 *            The connector service for which to return the list of adapter factory
	 *            ids.
	 * @return The list of adapter factory ids (<code>String[]</code>), or null if not configured.
	 * @see #ADAPTER_FACTORY_PROPERTY_NAME_FIELD_IDENTIFIER
	 */
	public <T extends Connectable> String[] getAdapterFactory(Class<T> connectorType);

	/**
	 * Gets the configuration (ConfigurationService) of the connector's component.
	 * 
	 * @return The configuration (<code>ConfigurationService</code>) value.
	 */
	public Configurable getConfiguration();

	/**
	 * Gets the ID (String) value.
	 * 
	 * @return The ID (<code>String</code>) value.
	 */
	public String getId();

	/**
	 * Gets the connector ID (String) value.
	 * 
	 * @return The connector ID (<code>String</code>) value.
	 */
	public String getConnectorId();

	/**
	 * Gets the key to uniquely identify this connector (ConnectorKey) value.
	 * The connector type must be passed, as the class implementing this method
	 * may implement multiple connector interfaces. 
	 * 
	 * @param connectorType The connector type (an interface extending
	 *         <code>ConnectorService</code>).
	 * @return The key for this connector (
	 *         <code>ConnectorKey</code>).
	 */
	public ConnectorKeyService getConnectorKey(Class<? extends Connectable> connectorType);

	/**
	 * Get the connector types directly implemented by this object.
	 * @return Connector types directly implemented by this object.
	 */
	public Class<Connectable> [] getConnectorTypes();

	/**
	 * Gets the adapter name (String) value.
	 * 
	 * @return The name of the adapter providing the connector service (
	 *         <code>String</code>) value.
	 */
	public String getName();

	/**
	 * Is enabled. Defines if the connector is available.
	 * 
	 * @return The is enabled (<code>boolean</code>) value.
	 */
	public boolean isEnabled();

}
