package net.powermatcher.core.direct.protocol.adapter;


import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.agent.framework.log.LogListenerConnectorService;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * <p>
 * Adapter class to provide an agent with functionality to log
 * Power Matcher protocol events.
 * </p>
 * <p>
 * This adapter directly connects a PowerMatcher agent or matcher to a log listener directly using
 * the LogListenerService interfaces of the agent or matcher and the log listener connectors.
 * </p>
 * <p>
 * The adapter is created for the loggin connector as primary connector interface. Upon binding,
 * the adapter locates the log listener using the connector locater.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogListenerService
 * @see LoggingConnectorService
 */
public class DirectLoggingAdapter extends Adapter {

	/**
	 * Define the logging connector (LoggingConnectorService) field.
	 */
	private LoggingConnectorService loggingConnector;

	/**
	 * Define the log listener connector (LogListenerConnectorService) field.
	 */
	private LogListenerConnectorService logListenerConnector;

	/**
	 * Define the log listener reference (ConnectorReference) field.
	 */
	private ConnectorReference<LogListenerConnectorService> logListenerRef;

	/**
	 * Constructs an instance of this class.
	 */
	public DirectLoggingAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public DirectLoggingAdapter(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		super.bind();
		if (this.logListenerRef != null) {
			this.logListenerConnector = this.logListenerRef.getConnector();
		}
		this.logListenerConnector.bind();
		this.loggingConnector.bind(this.logListenerConnector.getLogListener());
	}

	/**
	 * Gets the logging connector (LoggingConnectorService) value.
	 * 
	 * @return The logging connector (LoggingConnectorService) value.
	 */
	public LoggingConnectorService getLoggingConnector() {
		return this.loggingConnector;
	}

	/**
	 * Gets the log listener connector (LogListenerConnectorService) value.
	 * 
	 * @return The log listener  connector (LogListenerConnectorService) value.
	 */
	public LogListenerConnectorService getLogListenerConnector() {
		return this.logListenerConnector;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.loggingConnector.isEnabled();
	}

	/**
	 * Sets the logging connector value.
	 * 
	 * @param loggingConnector
	 *            The logging connector (<code>LoggingConnectorService</code>)
	 *            parameter.
	 */
	public void setLoggingConnector(final LoggingConnectorService loggingConnector) {
		this.loggingConnector = loggingConnector;
	}

	/**
	 * Sets the log listener connector value.
	 * 
	 * @param logListenerConnector
	 *            The log listener connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 */
	public void setLogListenerConnector(final LogListenerConnectorService logListenerConnector) {
		this.logListenerConnector = logListenerConnector;
	}

	void setLogListenerRef(ConnectorReference<LogListenerConnectorService> logListenerRef) {
		this.logListenerRef = logListenerRef;
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.loggingConnector.unbind(this.logListenerConnector.getLogListener());
		this.logListenerConnector.unbind();
	}

}
