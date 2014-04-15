package net.powermatcher.core.adapter;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.powermatcher.core.adapter.service.ConnectorKeyService;
import net.powermatcher.core.adapter.service.ConnectorService;
import net.powermatcher.core.object.ConnectorKey;


/**
 * A TargetConnectorFactoryTracker is used to bind an adapter created by an adapter factory to each
 * component that registers a specific ConnectorService type T with a corresponding factory id.
 * 
 * In case a connector is added using the addConnector(...) method, it will be added 
 * to a connector collection. If the factoryKey of the connector factory and the connector tracker's factoryKey match
 * the connector tracker listener (DirectConnectorTrackerListener) is invoked to create and bind the
 * adapter to the connector as soon as the adapter factory is activated.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectConnectorTrackerListener
 * @see ConnectorService
 * 
 * @param <T> Defines the generic ConnectorService type that ConnectorTracker will use.
 */
public class DirectConnectorFactoryTracker<T extends ConnectorService, F extends ConnectorService> extends AbstractConnectorFactoryTracker<T> {

	/**
	 * Get key with the specified cluster ID and connector ID parameters and
	 * return the String result.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param connectorId
	 *            The connector ID (<code>String</code>) parameter.
	 * @return Results of the get key (<code>ConnectorKeyService</code>) value.
	 */
	private static ConnectorKeyService getKey(final String clusterId, final String connectorId) {
		return new ConnectorKey(clusterId, connectorId);
	}

	/**
	 * Define the connectors (Set<T>) field.
	 */
	protected Set<ConnectorAndFactoryReference<T>> connectors = new HashSet<ConnectorAndFactoryReference<T>>();

	/**
	 * Define the tracker listener (DirectConnectorTrackerListener<T>) field.
	 */
	private DirectConnectorTrackerListener<T, F> trackerListener;
	/**
	 * Define the set of connectors by target connector id (Map<ConnectorKeyService, F>) field.
	 */
	private Map<ConnectorKeyService, Set<ConnectorAndFactoryReference<T>>> connectorsByTargetConnectorId = new HashMap<ConnectorKeyService, Set<ConnectorAndFactoryReference<T>>>();
	/**
	 * Define the target connectors (Map<ConnectorKeyService, F>) field.
	 */
	private Map<ConnectorKeyService, F> targetConnectorsByConnectorId = new HashMap<ConnectorKeyService, F>();
	/**
	 * Constructs an instance of this class from the specified tracker listener
	 * parameter.
	 * 
	 * @param trackerListener
	 *            The tracker listener (<code>DirectConnectorTrackerListener<T></code>
	 *            ) parameter.
	 */
	public DirectConnectorFactoryTracker(final DirectConnectorTrackerListener<T, F> trackerListener) {
		this.trackerListener = trackerListener;
	}

	/**
	 * Add connector connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>T</code>) parameter.
	 * @see #removeTargetConnector(ConnectorService)
	 */
	public synchronized void addTargetConnector(final F targetConnector) {
		String targetConnectorId = targetConnector.getConnectorId();
		ConnectorKeyService targetConnectorKey = getKey(targetConnector.getClusterId(), targetConnectorId);
		this.targetConnectorsByConnectorId.put(targetConnectorKey, targetConnector);
		if (this.factoryKey != null) {
			Set<ConnectorAndFactoryReference<T>> connectorSet = this.connectorsByTargetConnectorId.get(targetConnectorKey);
			if (connectorSet != null) {
				for (ConnectorAndFactoryReference<T> connectorAndFactory : connectorSet) {
					String[] factoryIds = connectorAndFactory.getFactoryIds();
					String[] targetConnectorIds = connectorAndFactory.getTargetConnectorIds();
					T sourceConnector = connectorAndFactory.getConnector();
					for (int i = 0; i < factoryIds.length; i++) {
						if (getFactoryKey(sourceConnector.getClusterId(), factoryIds[i]).equals(this.factoryKey) &&
							targetConnectorIds[i].equals(targetConnectorId)) {
							this.trackerListener.bind(sourceConnector, targetConnector);
						}
					}
				}
			}
		}
	}

	/**
	 * Remove connector connector with the specified connector parameter.
	 * 
	 * @param targetConnector
	 *            The target connector (<code>T</code>) parameter.
	 * @see #addTargetConnector(ConnectorService)
	 */
	public synchronized void removeTargetConnector(final F targetConnector) {
		String targetConnectorId = targetConnector.getConnectorId();
		ConnectorKeyService targetConnectorKey = getKey(targetConnector.getClusterId(), targetConnectorId);
		if (this.factoryKey != null) {
			Set<ConnectorAndFactoryReference<T>> connectorSet = this.connectorsByTargetConnectorId.get(targetConnectorKey);
			if (connectorSet != null) {
				for (ConnectorAndFactoryReference<T> connectorAndFactory : connectorSet) {
					String[] factoryIds = connectorAndFactory.getFactoryIds();
					String[] targetConnectorIds = connectorAndFactory.getTargetConnectorIds();
					T sourceConnector = connectorAndFactory.getConnector();
					for (int i = 0; i < factoryIds.length; i++) {
						if (getFactoryKey(sourceConnector.getClusterId(), factoryIds[i]).equals(this.factoryKey) &&
								targetConnectorIds[i].equals(targetConnectorId)) {
							this.trackerListener.unbind(sourceConnector, targetConnector);
						}
					}
				}
			}
		}
		this.targetConnectorsByConnectorId.remove(targetConnectorKey);
	}

