package net.powermatcher.core.direct.protocol.adapter;


import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.adapter.service.ConnectorLocaterService;
import net.powermatcher.core.adapter.service.DirectAdapterFactoryService;
import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.log.LogListenable;
import net.powermatcher.core.agent.framework.log.Logable;
import net.powermatcher.core.agent.framework.log.LogPublishable;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * Adapter factory for DirectLoggingAdapter.
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogPublishable
 * @see DirectLoggingAdapter
 * @see Logable
 */
public class DirectLoggingAdapterFactory implements DirectAdapterFactoryService<LogPublishable, LogListenable> {

	public DirectLoggingAdapterFactory() {
	}

	@Override
	public DirectLoggingAdapter createAdapter(Configurable configuration, LogPublishable connector,
			ConnectorLocaterService connectorLocater, int adapterIndex) {
		String loggingAgentId = getTargetConnectorIds(connector)[adapterIndex];
		ConnectorReference<LogListenable> logListenerRef = new ConnectorReference<LogListenable>(
				connectorLocater, connector.getConnectorId(), LogListenable.class, connector.getClusterId(), loggingAgentId);
		DirectLoggingAdapter loggingAdapter = new DirectLoggingAdapter(configuration);
		loggingAdapter.setLogPublisher(connector);
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
	public DirectLoggingAdapter createAdapter(final Configurable configuration,
			final LogPublishable loggingConnector, final LogListenable logListenerConnector) {
		DirectLoggingAdapter loggingAdapter = new DirectLoggingAdapter(configuration);
		loggingAdapter.setLogPublisher(loggingConnector);
		loggingAdapter.setLogListener(logListenerConnector);
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
		return DirectLoggingAdapter.class.getSimpleName();
	}
	
}
