package net.powermatcher.core.launcher.main;


import java.util.HashMap;
import java.util.Map;

import net.powermatcher.core.adapter.service.ConnectorKeyService;
import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.object.ConnectorKey;

/**
 * @author IBM
 * @version 0.9.0
 */
public class ConnectorRegistry implements ConnectorLocaterService {
	
	private Map<ConnectorKeyService, Connectable> connectorRegistry = new HashMap<ConnectorKeyService, Connectable>();

	public ConnectorRegistry() {
		super();
	}

	public void add(Connectable connector) {
		Class<Connectable> connectorTypes[] = connector.getConnectorTypes();
		for (int i = 0; i < connectorTypes.length; i++) {
			connectorRegistry.put(connector.getConnectorKey(connectorTypes[i]), connector);
		}
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Connectable> T locateConnector(Class<T> connectorType, String clusterId, String connectorId) throws Exception {
		Connectable connector = connectorRegistry.get(new ConnectorKey(connectorType, clusterId, connectorId));
		return (T)connector;
	}

}
