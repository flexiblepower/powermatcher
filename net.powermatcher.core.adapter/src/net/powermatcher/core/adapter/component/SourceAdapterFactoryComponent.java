package net.powermatcher.core.adapter.component;


import java.util.Map;

import net.powermatcher.core.adapter.SourceConnectorFactoryTracker;
import net.powermatcher.core.adapter.SourceConnectorTrackerListener;
import net.powermatcher.core.adapter.service.Adaptable;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.adapter.service.SourceAdapterFactoryService;

import org.osgi.framework.BundleContext;


/**
 * The SourceAdapterFactoryComponent is a wrapper class that creates an OSGi component of
 * a SourceAdapter. A ConnectorTracker will bind the adapter component to a component 
 * that implements the T interface if that component instance has the
 * same id and cluster id.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see SourceConnectorFactoryTracker
 * @see SourceConnectorTrackerListener
 * @see Adaptable
 */
public abstract class SourceAdapterFactoryComponent<T extends Connectable> extends AdapterFactoryComponent<T> {

	/**
	 * Define the adapter factory (SourceAdapterFactoryService) field.
	 */
	private SourceAdapterFactoryService<T> factory; 

	/**
	 * Define the connector tracker (SourceConnectorFactoryTracker<T>)
	 * field.
	 */
	private SourceConnectorFactoryTracker<T> connectorTracker;

	/**
	 * Constructs an instance of this class.
	 */
	public SourceAdapterFactoryComponent(SourceAdapterFactoryService<T> factory) {
		this.factory = factory;
		this.connectorTracker = new SourceConnectorFactoryTracker<T>(
				new SourceConnectorTrackerListener<T>() {
					/**
					 * Bind with the specified source connector and target connector id parameters.
					 * 
					 * @param sourceConnector
					 *            The source connector (<code>T</code>) parameter.
					 * @param targetConnectorId
					 *            The target connector id (<code>String</code>) parameter.
					 */
					@Override
					public void bind(T sourceConnector, String targetConnectorId) {
						SourceAdapterFactoryComponent.this.bind(sourceConnector, targetConnectorId);
					}

					/**
					 * Unbind with the specified source connector and target connector id parameters.
					 * 
					 * @param sourceConnector
					 *            The source connector (<code>T</code>) parameter.
					 * @param targetConnectorId
					 *            The target connector id (<code>String</code>) parameter.
					 */
					@Override
					public void unbind(T sourceConnector, String targetConnectorId) {
						SourceAdapterFactoryComponent.this.unbind(sourceConnector, targetConnectorId);
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
	 * Add source connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @see #removeSourceConnector(Connectable)
	 */
	protected void addSourceConnector(final T sourceConnector) {
		this.connectorTracker.addConnector(sourceConnector, sourceConnector.getAdapterFactory(getConnectorType()), this.factory.getTargetConnectorIds(sourceConnector));
	}

	/**
	 * Bind with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 */
	private void bind(final T sourceConnector, String targetConnectorId) {
		if (sourceConnector.isEnabled()) {
			logStatus("Starting", sourceConnector, targetConnectorId);
			try {
				Adaptable adapter = factory.createAdapter(createAdapterConfiguration(sourceConnector), sourceConnector, targetConnectorId);
				if (addAdapterReference(adapter)) {
					adapter.bind();
					registerAdapter(sourceConnector, adapter);
				}
			} catch (final Exception e) {
				logStatus("Failed to bind", sourceConnector, targetConnectorId, e);
			}
		} else {
			logStatus("Not starting disabled", sourceConnector, targetConnectorId);
		}
	}

	/**
	 * Deactivate.
	 */
	protected void deactivate() {
		this.connectorTracker.deactivateAdapterFactory();
	}

	/**
	 * Remove source connector with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @see #addSourceConnector(Connectable)
	 */
	protected void removeSourceConnector(final T sourceConnector) {
		this.connectorTracker.removeConnector(sourceConnector, sourceConnector.getAdapterFactory(getConnectorType()), this.factory.getTargetConnectorIds(sourceConnector));
	}

	/**
	 * Unbind with the specified source connector parameter.
	 * 
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 */
	private void unbind(final T sourceConnector, String targetConnectorId) {
		logStatus("Stopping", sourceConnector, targetConnectorId);
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
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 * @return Message to be logged
	 */
	private String getMessage(String message, T sourceConnector, String targetConnectorId) {
		return message + ' ' + factory.getAdapterName() + " from " + sourceConnector.getName() + ' ' + sourceConnector.getClusterId() + '.' + sourceConnector.getId() + " as " + sourceConnector.getConnectorId() + " to " + targetConnectorId;
	}

	/**
	 * Log message describing the exception status and identifying the adapter.
	 * @param message New status
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 * @param e The exception
	 */
	private void logStatus(String message, T sourceConnector, String targetConnectorId, Exception e) {
		logInfo(getMessage(message, sourceConnector, targetConnectorId), e);
	}

	/**
	 * Log message describing the status and identifying the adapter.
	 * @param message New status
	 * @param sourceConnector
	 *            The source connector (<code>T</code>) parameter.
	 * @param targetConnectorId
	 *            The target connector id (<code>String</code>) parameter.
	 */
	private void logStatus(String message, T sourceConnector, String targetConnectorId) {
		logInfo(getMessage(message, sourceConnector, targetConnectorId));
	}

}
