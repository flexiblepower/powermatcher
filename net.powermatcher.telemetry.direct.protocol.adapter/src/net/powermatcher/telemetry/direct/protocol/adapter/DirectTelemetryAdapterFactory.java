package net.powermatcher.telemetry.direct.protocol.adapter;


import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.telemetry.config.TelemetryConfiguration;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * Adapter factory for DirectTelemetryAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryConnectorService
 * @see TelemetryListenerConnectorService
 * @see DirectTelemetryAdapter
 * @see TelemetryService
 */
public class DirectTelemetryAdapterFactory implements DirectAdapterFactoryService<TelemetryConnectorService, TelemetryListenerConnectorService> {

	public DirectTelemetryAdapterFactory() {
	}

	@Override
	public DirectTelemetryAdapter createAdapter(Configurable configuration, TelemetryConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String telemetryAgentId = getTargetConnectorIds(connector)[adapterIndex];
		ConnectorReference<TelemetryListenerConnectorService> telemetryListenerRef = new ConnectorReference<TelemetryListenerConnectorService>(
				connectorLocater, connector.getConnectorId(), TelemetryListenerConnectorService.class, connector.getClusterId(), telemetryAgentId);
		DirectTelemetryAdapter telemetryAdapter = new DirectTelemetryAdapter(configuration);
		telemetryAdapter.setTelemetryConnector(connector);
		telemetryAdapter.setTelemetryListenerRef(telemetryListenerRef);
		return telemetryAdapter;
	}

	/**
	 * Create adapter with the specified configuration and agent connector
	 * parameters and return the DirectTelemetryAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param telemetryConnector
	 *            The telemetry connector (<code>TelemetryConnectorService</code>)
	 *            parameter.
	 * @param telemetryListenerConnector
	 *            The telemetry listener connector (<code>TelemetryListenerConnectorService</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>DirectTelemetryAdapter</code>)
	 *         value.
	 */
	@Override
	public DirectTelemetryAdapter createAdapter(final Configurable configuration,
			final TelemetryConnectorService telemetryConnector, final TelemetryListenerConnectorService telemetryListenerConnector) {
		DirectTelemetryAdapter telemetryAdapter = new DirectTelemetryAdapter(configuration);
		telemetryAdapter.setTelemetryConnector(telemetryConnector);
		telemetryAdapter.setTelemetryListenerConnector(telemetryListenerConnector);
		return telemetryAdapter;
	}

	/**
	 * Get the telemetry agent ids configured for the agent.
	 * @param connector The agent to get the telemetry agent id from
	 * @return The telemetry agent id configured for the logger.
	 */
	@Override
	public String[] getTargetConnectorIds(final TelemetryConnectorService connector) {
		return connector.getConfiguration().getProperty(TelemetryConfiguration.TELEMETRY_LISTENER_ID_PROPERTY, TelemetryConfiguration.TELEMETRY_LISTENER_ID_DEFAULT );
	}

	@Override
	public String getAdapterName() {
		return DirectTelemetryAdapter.class.getSimpleName();
	}
	
}
