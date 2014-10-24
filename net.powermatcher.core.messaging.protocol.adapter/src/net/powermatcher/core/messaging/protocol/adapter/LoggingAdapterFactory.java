package net.powermatcher.core.messaging.protocol.adapter;


import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.SourceAdapterFactoryService;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.log.Logable;
import net.powermatcher.core.agent.framework.log.LogPublishable;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * Adapter factory for LoggingAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogPublishable
 * @see LoggingAdapter
 * @see Logable
 */
public class LoggingAdapterFactory implements SourceAdapterFactoryService<LogPublishable> {

	public LoggingAdapterFactory() {
	}

	@Override
	public LoggingAdapter createAdapter(Configurable configuration, LogPublishable connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String loggingAgentId = getTargetConnectorIds(connector)[adapterIndex];
		return createAdapter(configuration, connector, loggingAgentId);
	}

	/**
	 * Create adapter with the specified configuration and log listener
	 * connector parameters and return the LoggingProtocolAdapter result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param loggingConnector
	 *            The log listener connector (
	 *            <code>LoggingConnectorService</code>) parameter.
	 * @return Results of the create adapter (<code>LogListenerAdapter</code>)
	 *         value.
	 */
	@Override
	public LoggingAdapter createAdapter(final Configurable configuration,
			final LogPublishable loggingConnector, final String loggingAgentId) {
		LoggingAdapter loggingAdapter = new LoggingAdapter(configuration);
		loggingAdapter.setLoggingConnector(loggingConnector);
		loggingAdapter.setLoggingAgentId(loggingAgentId);
		return loggingAdapter;
	}

	/**
	 * Get the logging agent ids configured for the logger.
	 * @param connector The logger to get the logging agent id from
	 * @return The logging agent id configured for the logger.
	 */
	@Override
	public String[] getTargetConnectorIds(final LogPublishable connector) {
		return connector.getConfiguration().getProperty(AgentConfiguration.LOG_LISTENER_ID_PROPERTY, AgentConfiguration.LOG_LISTENER_ID_DEFAULT );
	}

	@Override
	public String getAdapterName() {
		return LoggingAdapter.class.getSimpleName();
	}
	
}
