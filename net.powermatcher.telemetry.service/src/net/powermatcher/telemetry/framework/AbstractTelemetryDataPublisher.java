package net.powermatcher.telemetry.framework;


import java.util.Date;
import java.util.Set;

import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import net.powermatcher.telemetry.model.data.AlertData;
import net.powermatcher.telemetry.model.data.Data;
import net.powermatcher.telemetry.model.data.MeasurementData;
import net.powermatcher.telemetry.model.data.RequestData;
import net.powermatcher.telemetry.model.data.RequestResponseProperty;
import net.powermatcher.telemetry.model.data.ResponseData;
import net.powermatcher.telemetry.model.data.StatusData;
import net.powermatcher.telemetry.model.data.TelemetryData;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractTelemetryDataPublisher {
	/**
	 * Define the configuration service (ConfigurationService) field.
	 */
	private Configurable configurationService;
	/**
	 * Define the cluster ID (String) field.
	 */
	private String clusterId;
	/**
	 * Define the agent ID (String) field.
	 */
	private String agentId;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * service.
	 * 
	 * @param configurationService
	 *            The configuration service (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public AbstractTelemetryDataPublisher(final Configurable configurationService) {
		this.configurationService = configurationService;
		getSettingsFromProperties();
	}

	/**
	 * Create telemetry data and return the TelemetryData result.
	 * 
	 * @return Results of the create telemetry data (<code>TelemetryData</code>)
	 *         value.
	 */
	protected TelemetryData createTelemetryData() {
		return new TelemetryData(this.clusterId, this.agentId);
	}

	/**
	 * Initialize.
	 */
	private void getSettingsFromProperties() {
		this.clusterId = this.configurationService.getProperty(ConnectableObjectConfiguration.CLUSTER_ID_PROPERTY, (String) null);
		this.agentId = this.configurationService.getProperty(ConnectableObjectConfiguration.ID_PROPERTY, (String) null);
	}

	/**
	 * Publish generic data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>data</code>) parameter.
	 * @see #publishData(Data[])
	 */
	public void publishData(final Data data) {
		TelemetryData telemetryData = createTelemetryData();
		telemetryData.addData(data);
		publishTelemetryData(telemetryData);
	}

	/**
	 * Publish generic data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>Data[]</code>) parameter.
	 * @see #publishData(Data)
	 */
	public void publishData(final Data[] data) {
		TelemetryData telemetryData = createTelemetryData();
		for (int i = 0; i < data.length; i++) {
			telemetryData.addData(data[i]);
		}
		publishTelemetryData(telemetryData);
	}

	/**
	 * Publish alert data with the specified alert data parameter.
	 * 
	 * @param alertData
	 *            The alert data (<code>AlertData</code>) parameter.
	 * @see #publishAlertData(AlertData[])
	 * @see #publishAlertData(String,Date)
	 */
	public void publishAlertData(final AlertData alertData) {
		publishData(alertData);
	}

	/**
	 * Publish alert data with the specified alert data parameter.
	 * 
	 * @param alertData
	 *            The alert data (<code>AlertData[]</code>) parameter.
	 * @see #publishAlertData(AlertData)
	 * @see #publishAlertData(String,Date)
	 */
	public void publishAlertData(final AlertData[] alertData) {
		publishData(alertData);
	}

	/**
	 * Publish alert data with the specified value and ts parameters.
	 * 
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #publishAlertData(AlertData)
	 * @see #publishAlertData(AlertData[])
	 */
	public void publishAlertData(final String value, final Date ts) {
		AlertData alertData = new AlertData(value, ts);
		publishData(alertData);
	}

	/**
	 * Publish measurement data with the specified measurement data parameter.
	 * 
	 * @param measurementData
	 *            The measurement data (<code>MeasurementData</code>) parameter.
	 * @see #publishMeasurementData(MeasurementData[])
	 * @see #publishMeasurementData(String,String,Float,Integer,Date)
	 */
	public void publishMeasurementData(final MeasurementData measurementData) {
		publishData(measurementData);
	}

	/**
	 * Publish measurement data with the specified measurement data parameter.
	 * 
	 * @param measurementData
	 *            The measurement data (<code>MeasurementData[]</code>)
	 *            parameter.
	 * @see #publishMeasurementData(MeasurementData)
	 * @see #publishMeasurementData(String,String,Float,Integer,Date)
	 */
	public void publishMeasurementData(final MeasurementData[] measurementData) {
		publishData(measurementData);
	}

	/**
	 * Publish measurement data with the specified value name, units, value,
	 * period and ts parameters.
	 * 
	 * @param valueName
	 *            The value name (<code>String</code>) parameter.
	 * @param units
	 *            The units (<code>String</code>) parameter.
	 * @param value
	 *            The value (<code>Float</code>) parameter.
	 * @param period
	 *            The period (<code>Integer</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #publishMeasurementData(MeasurementData)
	 * @see #publishMeasurementData(MeasurementData[])
	 */
	public void publishMeasurementData(final String valueName, final String units, final Float value, final Integer period,
			final Date ts) {
		MeasurementData measurementData = new MeasurementData(valueName, units);
		measurementData.addSingleValue(value, period, ts);
		publishData(measurementData);
	}

	/**
	 * Publish request data with the specified request data parameter.
	 * 
	 * @param requestData
	 *            The request data (<code>RequestData</code>)
	 *            parameter.
	 * @see #publishRequestData(RequestData[])
	 * @see #publishRequestData(String,String,Set,Date)
	 */
	public void publishRequestData(final RequestData requestData) {
		publishData(requestData);
	}

	/**
	 * Publish request data with the specified request data parameter.
	 * 
	 * @param requestData
	 *            The request data (<code>RequestData[]</code>)
	 *            parameter.
	 * @see #publishRequestData(RequestData)
	 * @see #publishRequestData(String,String,Set,Date)
	 */
	public void publishRequestData(final RequestData[] requestData) {
		publishData(requestData);
	}

	/**
	 * Publish request data with the specified request type, request ID,
	 * properties and ts parameters.
	 * 
	 * @param requestType
	 *            The request type (<code>String</code>) parameter.
	 * @param requestId
	 *            The request ID (<code>String</code>) parameter.
	 * @param properties
	 *            The properties (<code>Set<RequestResponseProperty></code>)
	 *            parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #publishRequestData(RequestData)
	 * @see #publishRequestData(RequestData[])
	 */
	public void publishRequestData(final String requestType, final String requestId,
			final Set<RequestResponseProperty> properties, final Date ts) {
		RequestData requestData = new RequestData(requestType, requestId, properties, ts);
		publishData(requestData);
	}

	/**
	 * Publish response data with the specified response data parameter.
	 * 
	 * @param responseData
	 *            The response data (<code>ResponseData</code>)
	 *            parameter.
	 * @see #publishResponseData(ResponseData[])
	 * @see 
	 *      #publishResponseData(String,String,Set,Date)
	 */
	public void publishResponseData(final ResponseData responseData) {
		publishData(responseData);
	}

	/**
	 * Publish response data with the specified request response data parameter.
	 * 
	 * @param responseData
	 *            The  response data (<code>ResponseData[]</code>)
	 *            parameter.
	 * @see #publishResponseData(ResponseData)
	 * @see 
	 *      #publishResponseData(String,String,Set,Date)
	 */
	public void publishResponseData(final ResponseData[] responseData) {
		publishData(responseData);
	}

	/**
	 * Publish response data with the specified request type, request ID,
	 * properties and ts parameters.
	 * 
	 * @param requestType
	 *            The request type (<code>String</code>) parameter.
	 * @param requestId
	 *            The request ID (<code>String</code>) parameter.
	 * @param properties
	 *            The properties (<code>Set<RequestResponseProperty></code>)
	 *            parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #publishResponseData(ResponseData)
	 * @see #publishResponseData(ResponseData[])
	 */
	public void publishResponseData(final String requestType, final String requestId,
			final Set<RequestResponseProperty> properties, final Date ts) {
		ResponseData responseData = new ResponseData(requestType, requestId, properties, ts);
		publishData(responseData);
	}

	/**
	 * Publish status data with the specified status data parameter.
	 * 
	 * @param statusData
	 *            The status data (<code>StatusData</code>) parameter.
	 * @see #publishStatusData(StatusData[])
	 * @see #publishStatusData(String,String,Date)
	 */
	public void publishStatusData(final StatusData statusData) {
		publishData(statusData);
	}

	/**
	 * Publish status data with the specified status data parameter.
	 * 
	 * @param statusData
	 *            The status data (<code>StatusData[]</code>) parameter.
	 * @see #publishStatusData(StatusData)
	 * @see #publishStatusData(String,String,Date)
	 */
	public void publishStatusData(final StatusData[] statusData) {
		publishData(statusData);
	}

	/**
	 * Publish status data with the specified value name, value and ts
	 * parameters.
	 * 
	 * @param valueName
	 *            The value name (<code>String</code>) parameter.
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #publishStatusData(StatusData)
	 * @see #publishStatusData(StatusData[])
	 */
	public void publishStatusData(final String valueName, final String value, final Date ts) {
		StatusData statusData = new StatusData(valueName);
		statusData.addSingleValue(value, ts);
		publishData(statusData);
	}

	/**
	 * Publish telemetry data object.
	 * 
	 * @param telemetryData The constructed telemetry data object.
	 */
	abstract protected void publishTelemetryData(TelemetryData telemetryData);

}
