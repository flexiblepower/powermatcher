package net.powermatcher.core.object;


import net.powermatcher.core.adapter.service.ConnectorKeyService;
import net.powermatcher.core.adapter.service.ConnectorService;

/**
 * 
 * @author IBM
 * @version 0.9.0
 */
public class ConnectorKey implements ConnectorKeyService {

	private Class<? extends ConnectorService> connectorType;
	private String clusterId;
	private String connectorId;

	/**
	 * Construct a new key for a specific connector.
	 * @param connectorType The connector type (an interface extending
	 *         <code>ConnectorService</code>).
	 * @param connector The connector <code>ConnectorService</code>).
	 */
	public ConnectorKey(Class<? extends ConnectorService> connectorType, ConnectorService connector) {
		this(connectorType, connector.getClusterId(), connector.getConnectorId());
	}

	/**
	 * Construct a new key for a given connector type.
	 * @param connectorType The connector type (an interface extending
	 *         <code>ConnectorService</code>).
	 * @param clusterId The cluster id of the connector.
	 * @param connectorId The connector id of the connector.
	 */
	public ConnectorKey(Class<? extends ConnectorService> connectorType, String clusterId, String connectorId) {
		if (!connectorType.isInterface()) {
			throw new IllegalArgumentException("Connector types must be identified by their interface, not a class");
		}
		this.connectorType = connectorType;
		this.clusterId = clusterId;
		this.connectorId = connectorId;
	}

	/**
	 * Construct a new key for an anonymous connector type.
	 * @param clusterId The cluster id of the connector.
	 * @param connectorId The connector id of the connector.
	 */
	public ConnectorKey(String clusterId, String connectorId) {
		this.clusterId = clusterId;
		this.connectorId = connectorId;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other instanceof ConnectorKey) {
			ConnectorKey otherKey = (ConnectorKey)other;
			return this.connectorType == otherKey.connectorType && 
				this.clusterId.equals(otherKey.clusterId) &&
				this.connectorId.equals(otherKey.connectorId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = this.clusterId.hashCode() ^ this.connectorId.hashCode();;
		if (this.connectorType != null) {
			hashCode ^= this.connectorType.hashCode(); 
		}
		return hashCode;
	}

}
