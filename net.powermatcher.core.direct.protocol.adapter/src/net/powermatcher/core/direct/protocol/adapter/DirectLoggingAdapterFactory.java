package net.powermatcher.core.direct.protocol.adapter;


import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.log.LogListenerConnectorService;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * Adapter factory for DirectLoggingAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see LoggingConnectorService
 * @see DirectLoggingAdapter
 * @see LogListenerService
 */
public class DirectLoggingAdapterFactory implements DirectAdapterFactoryService<LoggingConnectorService, LogListenerConnectorService> {

	public DirectLoggingAdapterFactory() {
	}

	@Override
	public DirectLoggingAdapter createAdapter(ConfigurationService configuration, LoggingConnectorService connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String loggingAgentId = getTargetConnectorIds(connector)[adapterIndex];
		ConnectorReference<LogListenerConnectorService> logListenerRef = new ConnectorReference<LogListenerConnectorService>(
				connectorLocater, connector.getConnectorId(), LogListenerConnectorService.class, connector.getClusterId(), loggingAgentId);
		DirectLoggingAdapter loggingAdapter = new DirectLoggingAdapter(configuration);
		loggingAdapter.setLoggingConnector(connector);
		loggingAdapter.setLogListenerRef(logListenerRef);
		return loggingAdapter;
	}

	/**
	 * Create adapter with the specified configuration and agent connector
	 * parameters and return the DirectLoggingAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param loggingConnector
	 *            The logging connector (<code>LoggingConnectorService</code>)
	 *            parameter.
	 * @param logListenerConnector
	 *            The logging connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 * @return Results of the create adapter (<code>DirectLoggingAdapter</code>)
	 *         value.
	 */
	@Override
	public DirectLoggingAdapter createAdapter(final ConfigurationService configuration,
			final LoggingConnectorService loggingConnector, final LogListenerConnectorService logListenerConnector) {
		DirectLoggingAdapter loggingAdapter = new DirectLoggingAdapter(configuration);
		loggingAdapter.setLoggingConnector(loggingConnector);
		loggingAdapter.setLogListenerConnector(logListenerConnector);
		return loggingAdapter;
	}

	/**
	 * Get the logging agent ids configured for the logger.
	 * @param connector The logger to get the logging agent id from
	 * @return The logging agent id configured for the logger.
	 */
	@Override
	public String[] getTargetConnectorIds(final LoggingConnectorService connector) {
		return connector.getConfiguration().getProperty(AgentConfiguration.LOG_LISTENER_ID_PROPERTY, AgentConfiguration.LOG_LISTENER_ID_DEFAULT );
	}

	@Override
	public String getAdapterName() {
		return DirectLoggingAdapter.class.getSimpleName();
	}
	
}
