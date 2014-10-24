package net.powermatcher.core.adapter.component;


import java.util.HashMap;
import java.util.Map;

import net.powermatcher.core.adapter.service.Adaptable;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.IdentifiableObject;
import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;

import org.osgi.framework.BundleContext;

/**
 * 
 * @author IBM
 * @version 0.9.0
 */
public abstract class AdapterFactoryComponent<T extends Connectable> extends IdentifiableObject {

	/**
	 * 
	 */
	protected BundleContext context;

	/**
	 * 
	 */
	private Map<T, ConnectableRegistration<Adaptable>> adapterByConnector = new HashMap<T, ConnectableRegistration<Adaptable>>();

	
	/**
	 * 
	 */
	private Map<Adaptable, Integer> adapterReferences = new HashMap<Adaptable, Integer>();

	/**
	 * 
	 */
	private Map<String, Object> properties;

	/**
	 * 
	 */
	AdapterFactoryComponent() {
		super();
	}

	/**
	 * Create the configuration for an adapter for a given connector.
	 * The configuration is derived from the connectable component configuration, using the factory configuration
	 * to provide default values. If the connector or factory configuration specifies a connector.id property,
	 * he connector.id is used as the id for the adapter. Otherwise, the id of the adapter is the
	 * same as the id of the connectable component.
	 * @param connector The connector of the connectable component (usually the connectable component itself).
	 * @return The configuration for the adapter derived from the configuration of the connectable and the factory.
	 * @throws Exception
	 */
	protected Configurable createAdapterConfiguration(final T connector) throws Exception {
		Map<String, Object> adapterProperties = new HashMap<String, Object>(this.properties);
		Map<String, Object> connectableConfiguration = connector.getConfiguration().getProperties();
		adapterProperties.putAll(connectableConfiguration);
		/*
		 * If the connectable object explicitly specifies connector.id, the
		 * adapter must be create with id = connector.id of connectable. This
		 * allows shared adapters (like for example for messaging connections)
		 * to be created by specifying the same connector.id in different
		 * connecables.
		 */
		String connectorId = (String) connectableConfiguration.get(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY);
		if (connectorId != null) {
			adapterProperties.put(IdentifiableObjectConfiguration.ID_PROPERTY, connectorId);
			String factoryConnectorId = (String)this.properties.get(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY);
			if (factoryConnectorId != null) {
				adapterProperties.put(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY,
						this.properties.get(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY));
			}
		}
		return new BaseConfiguration(adapterProperties);
	}

	/**
	 * Get the Java type of the connector T. Due to type erasure it is necessary
	 * to gave a method return the type explicitly for use in the call to
	 * getTargetConnectorIds.
	 * 
	 * @see DirectAdapterFactoryService#getTargetConnectorIds(Connectable)
	 * @return The Java type of the connector T.
	 */
	protected abstract Class<T> getConnectorType();

	/**
	 * @param context
	 * @param properties
	 */
	protected void initialize(final BundleContext context, final Map<String, Object> properties) {
		this.context = context;
		this.properties = properties;
		Configurable configuration = new BaseConfiguration(properties);
		setConfiguration(configuration);
	}

	/**
	 * Register the adapter created for a connector.
	 * @param connector
	 * @param adapter
	 */
	protected void registerAdapter(final T connector, final Adaptable adapter) {
		ConnectableRegistration<Adaptable> adapterRegistration = new ConnectableRegistration<Adaptable>(adapter);
		adapterRegistration.register(this.context);
		this.adapterByConnector.put(connector, adapterRegistration);
	}

	/**
	 * Get the adapter registered for a connector, if any.
	 * @param connector
	 * @return The adapter created for a connector, or null if no adapter has been registered,
	 */
	protected Adaptable getAdapter(final T connector) {
		ConnectableRegistration<Adaptable>  adapterRegistration = this.adapterByConnector.get(connector);
		if (adapterRegistration != null) {
			return adapterRegistration.getConnectable();
		}
		return null;
	}

	/**
	 * Unregister the adapter created for a connector.
	 * @param connector
	 */
	protected void unregisterAdapter(final T connector) {
		ConnectableRegistration<Adaptable>  adapterRegistration = this.adapterByConnector.remove(connector);
		adapterRegistration.unregister();
	}

	/**
	 * Increase the reference count for an adapter that can be shared between components.
	 * @param adapter
	 * @return True if this is the first reference, so the adapter must be initialized.
	 */
	protected boolean addAdapterReference(Adaptable adapter) {
		Integer referenceCount = this.adapterReferences.get(adapter);
		if (referenceCount == null) {
			this.adapterReferences.put(adapter, Integer.valueOf(1));
			return true;
		}
		this.adapterReferences.put(adapter, Integer.valueOf(referenceCount.intValue() + 1));					
		return false;
	}

	/**
	 * Decrease the reference count for an adapter that can be shared between components.
	 * @param adapter
	 * @return True if this was the last reference, so the adapter can be cleaned up.
	 */
	protected boolean removeAdapterReference(Adaptable adapter) {
		Integer referenceCount = this.adapterReferences.remove(adapter);
		if (referenceCount.intValue() == 1) {
			return true;
		}
		this.adapterReferences.put(adapter, Integer.valueOf(referenceCount.intValue() - 1));
		return false;
	}

}
