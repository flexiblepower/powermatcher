package net.powermatcher.telemetry.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.SourceAdapterFactoryService;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.telemetry.config.TelemetryConfiguration;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * Adapter factory for TelemetryAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryConnectorService
 * @see TelemetryAdapter
 * @see TelemetryService
 */
public class TelemetryAdapterFactory implements SourceAdapterFactoryService<TelemetryConnectorService> {

	public TelemetryAdapterFactory() {
	}

	@Override
	public TelemetryAdapter createAdapter(Configurable configuration, TelemetryConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String telemetryAgentId = getTargetConnectorIds(connector)[adapterIndex];
		return createAdapter(configuration, connector, telemetryAgentId);
	}

	/**
	 * Create telemetry adapter agent with the specified telemetry connector and
	 * configuration parameters and return the TelemetryAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param telemetryConnector
	 *            The telemetry connector (
	 *            <code>TelemetryConnectorService</code>) parameter.
	 * @return Results of the create adapter (
	 *         <code>TelemetryAdapter</code>) value.
	 */
	@Override
	public TelemetryAdapter createAdapter(final Configurable configuration,
			final TelemetryConnectorService telemetryConnector, final String telemetryAgentId) {
		TelemetryAdapter telemetryAdapter = new TelemetryAdapter(configuration);
		telemetryAdapter.setTelemetryConnector(telemetryConnector);
		telemetryAdapter.setTelemetryAgentId(telemetryAgentId);
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
		return TelemetryAdapter.class.getSimpleName();
	}
	
}