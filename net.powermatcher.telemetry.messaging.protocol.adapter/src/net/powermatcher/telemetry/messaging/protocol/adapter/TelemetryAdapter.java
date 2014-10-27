package net.powermatcher.telemetry.messaging.protocol.adapter;


import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.framework.MessagingAdapter;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.telemetry.messaging.protocol.adapter.constants.TelemetryConstants;
import net.powermatcher.telemetry.model.converter.XMLConverter;
import net.powermatcher.telemetry.model.data.TelemetryData;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * Adapter class that provides telemetry messaging publishing functionality.
 * 
 * <p>
 * The TelemetryAdapter can bind to a component that implements the TelemetryConnectorService.
 * </p>
 * <p>
 * The adapter provides the following services:
 * <ul>
 * <li>Publishing of telemetry events to the messaging bus. The adapter implements the TelemetryService
 * for the component to publish telemetry events. The implementation of processTelemetryData(TelemetryData) 
 * of this adapter publishes the TelemetryData on the configured messaging topic.</li>
 * </ul>
 * </p>
 * <p>
 * The adapter component can also serve as a parent class for an agent The agent will
 * inherit the telemetry functionality and does not need to use it as an adapter.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see MessagingAdapter
 * @see TelemetryService
 * @see TelemetryConstants
 * @see TelemetryConnectorService
 * @see TelemetryData
 * @see Topic
 */
public class TelemetryAdapter extends MessagingAdapter implements TelemetryService, TelemetryConstants {

	/**
	 * Define the telemetry agent ID (String) field.
	 */
	@SuppressWarnings("unused")
	private String telemetryAgentId;
	/**
	 * Define the generic data message topic (Topic) field.
	 */
	private Topic telemetryDataMessageTopic;
	/**
	 * Define the telemetry connector (TelemetryConnectorService) field.
	 */
	private TelemetryConnectorService telemetryConnector;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #TelemetryAdapter(Configurable)
	 */
	public TelemetryAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #TelemetryAdapter()
	 */
	public TelemetryAdapter(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>Topic[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[0];
	}

	/**
	 * Gets the telemetry connector (TelemetryConnectorService) value.
	 * 
	 * @return The telemetry connector (TelemetryConnectorService) value.
	 * @see #setTelemetryConnector(TelemetryConnectorService)
	 */
	public TelemetryConnectorService getTelemetryConnector() {
		return this.telemetryConnector;
	}

	/**
	 * Gets the telemetry data message topic (Topic) value.
	 * 
	 * @return The telemetry data message topic  (Topic) value.
	 */
	public Topic getTelemetryDataMessageTopic() {
		if (this.telemetryDataMessageTopic == null) {
			this.telemetryDataMessageTopic = TELEMETRY_DATA_MESSAGE_PREFIX.addLevel(getClusterId()).addLevel(getId());
		}
		return this.telemetryDataMessageTopic;
	}
	
	/**
	 * Is enabled.
	 * 
	 * @return The is enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && (this.telemetryConnector == null || this.telemetryConnector.isEnabled());
	}

	/**
	 * Publish telemetry data with the specified data parameter on the generic
	 * data message topic.
	 * 
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 */
	@Override
	public void processTelemetryData(final TelemetryData data) {
		publish(getTelemetryDataMessageTopic(), data);
	}

	/**
	 * Publish telemetry data with the specified data parameter on the 
	 * specified message topic.
	 * 
	 * @param topic The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 */
	protected void publish(Topic topic, TelemetryData data) {
		String xml = XMLConverter.toXMLString(data);
		publish(topic, xml);
	}

	/**
	 * Set the telemetry agent ID to publish to.
	 * @param telemetryAgentId The telemetry agent ID to publish to.
	 */
	public void setTelemetryAgentId(String telemetryAgentId) {
		this.telemetryAgentId = telemetryAgentId;
	}

	/**
	 * Sets the telemetry connector value.
	 * 
	 * @param telemetryConnector
	 *            The telemetry connector (
	 *            <code>TelemetryConnectorService</code>) parameter.
	 * @see #getTelemetryConnector()
	 * 
	 */
	public void setTelemetryConnector(final TelemetryConnectorService telemetryConnector) {
		this.telemetryConnector = telemetryConnector;
	}

	@Override
	public void bind() throws Exception {
		super.bind();
		this.telemetryConnector.bind(this);
	}

	@Override
	public void unbind() {
		this.telemetryConnector.unbind(null);
		super.unbind();
	}

}
