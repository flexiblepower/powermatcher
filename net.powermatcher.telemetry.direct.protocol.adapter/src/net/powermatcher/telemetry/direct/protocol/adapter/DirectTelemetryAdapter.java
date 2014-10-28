package net.powermatcher.telemetry.direct.protocol.adapter;


import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.adapter.ConnectorReference;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * <p>
 * Adapter class to provide an agent with functionality to log
 * Power Matcher protocol events.
 * </p>
 * <p>
 * This adapter directly connects a PowerMatcher agent or matcher to a telemetry listener directly using
 * the TelemetryListenerService interfaces of the agent or matcher and the telemetry listener connectors.
 * </p>
 * <p>
 * The adapter is created for the loggin connector as primary connector interface. Upon binding,
 * the adapter locates the telemetry listener using the connector locater.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryService
 * @see TelemetryConnectorService
 */
public class DirectTelemetryAdapter extends Adapter {

	private ConnectorReference<TelemetryListenerConnectorService> telemetryListenerRef;

	void setTelemetryListenerRef(ConnectorReference<TelemetryListenerConnectorService> telemetryListenerRef) {
		this.telemetryListenerRef = telemetryListenerRef;
	}

	/**
	 * Define the telemetry connector (TelemetryConnectorService) field.
	 */
	private TelemetryConnectorService telemetryConnector;

	/**
	 * Define the telemetry listener connector (TelemetryListenerConnectorService) field.
	 */
	private TelemetryListenerConnectorService telemetryListenerConnector;

	/**
	 * Constructs an instance of this class.
	 */
	public DirectTelemetryAdapter() {
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
	public DirectTelemetryAdapter(final ConfigurationService configuration) {
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
		if (this.telemetryListenerRef != null) {
			this.telemetryListenerConnector = this.telemetryListenerRef.getConnector();
		}
		this.telemetryListenerConnector.bind();
		this.telemetryConnector.bind(this.telemetryListenerConnector.getTelemetryListener());
	}

	/**
	 * Gets the telemetry connector (TelemetryConnectorService) value.
	 * 
	 * @return The telemetry connector (TelemetryConnectorService) value.
	 */
	public TelemetryConnectorService getTelemetryConnector() {
		return this.telemetryConnector;
	}

	/**
	 * Gets the telemetry listener connector (TelemetryListenerConnectorService) value.
	 * 
	 * @return The telemetry listener  connector (TelemetryListenerConnectorService) value.
	 */
	public TelemetryListenerConnectorService getTelemetryListenerConnector() {
		return this.telemetryListenerConnector;
	}

	/**
	 * Gets the enabled (boolean) value.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.telemetryConnector.isEnabled();
	}

	/**
	 * Sets the telemetry connector value.
	 * 
	 * @param telemetryConnector
	 *            The telemetry connector (<code>TelemetryConnectorService</code>)
	 *            parameter.
	 */
	public void setTelemetryConnector(final TelemetryConnectorService telemetryConnector) {
		this.telemetryConnector = telemetryConnector;
	}

	/**
	 * Sets the telemetry listener connector value.
	 * 
	 * @param telemetryListenerConnector
	 *            The telemetry listener connector (<code>TelemetryListenerConnectorService</code>)
	 *            parameter.
	 */
	public void setTelemetryListenerConnector(final TelemetryListenerConnectorService telemetryListenerConnector) {
		this.telemetryListenerConnector = telemetryListenerConnector;
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		this.telemetryConnector.unbind(this.telemetryListenerConnector.getTelemetryListener());
		this.telemetryListenerConnector.unbind();
	}

}
