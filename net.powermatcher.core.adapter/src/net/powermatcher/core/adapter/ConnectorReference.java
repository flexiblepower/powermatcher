package net.powermatcher.core.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.ConnectorService;

/**
 * 
 * @author IBM
 * @version 0.9.0
 * 
 */
public class ConnectorReference<T extends ConnectorService> {

	private ConnectorLocaterService connectorLocator;
	private String sourceConnectorId;
	private Class<T> targetConnectorType;
	private String clusterId;
	private String targetConnectorId;

	public ConnectorReference(ConnectorLocaterService connectorLocator, String sourceConnectorId, Class<T> targetConnectorType, String clusterId,
			String targetConnectorId) {
		super();
		this.connectorLocator = connectorLocator;
		this.sourceConnectorId = sourceConnectorId;
		this.targetConnectorType = targetConnectorType;
		this.clusterId = clusterId;
		this.targetConnectorId = targetConnectorId;
	}

	public T getConnector() throws Exception {
		T connector = this.connectorLocator.locateConnector(targetConnectorType, clusterId, targetConnectorId);		
		if (connector == null)
			throw new Exception("Failed to locate connector of type " + targetConnectorType.getSimpleName() + " for source="  + clusterId + '.' + sourceConnectorId + " and target=" + clusterId + '.' + targetConnectorId);
		return connector;
	}
	
}
