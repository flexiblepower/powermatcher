package net.powermatcher.telemetry.messaging.protocol.adapter;


import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.framework.MessagingAdapter;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.telemetry.messaging.protocol.adapter.constants.TelemetryConstants;
import net.powermatcher.telemetry.model.converter.XMLConverter;
import net.powermatcher.telemetry.model.data.TelemetryData;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * Adapter class that provides telemetry messaging subscribe functionality.
 * 
 * <p>
 * The TelemetryListenerAdapter can bind to a component that implements the TelemetryListenerConnectorService.
 * </p>
 * <p>
 * The adapter provides the following services:
 * <ul>
 * <li>Receiving of telemetry events from the messaging bus.
 * The adapter will read the and convert the message and then invoke the TelemetryService 
 * interface of the telemetry data listener that was retrieved from the 
 * TelemetryListenerConnectorService interface. </li>
 * </ul>
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see MessagingAdapter
 * @see TelemetryService
 * @see TelemetryConstants
 * @see TelemetryListenerConnectorService
 * @see TelemetryData
 * @see Topic
 */
public class TelemetryListenerAdapter extends MessagingAdapter implements TelemetryConstants {

	/**
	 * Define the telemetry data message pattern (Topic) field.
	 */
	private Topic telemetryDataMessagePattern;
	/**
	 * Define the legacy telemetry data message pattern (Topic) field.
	 */
	private Topic legacyTelemetryDataMessagePattern;

	/**
	 * Define the telemetry connector (TelemetryConnectorService) field.
	 */
	private TelemetryListenerConnectorService telemetryListenerConnector;

	/**
	 * Define the telemetry data listener (TelemetryDataService) field.
	 */
	private TelemetryService telemetryDataListener;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #TelemetryListenerAdapter(Configurable)
	 */
	public TelemetryListenerAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #TelemetryListenerAdapter()
	 */
	public TelemetryListenerAdapter(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Convert device message with the specified message topic and xml
	 * parameters and return the Map result.
	 * 
	 * @param messageTopic
	 *            The message topic (<code>Topic</code>) parameter.
	 * @param xml
	 *            The XML (<code>String</code>) parameter.
	 * @return Results of the convert device message (<code>TelemetryData</code>
	 *         ) data.
	 */
	private TelemetryData convertDeviceMessage(final Topic messageTopic, final String xml) {
		try {
			return XMLConverter.toData(xml);
		} catch (final Exception e) {
			logError("Failed to convert telemetry message: " + xml, e);
		}
		return null;
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>Topic[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[] { this.telemetryDataMessagePattern, this.legacyTelemetryDataMessagePattern };
	}

	/**
	 * Gets the telemetry listener connector (TelemetryConnectorService) value.
	 * 
	 * @return The telemetry listener connector (TelemetryListenerConnectorService) value.
	 * @see #setTelemetryListenerConnector(TelemetryListenerConnectorService)
	 */
	public TelemetryListenerConnectorService getTelemetryListenerConnector() {
		return this.telemetryListenerConnector;
	}

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has
	 *         been handled.
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic, final String data) {
		if (topic.matches(this.telemetryDataMessagePattern)) {
			handleTelemetryDataMessage(topic, data);
		} else {
			return super.handleMessageArrived(topic, data);
		}
		return true;
	}

	/**
	 * Handle telemetry data message with the specified message topic and xml
	 * parameters.
	 * 
	 * @param messageTopic
	 *            The message topic (<code>Topic</code>) parameter.
	 * @param xml
	 *            The XML (<code>String</code>) parameter.
	 */
	protected void handleTelemetryDataMessage(final Topic messageTopic, final String xml) {
		TelemetryData data = convertDeviceMessage(messageTopic, xml);
		if (this.telemetryDataListener != null) {
			this.telemetryDataListener.processTelemetryData(data);
		}
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.telemetryDataMessagePattern = TELEMETRY_DATA_MESSAGE_PREFIX.addLevel(getClusterId()).addLevel(
				Topic.SINGLE_LEVEL_WILDCARD);
		this.legacyTelemetryDataMessagePattern = LEGACY_TELEMETRY_DATA_MESSAGE_PREFIX.addLevel(getClusterId()).addLevel(
				Topic.SINGLE_LEVEL_WILDCARD);
	}

	/**
	 * Is enabled.
	 * 
	 * @return The is enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && (this.telemetryListenerConnector == null || this.telemetryListenerConnector.isEnabled());
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
		if (configuration != null) {
			initialize();
		}
	}

	/**
	 * Sets the telemetry listener connector value.
	 * 
	 * @param telemetryListenerConnector
	 *            The telemetry listener connector (
	 *            <code>TelemetryListenerConnectorService</code>) parameter.
	 * @see #getTelemetryListenerConnector()
	 * 
	 */
	public void setTelemetryListenerConnector(final TelemetryListenerConnectorService telemetryListenerConnector) {
		this.telemetryListenerConnector = telemetryListenerConnector;
	}

	@Override
	public void bind() throws Exception {
		super.bind();
		this.telemetryDataListener = telemetryListenerConnector.getTelemetryListener();
		telemetryListenerConnector.bind();
	}

	@Override
	public void unbind() {
		this.telemetryDataListener = null;
		telemetryListenerConnector.unbind();
		super.unbind();
	}
	
	

}
