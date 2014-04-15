package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.framework.log.LogListenerConnectorService;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * Adapter factory for LogListenerAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogListenerConnectorService
 * @see LogListenerAdapter
 * @see LogListenerService
 */
public class LogListenerAdapterFactory implements TargetAdapterFactoryService<LogListenerConnectorService> {

	public LogListenerAdapterFactory() {
	}

	@Override
	public LogListenerAdapter createAdapter(ConfigurationService configuration, LogListenerConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		return createAdapter(configuration, connector);
	}

	/**
	 * Create adapter with the specified configuration and log listener
	 * connector parameters and return the LoggingProtocolAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param logListenerConnector
	 *            The log listener connector (
	 *            <code>LogListenerConnectorService</code>) parameter.
	 * @return Results of the create adapter (<code>LogListenerAdapter</code>)
	 *         value.
	 */
	@Override
	public LogListenerAdapter createAdapter(final ConfigurationService configuration,
			final LogListenerConnectorService logListenerConnector) {
		LogListenerAdapter loggingAdapter = new LogListenerAdapter(configuration);
		loggingAdapter.setLogListenerConnector(logListenerConnector);
		return loggingAdapter;
	}

	@Override
	public String getAdapterName() {
		return LogListenerAdapter.class.getSimpleName();
	}
	
}
