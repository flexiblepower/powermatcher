package net.powermatcher.core.adapter.component;

import java.util.Dictionary;
import java.util.Hashtable;

import net.powermatcher.core.adapter.service.Connectable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * 
 * @author IBM
 * @version 0.9.0
 * 
 */
public class ConnectableRegistration<T> {
	
	/**
	 * 
	 */
	private T connectable;
	/**
	 * 
	 */
	private ServiceRegistration serviceRegistration;

	/**
	 * @param connectable
	 */
	public ConnectableRegistration(T connectable) {
		super();
		this.connectable = connectable;
	}

	/**
	 * Register all ConnectorServices interfaced in the OSGi service registry.
	 * Registers all connector services implemented by the connectable or any of its super 
	 * classes.
	 * @param context
	 */
	public void register(BundleContext context) {
		if (connectable instanceof Connectable) {
			Connectable connectorService = (Connectable) connectable;
			Class<Connectable>[] connectorTypes = connectorService.getConnectorTypes();
			if (connectorTypes.length > 0) {
				String[] serviceNames = new String[connectorTypes.length];
				for (int i = 0; i < serviceNames.length; i++) {
					serviceNames[i] = connectorTypes[i].getName();
				}
				Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(connectorService
						.getConfiguration().getProperties());
				this.serviceRegistration = context
						.registerService(serviceNames, connectable, serviceProperties);
			}
		}
	}

	/**
	 * Unregister all ConnectorServices interfaced from the OSGi service registry.
	 * Unrgisters all connector services implemented by the connectable or any of its super 
	 * classes.
	 * @return The connectable of this connectable registration.
	 */
	public T unregister() {
		if (this.serviceRegistration != null) {
			this.serviceRegistration.unregister();
			this.serviceRegistration = null;
		}
		return connectable;
	}

	public T getConnectable() {
		return connectable;
	}

}
