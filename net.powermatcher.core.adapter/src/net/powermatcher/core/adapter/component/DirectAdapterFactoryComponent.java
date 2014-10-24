package net.powermatcher.core.adapter.component;


import java.util.Map;

import net.powermatcher.core.adapter.DirectConnectorFactoryTracker;
import net.powermatcher.core.adapter.DirectConnectorTrackerListener;
import net.powermatcher.core.adapter.service.Adaptable;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;

import org.osgi.framework.BundleContext;


/**
 * The DirectAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a DirectSourceAdapter. A DirectConnectorFactoryTracker will bind the adapter component to a direct source adapter
 * component that implements the T interface if that component 
 * instance has the same connector id and cluster id.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectAdapterFactoryService
 * @see DirectConnectorFactoryTracker
 * @see DirectConnectorTrackerListener
 * @see Adaptable
 */
public abstract class DirectAdapterFactoryComponent<T extends Connectable, F extends Connectable> extends AdapterFactoryComponent<T> {

	/**
	 * Define the adapter factory (DirectAdapterFactory) field.
	 */
	private DirectAdapterFactoryService<T, F> factory; 

	/**
	 * Define the connector tracker
	 * (DirectConnectorFactoryTracker<T>) field.
	 */
	private DirectConnectorFactoryTracker<T, F> connectorTracker;

	/**
	 * Constructs an instance of this class.
	 */
	public DirectAdapterFactoryComponent(DirectAdapterFactoryService<T, F> factory) {
		this.factory = factory;
		this.connectorTracker = new DirectConnectorFactoryTracker<T, F>(
				new DirectConnectorTrackerListener<T, F>() {
					/**
					 * Bind with the specified source and target connector parameters.
					 * 
					 * @param sourceConnector
					 *            The source connector (<code>T</code>) parameter.
					 * @param targetConnector
					 *            The target connector (<code>F</code>) parameter.
					 */
					@Override
					public void bind(T sourceConnector, F targetConnector) {
						DirectAdapterFactoryComponent.this.bind(sourceConnector, targetConnector);
					}

					/**
					 * Unbind with the specified source and target connector parameters.
					 * 
					 * @param sourceConnector
					 *            The source connector (<code>T</code>) parameter.
					 * @param targetConnector
					 *            The target connector (<code>F</code>) parameter.
					 */
					@Override
					public void unbind(T sourceConnector, F targetConnector) {
						DirectAdapterFactoryComponent.this.unbind(sourceConnector, targetConnector);
					}
				});
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	protected void activate(final BundleContext context, final Map<String, Object> properties) {
		initialize(context, properties);
		this.connectorTracker.activateAdapterFactory(getClusterId(), getId());
	}

	/**
	 * Add source connector with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>T</code>)
	 *            parameter.
	 * @see #removeSourceConnector(Connectable)
	 */
	protected void addSourceConnector(final T connector) {
		this.connectorTracker.addSourceConnector(connector, connector.getAdapterFactory(getConnectorType()), this.factory.getTargetConnectorIds(connector));
	}

	/**
	 * Add target connector with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>F</code>)
	 *            parameter.
	 * @see #removeTargetConnector(Connectable)
	 */
	protected void addTargetConnector(final F connector) {
		this.connectorTracker.addTargetConnector(connector);
	}

	/**
	 * Bind with the specified source and target connector parameters.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 */
	private void bind(T sourceConnector, F targetConnector) {
		if (sourceConnector.isEnabled() && targetConnector.isEnabled()) {
			logStatus("Starting", sourceConnector, targetConnector);
			try {
				Adaptable adapter = factory.createAdapter(createAdapterConfiguration(sourceConnector), sourceConnector, targetConnector);
				if (addAdapterReference(adapter)) {
					adapter.bind();
					registerAdapter(sourceConnector, adapter);
				}
			} catch (final Exception e) {
				logStatus("Failed to bind", sourceConnector, targetConnector, e);
			}
		} else {
			logStatus("Not starting disabled", sourceConnector, targetConnector);
		}
	}

	/**
	 * Deactivate.
	 */
	protected void deactivate() {
		this.connectorTracker.deactivateAdapterFactory();
	}

	/**
	 * Remove source connector with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>T</code>)
	 *            parameter.
	 * @see #addSourceConnector(Connectable)
	 */
	protected void removeSourceConnector(final T connector) {
		this.connectorTracker.removeSourceConnector(connector, connector.getAdapterFactory(getConnectorType()), this.factory.getTargetConnectorIds(connector));
	}

	/**
	 * Remove target connector with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>F</code>)
	 *            parameter.
	 * @see #addTargetConnector(Connectable)
	 */
	protected void removeTargetConnector(final F connector) {
		this.connectorTracker.removeTargetConnector(connector);
	}

	/**
	 * Unbind with the specified source and target connector parameters.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 */
	private void unbind(T sourceConnector, F targetConnector) {
		logStatus("Stopping", sourceConnector, targetConnector);
		Adaptable adapter = getAdapter(sourceConnector);
		if (adapter != null) {
			if (removeAdapterReference(adapter)) {
				unregisterAdapter(sourceConnector);
				adapter.unbind();
			}
		}
	}

	/**
	 * Get message describing the status and identifying the adapter.
	 * @param message New status
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 * @return Message to be logged
	 */
	private String getMessage(String message, T sourceConnector, F targetConnector) {
		return message + ' ' + factory.getAdapterName() + " from " + sourceConnector.getName() + ' ' + sourceConnector.getClusterId() + '.' + sourceConnector.getId() + " as " + sourceConnector.getConnectorId() + " to " + targetConnector.getConnectorId();
	}

	/**
	 * Log message describing the exception status and identifying the adapter.
	 * @param message New status
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 * @param e The exception
	 */
	private void logStatus(String message, T sourceConnector, F targetConnector, Exception e) {
		logInfo(getMessage(message, sourceConnector, targetConnector), e);
	}

	/**
	 * Log message describing the status and identifying the adapter.
	 * @param message New status
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnector
	 *            The target connector (<code>F</code>) parameter.
	 */
	private void logStatus(String message, T sourceConnector, F targetConnector) {
		logInfo(getMessage(message, sourceConnector, targetConnector));
	}

}
