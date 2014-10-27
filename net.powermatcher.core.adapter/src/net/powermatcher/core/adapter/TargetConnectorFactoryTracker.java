package net.powermatcher.core.adapter;


import java.util.HashSet;
import java.util.Set;

import net.powermatcher.core.adapter.service.Connectable;

/**
 * A TargetConnectorFactoryTracker is used to bind an adapter created by an adapter
 * factory to each component that registers a specific ConnectorService type T
 * with a corresponding factory id.
 * 
 * In case a connector is added using the addConnector(...) method, it will be
 * added to a connector collection. If the factoryKey of the connector factory
 * and the connector tracker's factoryKey match the connector tracker listener
 * (ConnectorTrackerListener) is invoked to create and bind the adapter to the
 * connector as soon as the adapter factory is activated.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TargetConnectorTrackerListener
 * @see Connectable
 * 
 * @param <T>
 *            Defines the generic ConnectorService type that ConnectorTracker
 *            will use.
 */
public class TargetConnectorFactoryTracker<T extends Connectable> extends AbstractConnectorFactoryTracker<T> {
	/**
	 * Define the connectors (Set<T>) field.
	 */
	protected Set<ConnectorAndFactoryReference<T>> connectors = new HashSet<ConnectorAndFactoryReference<T>>();

	/**
	 * Define the tracker listener (TargetConnectorTrackerListener<T>) field.
	 */
	private TargetConnectorTrackerListener<T> trackerListener;

	/**
	 * Constructs an instance of this class from the specified tracker listener
	 * parameter.
	 * 
	 * @param trackerListener
	 *            The tracker listener (<code>ConnectorTrackerListener<T></code>
	 *            ) parameter.
	 */
	public TargetConnectorFactoryTracker(final TargetConnectorTrackerListener<T> trackerListener) {
		this.trackerListener = trackerListener;
	}

	/**
	 * Activate adapter factory with the specified factory ID parameter.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param factoryId
	 *            The adapter factory ID (<code>String</code>) parameter.
	 * @see #deactivateAdapterFactory()
	 */
	public synchronized void activateAdapterFactory(final String clusterId, final String factoryId) {
		this.factoryKey = getFactoryKey(clusterId, factoryId);
		for (ConnectorAndFactoryReference<T> connectorAndFactory : this.connectors) {
			String[] factoryIds = connectorAndFactory.getFactoryIds();
			T connector = connectorAndFactory.getConnector();
			for (int i = 0; i < factoryIds.length; i++) {
				if (getFactoryKey(connector.getClusterId(), factoryIds[i]).equals(this.factoryKey)) {
					this.trackerListener.bind(connector);
				}
			}
		}
	}

	/**
	 * Add connector connector with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>T</code>) parameter.
	 * @param factoryIds
	 *            The list of adapter factory IDs (<code>String[]</code>) parameter.
	 * @see #removeConnector(Connectable, String[])
	 */
	public synchronized void addConnector(final T connector, final String[] factoryIds) {
		ConnectorAndFactoryReference<T> connectorAndFactory = new ConnectorAndFactoryReference<T>(connector, factoryIds);
		this.connectors.add(connectorAndFactory);
		if (this.factoryKey != null) {
			String clusterId = connector.getClusterId();
			for (int i = 0; i < factoryIds.length; i++) {
				if (getFactoryKey(clusterId, factoryIds[i]).equals(this.factoryKey)) {
					this.trackerListener.bind(connector);
				}
			}
		}
	}

	/**
	 * Deactivate adapter factory.
	 * 
	 * @see #activateAdapterFactory(String,String)
	 */
	public synchronized void deactivateAdapterFactory() {
		for (ConnectorAndFactoryReference<T> connectorAndFactory : this.connectors) {
			String[] factoryIds = connectorAndFactory.getFactoryIds();
			for (int i = 0; i < factoryIds.length; i++) {
				T connector = connectorAndFactory.getConnector();
				if (getFactoryKey(connector.getClusterId(), factoryIds[i]).equals(this.factoryKey)) {
					this.trackerListener.unbind(connector);
				}
			}
		}
		this.factoryKey = null;
	}

	/**
	 * Remove connector connector with the specified connector parameter.
	 * 
	 * @param connector
	 *            The connector (<code>T</code>) parameter.
	 * @param factoryIds
	 *            The list of adapter factory IDs (<code>String[]</code>) parameter.
	 * @see #addConnector(Connectable, String[])
	 */
	public synchronized void removeConnector(final T connector, final String[] factoryIds) {
		ConnectorAndFactoryReference<T> connectorAndFactory = new ConnectorAndFactoryReference<T>(connector, factoryIds);
		if (this.factoryKey != null) {
			String clusterId = connector.getClusterId();
			for (int i = 0; i < factoryIds.length; i++) {
				if (getFactoryKey(clusterId, factoryIds[i]).equals(this.factoryKey)) {
					this.trackerListener.unbind(connector);
				}
			}
		}
		this.connectors.remove(connectorAndFactory);
	}

}
