package net.powermatcher.core.object;


import java.util.HashSet;
import java.util.Set;

import net.powermatcher.core.adapter.service.ConnectorKeyService;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import net.powermatcher.core.scheduler.service.TimeConnectorService;
import net.powermatcher.core.scheduler.service.TimeService;

/**
 * 
 * <p>
 * The purpose of the ConnectableObject class is that of a parent
 * class that can be used to build systems of connectable, configurable and
 * identifiable objects with a uniform logging facility.
 * </p>
 * <p>
 * The ConnectableObject is an abstract class that extends the IdentifiableObject by
 * providing a method for defining a configurable object's identity as well as
 * the connector identifier.
 * </p>
 * <p>
 * The properties can be set using a configuration object that implements the
 * ConfigurationService interface. The property names in the configuration
 * object are defined by the constants in the ConnectableObjectConfiguration class.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see ConfigurationService
 * @see ConnectableObjectConfiguration
 * @uml.annotations 
 *    uml_dependency="mmi:///#jsrctype^name=ConnectorService[jcu^name=ConnectorService.java[jpack^name=net.powermatcher.core.adapter.service[jsrcroot^srcfolder=src[project^id=net.powermatcher.core.adapter.service]]]]$uml.Interface"
 */
public abstract class ConnectableObject extends IdentifiableObject implements TimeConnectorService {

	/**
	 * Define a time source that always returns 0 for the time.
	 * This time source is used as long as no time source has been bound yet.
	 */
	private static final TimeService unboundTimeSource = new TimeService() {

		@Override
		public long currentTimeMillis() {
			return 0;
		}

		@Override
		public int getRate() {
			return 1;
		}
		
	};
	/**
	 * Define the Connector ID (String) field.
	 */
	private String connectorId;
	/**
	 * Define the time source (TimeService) that is used for obtaining real or
	 * simulated time.
	 */
	private TimeService timeSource = unboundTimeSource;

	/**
	 * Constructs an instance of this class.
	 */
	protected ConnectableObject() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter. All property settings like id, cluster id, logger and
	 * connector id should be set through the configuration parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	protected ConnectableObject(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>) to bind.
	 * @see #unbind(TimeService)
	 */
	@Override
	public void bind(final TimeService timeSource) {
		this.timeSource = timeSource;
	}

	/**
	 * Gets the list of configured adapter factory (String[]) ids.
	 * The adapter factory ids are configured using the Java simple name of the connector type interface as
	 * property key with a comma-separated list of adapter ids, or the defined name of the connector as a 
	 * property key with a comma-separated list of adapter ids.
	 * 
	 * @param connectorType
	 *            The connector service for which to return the list of adapter factory
	 *            ids.
	 * @return The list of adapter factory ids (<code>String[]</code>), or null if not configured.
	 */
	@Override
	public <T extends ConnectorService> String[] getAdapterFactory(final Class<T> connectorType) {
		if (!connectorType.isInterface()) {
			throw new IllegalArgumentException("Connector types must be identified by their interface, not a class");
		}
		String ids[];
		try {
			String name = (String)connectorType.getDeclaredField(ConnectorService.ADAPTER_FACTORY_PROPERTY_NAME_FIELD_IDENTIFIER).get(connectorType);
			ids = getProperty(name, ConnectorService.ADAPTER_FACTORY_PROPERTY_DEFAULT);
		} catch (Exception e) {
			ids = getProperty(connectorType.getSimpleName(), ConnectorService.ADAPTER_FACTORY_PROPERTY_DEFAULT);
		}
		return ids;
	}

	/**
	 * Gets the Connector ID (String) value.
	 * 
	 * @return Results of the get ID (<code>String</code>) value.
	 */
	@Override
	public String getConnectorId() {
		return this.connectorId;
	}

	/**
	 * Gets the key to uniquely identify this connector (ConnectorKeyService)
	 * value. The connector type must be passed, as the class implementing this
	 * method may implement multiple connector interfaces.
	 * 
	 * @param connectorType
	 *            The connector type (an interface extending
	 *            <code>ConnectorService</code>).
	 * @return The key for this connector ( <code>ConnectorKeyService</code>).
	 */
	@Override
	public ConnectorKeyService getConnectorKey(final Class<? extends ConnectorService> connectorType) {
		return new ConnectorKey(connectorType, this);
	}

	/**
	 * Get the connector types directly implemented by this object.
	 * 
	 * @return Connector types directly implemented by this object.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Class<ConnectorService>[] getConnectorTypes() {
		Set<Class<ConnectorService>> types = new HashSet<Class<ConnectorService>>();
		Class<?> cls = getClass();
		while (cls != null) {
			Class<?> interfaces[] = cls.getInterfaces();
			for (int i = 0; i < interfaces.length; i++) {
				if (ConnectorService.class.isAssignableFrom(interfaces[i])) {
					types.add((Class<ConnectorService>) interfaces[i]);
				}
			}
			cls = cls.getSuperclass();
		}
		return types.toArray(new Class[types.size()]);
	}

	/**
	 * Get the current or simulated time expressed as milliseconds since the
	 * time 00:00:00 UTC on January 1, 1970.
	 * 
	 * @return Current or simulated milllisecond time, or 0 if no time source
	 *         has been bound.
	 * @see System#currentTimeMillis()
	 */
	public long getCurrentTimeMillis() {
		TimeService timeSource = getTimeSource();
		if (timeSource == null) {
			return 0;
		}
		return timeSource.currentTimeMillis();
	}

	/**
	 * Get the time source bound to this object. Subclasses shall use the time
	 * source to obtain the current real-time or simulated clock.
	 * 
	 * @return The time source to this object, or null if no time source is
	 *         bound.
	 */
	public TimeService getTimeSource() {
		return this.timeSource;
	}

	/**
	 * Sets the configuration value. The connectorId property values is
	 * retrieved from this configuration value. The property names are defined
	 * by the constants in class ConnectableObjectConfiguration.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * 
	 * @see ConnectableObjectConfiguration
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
		this.connectorId = getProperty(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY, getId());
	}

	/**
	 * Unbind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>) to unbind.
	 * 
	 * @see #bind(TimeService)
	 */
	@Override
	public void unbind(final TimeService timeSource) {
		this.timeSource = unboundTimeSource;
	}

}
