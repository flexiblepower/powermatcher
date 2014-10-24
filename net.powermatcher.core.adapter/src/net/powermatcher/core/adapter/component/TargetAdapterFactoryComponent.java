package net.powermatcher.core.adapter.component;


import java.util.Map;

import net.powermatcher.core.adapter.TargetConnectorFactoryTracker;
import net.powermatcher.core.adapter.TargetConnectorTrackerListener;
import net.powermatcher.core.adapter.service.Adaptable;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;

import org.osgi.framework.BundleContext;


/**
 * The TargetAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a TargetProtocolAdapter. A ConnectorTracker will bind the adapter component to a component 
 * that implements the T interface if that component instance has the
 * same id and cluster id.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TargetAdapterFactoryService
 * @see TargetConnectorFactoryTracker
 * @see TargetConnectorTrackerListener
 * @see Adaptable
 */
public abstract class TargetAdapterFactoryComponent<T extends Connectable> extends AdapterFactoryComponent<T> {

	/**
	 * Define the adapter factory (TargetAdapterFactoryService) field.
	 */
	private TargetAdapterFactoryService<T> factory; 

	/**
	 * Define the connector tracker (TargetConnectorFactoryTracker<T>)
	 * field.
	 */
	private TargetConnectorFactoryTracker<T> connectorTracker;

	/**
	 * Constructs an instance of this class.
	 */
	public TargetAdapterFactoryComponent(TargetAdapterFactoryService<T> factory) {
		this.factory = factory;
		this.connectorTracker = new TargetConnectorFactoryTracker<T>(
				new TargetConnectorTrackerListener<T>() {
					/**
					 * Bind with the specified target connector parameter.
					 * 
					 * @param targetConnector
					 *            The target connector (
					 *            <code>T</code>)
					 *            parameter.
					 * @see #bind(T)
					 */
					@Override
					public void bind(final T targetConnector) {
						TargetAdapterFactoryComponent.this.bind(targetConnector);
					}

					/**
					 * Unbind with the specified target connector parameter.
					 * 
					 * @param targetConnector
					 *            The target connector (
					 *            <code>T</code>)
					 *            parameter.
					 * @see #unbind(T)
					 */
					@Override
					public void unbind(final T targetConnector) {
						TargetAdapterFactoryComponent.this.unbind(targetConnector);
					}
				});
	}

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	protected void activate(BundleContext context, final Map<String, Object> properties) {
		initialize(context, properties);
		this.connectorTracker.activateAdapterFactory(getClusterId(), getId());
	}

	/**
	 * Add target connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>T</code>)
	 *            parameter.
	 * @see #removeTargetConnector(Connectable)
	 */
	protected void addTargetConnector(final T targetConnector) {
		this.connectorTracker.addConnector(targetConnector, targetConnector.getAdapterFactory(getConnectorType()));
	}

	/**
	 * Bind with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>T</code>)
	 *            parameter.
	 */
	private void bind(final T targetConnector) {
		if (targetConnector.isEnabled()) {
			logStatus("Starting", targetConnector);
			try {
				Adaptable adapter = factory.createAdapter(createAdapterConfiguration(targetConnector), targetConnector);
				if (addAdapterReference(adapter)) {
					adapter.bind();
					registerAdapter(targetConnector, adapter);
				}
			} catch (final Exception e) {
				logStatus("Failed to bind", targetConnector, e);
			}
		} else {
			logStatus("Not starting disabled", targetConnector);
		}
	}

	/**
	 * Deactivate.
	 */
	protected void deactivate() {
		this.connectorTracker.deactivateAdapterFactory();
	}

	/**
	 * Remove target connector with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>T</code>)
	 *            parameter.
	 * @see #addTargetConnector(Connectable)
	 */
	protected void removeTargetConnector(final T targetConnector) {
		this.connectorTracker.removeConnector(targetConnector, targetConnector.getAdapterFactory(getConnectorType()));
	}

	/**
	 * Unbind with the specified target connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>T</code>)
	 *            parameter.
	 */
	private void unbind(final T targetConnector) {
		logStatus("Stopping", targetConnector);
		Adaptable adapter = getAdapter(targetConnector);
		if (adapter != null) {
			if (removeAdapterReference(adapter)) {
				unregisterAdapter(targetConnector);
				adapter.unbind();
			}
		}
	}

	/**
	 * Get message describing the status and identifying the adapter.
	 * @param message New status
	 * @param targetConnector
	 *            The target connector (<code>T</code>) parameter.
	 * @return Message to be logged
	 */
	private String getMessage(String message, T targetConnector) {
		return message + ' ' + factory.getAdapterName() + " for " + targetConnector.getName() + ' ' + targetConnector.getClusterId() + '.' + targetConnector.getId() + " as " + targetConnector.getConnectorId();
	}

	/**
	 * Log message describing the exception status and identifying the adapter.
	 * @param message New status
	 * @param targetConnector
	 *            The target connector (<code>T</code>) parameter.
	 * @param e The exception
	 */
	private void logStatus(String message, T targetConnector, Exception e) {
		logInfo(getMessage(message, targetConnector), e);
	}

	/**
	 * Log message describing the status and identifying the adapter.
	 * @param message New status
	 * @param targetConnector
	 *            The target connector (<code>T</code>) parameter.
	 */
	private void logStatus(String message, T targetConnector) {
		logInfo(getMessage(message, targetConnector));
	}

	protected TargetAdapterFactoryService<T> getFactory() {
		return this.factory;
	}

	protected void setFactory(TargetAdapterFactoryService<T> factory) {
		this.factory = factory;
	}

}