	/**
	 * Add connector connector with the specified connector parameter.
	 * 
	 * @param sourceConnector
	 *            The connector (<code>T</code>) parameter.
	 * @param factoryIds
	 *            The list of adapter factory IDs (<code>String[]</code>) parameter.
	 * @param targetConnectorIds
	 *            The list of target connector IDs (<code>String[]</code>) parameter.
	 * @see #removeSourceConnector(ConnectorService, String[], String[])
	 */
	public synchronized void addSourceConnector(final T sourceConnector, final String[] factoryIds, final String[] targetConnectorIds) {
		ConnectorAndFactoryReference<T> connectorAndFactory = new ConnectorAndFactoryReference<T>(sourceConnector, factoryIds, targetConnectorIds);
		this.connectors.add(connectorAndFactory);
		for (int i = 0; i < targetConnectorIds.length; i++) {
			String targetConnectorId = targetConnectorIds[i];
			ConnectorKeyService connectorKey = getKey(sourceConnector.getClusterId(), targetConnectorId);
			Set<ConnectorAndFactoryReference<T>> connectorSet = this.connectorsByTargetConnectorId.get(connectorKey);
			if (connectorSet == null) {
				connectorSet = new HashSet<ConnectorAndFactoryReference<T>>(1);
			}
			connectorSet.add(connectorAndFactory);
			this.connectorsByTargetConnectorId.put(connectorKey, connectorSet);
		}
		if (this.factoryKey != null) {
			bindIfPaired(connectorAndFactory);
		}
	}

	private void bindIfPaired(ConnectorAndFactoryReference<T> connectorAndFactory) {
		String[] factoryIds = connectorAndFactory.getFactoryIds();
		T connector = connectorAndFactory.getConnector();
		for (int i = 0; i < factoryIds.length; i++) {
			if (getFactoryKey(connector.getClusterId(), factoryIds[i]).equals(this.factoryKey)) {
				String targetConnectorId = connectorAndFactory.getTargetConnectorIds()[i];
				ConnectorKeyService targetConnectorKey = getKey(connector.getClusterId(), targetConnectorId);
				F targetConnector = this.targetConnectorsByConnectorId.get(targetConnectorKey);
				if (targetConnector != null) {
					this.trackerListener.bind(connector, targetConnector);
				}
			}
		}
	}

	private void unbindIfPaired(ConnectorAndFactoryReference<T> connectorAndFactory) {
		String[] factoryIds = connectorAndFactory.getFactoryIds();
		T connector = connectorAndFactory.getConnector();
		for (int i = 0; i < factoryIds.length; i++) {
			if (getFactoryKey(connector.getClusterId(), factoryIds[i]).equals(this.factoryKey)) {
				String targetConnectorId = connectorAndFactory.getTargetConnectorIds()[i];
				ConnectorKeyService targetConnectorKey = getKey(connector.getClusterId(), targetConnectorId);
				F targetConnector = this.targetConnectorsByConnectorId.get(targetConnectorKey);
				if (targetConnector != null) {
					this.trackerListener.unbind(connector, targetConnector);
				}
			}
		}
	}

	/**
	 * Remove connector connector with the specified connector parameter.
	 * 
	 * @param sourceConnector
	 *            The connector (<code>T</code>) parameter.
	 * @param factoryIds
	 *            The list of adapter factory IDs (<code>String[]</code>) parameter.
	 * @param targetConnectorIds
	 *            The list of target connector IDs (<code>String[]</code>) parameter.
	 * @see #addSourceConnector(ConnectorService, String[], String[])
	 */
	public synchronized void removeSourceConnector(final T sourceConnector, final String[] factoryIds, final String[] targetConnectorIds) {
		ConnectorAndFactoryReference<T> connectorAndFactory = new ConnectorAndFactoryReference<T>(sourceConnector, factoryIds, targetConnectorIds); 
		if (this.factoryKey != null) {
			unbindIfPaired(connectorAndFactory);
		}
		for (int i = 0; i < targetConnectorIds.length; i++) {
			String targetConnectorId = targetConnectorIds[i];
			ConnectorKeyService connectorKey = getKey(sourceConnector.getClusterId(), targetConnectorId);
			Set<ConnectorAndFactoryReference<T>> connectorSet = this.connectorsByTargetConnectorId.get(connectorKey);
			if (connectorSet != null) {
				connectorSet.remove(connectorAndFactory);
			}
		}
		this.connectors.remove(connectorAndFactory);
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
			bindIfPaired(connectorAndFactory);
		}
	}

	/**
	 * Deactivate adapter factory.
	 * 
	 * @see #activateAdapterFactory(String,String)
	 */
	public synchronized void deactivateAdapterFactory() {
		for (ConnectorAndFactoryReference<T> connectorAndFactory : this.connectors) {
			unbindIfPaired(connectorAndFactory);
		}
		this.factoryKey = null;
	}

	
}

