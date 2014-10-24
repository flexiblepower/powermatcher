package net.powermatcher.core.direct.protocol.adapter;


import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.agent.framework.log.LogListenable;
import net.powermatcher.core.agent.framework.log.Logable;
import net.powermatcher.core.agent.framework.log.LogPublishable;
import net.powermatcher.core.configurable.service.Configurable;


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
 * @see Logable
 * @see LogPublishable
 */
public class DirectLoggingAdapter extends Adapter {

	/**
	 * Define the logging connector (LoggingConnectorService) field.
	 */
	private LogPublishable logPublisher;

	/**
	 * Define the log listener connector (LogListenerConnectorService) field.
	 */
	private LogListenable logListener;

	/**
	 * Define the log listener reference (ConnectorReference) field.
	 */
	private ConnectorReference<LogListenable> logListenerRef;

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
	public DirectLoggingAdapter(final Configurable configuration) {
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
			this.logListener = this.logListenerRef.getConnector();
		}
		this.logListener.bind();
		this.logPublisher.bind(this.logListener.getLogListener());
	}

	/**
	 * Gets the logging connector (LoggingConnectorService) value.
	 * 
	 * @return The logging connector (LoggingConnectorService) value.
	 */
	public LogPublishable getLogPublisher() {
		return this.logPublisher;
	}

	/**
	 * Gets the log listener connector (LogListenerConnectorService) value.
	 * 
	 * @return The log listener  connector (LogListenerConnectorService) value.
	 */
	public LogListenable getLogListener() {
		return this.logListener;
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
		return super.isEnabled() && this.logPublisher.isEnabled();
	}

	/**
	 * Sets the logging connector value.
	 * 
	 * @param logPublisher
	 *            The logging connector (<code>LoggingConnectorService</code>)
	 *            parameter.
	 */
	public void setLogPublisher(final LogPublishable logPublisher) {
		this.logPublisher = logPublisher;
	}

	/**
	 * Sets the log listener connector value.
	 * 
	 * @param logListener
	 *            The log listener connector (<code>LogListenerConnectorService</code>)
	 *            parameter.
	 */
	public void setLogListener(final LogListenable logListener) {
		this.logListener = logListener;
	}

	void setLogListenerRef(ConnectorReference<LogListenable> logListenerRef) {
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
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.logPublisher.unbind(this.logListener.getLogListener());
		this.logListener.unbind();
	}

}
