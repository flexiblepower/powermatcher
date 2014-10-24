package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.TargetAdapterFactoryService;
import net.powermatcher.core.agent.framework.log.LogListenable;
import net.powermatcher.core.agent.framework.log.Logable;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * Adapter factory for LogListenerAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogListenable
 * @see LogListenerAdapter
 * @see Logable
 */
public class LogListenerAdapterFactory implements TargetAdapterFactoryService<LogListenable> {

	public LogListenerAdapterFactory() {
	}

	@Override
	public LogListenerAdapter createAdapter(Configurable configuration, LogListenable connector,
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
	public LogListenerAdapter createAdapter(final Configurable configuration,
			final LogListenable logListenerConnector) {
		LogListenerAdapter loggingAdapter = new LogListenerAdapter(configuration);
		loggingAdapter.setLogListenerConnector(logListenerConnector);
		return loggingAdapter;
	}

	@Override
	public String getAdapterName() {
		return LogListenerAdapter.class.getSimpleName();
	}
	
}
