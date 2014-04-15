package net.powermatcher.telemetry.model.data;


import java.util.Map;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public class TelemetryData extends Data {
	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #TelemetryData(String, String)
	 */
	public TelemetryData(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Constructs an instance of this class from the specified cluster ID,
	 * location ID, equipment type and equipment ID parameters.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @see #TelemetryData(Map)
	 */
	public TelemetryData(final String clusterId, final String agentId) {
		this.dataMap.put(TelemetryModelConstants.CLUSTER_ID_DATA_KEY, clusterId);
		this.dataMap.put(TelemetryModelConstants.AGENT_ID_DATA_KEY, agentId);
	}

	/**
	 * Add alert data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>AlertData</code>) parameter.
	 * @see #getAlertData()
	 */
	public void addAlertData(final AlertData data) {
		addData(data);
	}

	/**
	 * Add control data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>ControlData</code>) parameter.
	 * @see #getControlData()
	 */
	public void addControlData(final ControlData data) {
		addData(data);
	}

	/**
	 * Add measurement data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>MeasurementData</code>) parameter.
	 * @see #getMeasurementData()
	 */
	public void addMeasurementData(final MeasurementData data) {
		addData(data);
	}

	/**
	 * Add request data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>RequestResponseData</code>) parameter.
	 * @see #getRequestData()
	 */
	public void addRequestData(final RequestResponseData data) {
		addData(data);
	}

	/**
	 * Add response data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>RequestResponseData</code>) parameter.
	 */
	public void addResponseData(final RequestResponseData data) {
		addData(data);
	}

	/**
	 * Add status data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>StatusData</code>) parameter.
	 * @see #getStatusData()
	 */
	public void addStatusData(final StatusData data) {
		addData(data);
	}

	/**
	 * Gets the alert data (AlertData[]) value.
	 * 
	 * @return The alert data (<code>AlertData[]</code>) value.
	 * @see #addAlertData(AlertData)
	 */
	public AlertData[] getAlertData() {
		Map<String, Object>[] dataMaps = getChildren(TelemetryModelConstants.ALERT_DATA_KEY);
		AlertData[] data = new AlertData[dataMaps.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new AlertData(dataMaps[i]);
		}
		return data;
	}

	/**
	 * Gets the control data (ControlData[]) value.
	 * 
	 * @return The control data (<code>ControlData[]</code>) value.
	 * @see #addControlData(ControlData)
	 */
	public ControlData[] getControlData() {
		Map<String, Object>[] dataMaps = getChildren(TelemetryModelConstants.CONTROL_DATA_KEY);
		ControlData[] data = new ControlData[dataMaps.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new ControlData(dataMaps[i]);
		}
		return data;
	}

	/**
	 * Gets the agent ID (String) value.
	 * 
	 * @return The agent ID (<code>String</code>) value.
	 */
	public String getAgentId() {
		return (String) this.dataMap.get(TelemetryModelConstants.AGENT_ID_DATA_KEY);
	}

	/**
	 * Gets the key (String) value for the data type.
	 * 
	 * @return The data type key (<code>String</code>).
	 */
	@Override
	public String getKey() {
		return TelemetryModelConstants.TELEMETRY_DATA_KEY;
	}

	/**
	 * Gets the measurement data (MeasurementData[]) value.
	 * 
	 * @return The measurement data (<code>MeasurementData[]</code>) value.
	 * @see #addMeasurementData(MeasurementData)
	 */
	public MeasurementData[] getMeasurementData() {
		Map<String, Object>[] dataMaps = getChildren(TelemetryModelConstants.MEASUREMENT_DATA_KEY);
		MeasurementData[] data = new MeasurementData[dataMaps.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new MeasurementData(dataMaps[i]);
		}
		return data;
	}

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return The cluster ID (<code>String</code>) value.
	 */
	public String getClusterId() {
		return (String) this.dataMap.get(TelemetryModelConstants.CLUSTER_ID_DATA_KEY);
	}

	/**
	 * Gets the response data (ResponseData[]) value.
	 * 
	 * @return The response data (<code>ResponseData[]</code>) value.
	 * @see #addResponseData(RequestResponseData)
	 */
	public ResponseData[] getResponseData() {
		Map<String, Object>[] dataMaps = getChildren(TelemetryModelConstants.RESPONSE_DATA_KEY);
		ResponseData[] data = new ResponseData[dataMaps.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new ResponseData(dataMaps[i]);
		}
		return data;
	}

	/**
	 * Gets the request data (RequestData[]) value.
	 * 
	 * @return The request data (<code>RequestData[]</code>) value.
	 * @see #addRequestData(RequestResponseData)
	 */
	public RequestData[] getRequestData() {
		Map<String, Object>[] dataMaps = getChildren(TelemetryModelConstants.REQUEST_DATA_KEY);
		RequestData[] data = new RequestData[dataMaps.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new RequestData(dataMaps[i]);
		}
		return data;
	}

	/**
	 * Gets the status data (StatusData[]) value.
	 * 
	 * @return The status data (<code>StatusData[]</code>) value.
	 * @see #addStatusData(StatusData)
	 */
	public StatusData[] getStatusData() {
		Map<String, Object>[] dataMaps = getChildren(TelemetryModelConstants.STATUS_DATA_KEY);
		StatusData[] data = new StatusData[dataMaps.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = new StatusData(dataMaps[i]);
		}
		return data;
	}

}
