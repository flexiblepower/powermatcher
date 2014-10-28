package net.powermatcher.telemetry.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.AdapterService;
import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * Adapter factory for TelemetryAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryListenerConnectorService
 * @see TelemetryListenerAdapter
 * @see TelemetryService
 */
public class TelemetryListenerAdapterFactory implements TargetAdapterFactoryService<TelemetryListenerConnectorService> {

	public TelemetryListenerAdapterFactory() {
	}

	@Override
	public AdapterService createAdapter(ConfigurationService configuration, TelemetryListenerConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create telemetry adapter agent with the specified telemetry listener connector and
	 * configuration parameters and return the TelemetryAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param telemetryListenerConnector
	 *            The telemetry listener connector (
	 *            <code>TelemetryListenerConnectorService</code>) parameter.
	 * @return Results of the create adapter (
	 *         <code>TelemetryAdapter</code>) value.
	 */
	@Override
	public TelemetryListenerAdapter createAdapter(final ConfigurationService configuration,
			final TelemetryListenerConnectorService telemetryListenerConnector) {
		TelemetryListenerAdapter telemetryAdapter = new TelemetryListenerAdapter(configuration);
		telemetryAdapter.setTelemetryListenerConnector(telemetryListenerConnector);
		return telemetryAdapter;
	}

	@Override
	public String getAdapterName() {
		return TelemetryListenerAdapter.class.getSimpleName();
	}
	
}
