package net.powermatcher.core.adapter;


import net.powermatcher.core.adapter.service.ConnectorKeyService;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.object.ConnectorKey;

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
 * @see Connectable
 * 
 * @param <T>
 *            Defines the generic ConnectorService type that ConnectorTracker
 *            will use.
 */
public abstract class AbstractConnectorFactoryTracker<T extends Connectable> {
	/**
	 * The ConnectorAndFactoryReference is a wrapper for a connector and its
	 * associated list factory references for the specific type T of connector.
	 * Optionally, for each factory reference a target connector ID specifies the target connector to connect to.
	 * Two wrapper instances are equal if they are for the same connector so that the
	 * wrappers can be added and removed from the connector set.
	 * 
	 * @param <T>
	 */
	protected static class ConnectorAndFactoryReference<T extends Connectable> {
		/**
		 * The connector being wrapped.
		 */
		private T connector;
		/**
		 * The list of adapter factory IDs that the connector is specifying for type T.
		 */
		private String[] factoryIds;
		/**
		 * The optional list of ids of the targets that should be connected to.
		 */
		private String[] targetConnectorIds;

		/**
		 * Construct a wrapper instance for a connector and its associated
		 * factory key.
		 * 
		 * @param connector
		 *            The connector (<code>T</code>) parameter.
		 * @param factoryIds
		 *            The list of adapter factory IDs (<code>String[]</code>) parameter.
		 */
		public ConnectorAndFactoryReference(final T connector, final String[] factoryIds) {
			this.connector = connector;
			this.factoryIds = factoryIds;
		}

		/**
		 * Construct a wrapper instance for a connector and its associated
		 * factory key.
		 * 
		 * @param connector
		 *            The connector (<code>T</code>) parameter.
		 * @param factoryIds
		 *            The list of adapter factory IDs (<code>String[]</code>) parameter.
		 * @param targetConnectorIds
		 *            The list of target connector IDs (<code>String[]</code>) parameter.
		 */
		public ConnectorAndFactoryReference(final T connector, final String[] factoryIds, final String[] targetConnectorIds) {
			this.connector = connector;
			this.factoryIds = factoryIds;
			this.targetConnectorIds = targetConnectorIds;
		}
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object o) {
			if (o == this) {
				return true;
			}
			if (o instanceof ConnectorAndFactoryReference) {
				@SuppressWarnings("unchecked")
				ConnectorAndFactoryReference<T> connectorAndFactory = (ConnectorAndFactoryReference<T>) o;
				return this.connector == connectorAndFactory.connector;
			}
			return false;
		}

		/**
		 * Get the wrapped connector.
		 * 
		 * @return The wrapped connector.
		 */
		public T getConnector() {
			return this.connector;
		}

		/**
		 * Get the list of adapter factory IDs that the connector is specifying for type T.
		 * 
		 * @return The list of adapter factory IDs.
		 */
		public String[] getFactoryIds() {
			return this.factoryIds;
		}

		/**
		 * Get the optional list of target connector ids of the wrapped connector.
		 * 
		 * @return The optional list of target connector ids of the wrapped connector.
		 */
		public String[] getTargetConnectorIds() {
			return this.targetConnectorIds;
		}

		/**
		 * Get the optional target connector id of the wrapped connector for a given index in the list.
		 * 
		 * @return The optional target connector id of the wrapped connector for the given index.
		 */
		public String getTargetConnectorId(int index) {
			return this.targetConnectorIds == null ? null : this.targetConnectorIds[index]; 
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.connector.hashCode();
		}

	}

	/**
	 * Get factory key with the specified cluster ID and adapter factory ID
	 * parameters and return the String result.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param factoryId
	 *            The adapter factory ID (<code>String</code>) parameter.
	 * @return Results of the get factory key (<code>ConnectorKeyService</code>)
	 *         value.
	 */
	protected static ConnectorKeyService getFactoryKey(final String clusterId, final String factoryId) {
		return new ConnectorKey(clusterId, factoryId);
	}

	/**
	 * Define the tracking factoryKey (ConnectorKeyService) field.
	 */
	protected ConnectorKeyService factoryKey;

	/**
	 * Constructs an instance of this class from the specified tracker listener
	 * parameter.
	 *
	 */
	public AbstractConnectorFactoryTracker() {
	}

}
