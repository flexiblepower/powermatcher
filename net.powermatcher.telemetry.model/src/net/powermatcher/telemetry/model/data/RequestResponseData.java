package net.powermatcher.telemetry.model.data;


import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class RequestResponseData extends Data {
	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see 
	 *      #RequestResponseData(String,String,Set,Date)
	 */
	public RequestResponseData(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Constructs an instance of this class from the specified request type,
	 * request ID, property set and ts parameters.
	 * 
	 * @param requestType
	 *            The request type (<code>String</code>) parameter.
	 * @param requestId
	 *            The request ID (<code>String</code>) parameter.
	 * @param propertySet
	 *            The property set (<code>Set<RequestResponseProperty></code>)
	 *            parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #RequestResponseData(Map)
	 */
	public RequestResponseData(final String requestType, final String requestId,
			final Set<RequestResponseProperty> propertySet, final Date ts) {
		this.dataMap.put(TelemetryModelConstants.REQUEST_TYPE_DATA_KEY, requestType);
		this.dataMap.put(TelemetryModelConstants.REQUEST_ID_DATA_KEY, requestId);
		this.dataMap.put(TelemetryModelConstants.TIMESTAMP_DATA_KEY, ts);
		int propertyIndex = 0;
		for (RequestResponseProperty requestResponseProperty : propertySet) {
			Map<String, Object> propertyMap = new HashMap<String, Object>();
			this.dataMap.put(TelemetryModelConstants.PROPERTY_DATA_KEY + '_' + propertyIndex++, propertyMap);
			propertyMap.put(TelemetryModelConstants.NAME_DATA_KEY, requestResponseProperty.getName());
			propertyMap.put(TelemetryModelConstants.VALUE_DATA_KEY, requestResponseProperty.getValue());
			if (requestResponseProperty.getLogging() != null) {
				propertyMap.put(TelemetryModelConstants.LOGGING_DATA_KEY, requestResponseProperty.getLogging());
			}
		}
	}

	/**
	 * Gets the properties (Set<RequestResponseProperty>) value.
	 * 
	 * @return The properties (<code>Set<RequestResponseProperty></code>) value.
	 */
	public Set<RequestResponseProperty> getProperties() {
		Map<String, Object>[] propertyMaps = getChildren(TelemetryModelConstants.PROPERTY_DATA_KEY);
		Set<RequestResponseProperty> propertySet = new HashSet<RequestResponseProperty>();
		for (int j = 0; j < propertyMaps.length; j++) {
			Map<String, Object> propertyMap = propertyMaps[j];
			String name = (String) propertyMap.get(TelemetryModelConstants.NAME_DATA_KEY);
			String value = (String) propertyMap.get(TelemetryModelConstants.VALUE_DATA_KEY);
			Boolean logging = (Boolean) propertyMap.get(TelemetryModelConstants.LOGGING_DATA_KEY);
			propertySet.add(new RequestResponseProperty(name, value, logging));
		}
		return propertySet;
	}

	/**
	 * Gets the request ID (String) value.
	 * 
	 * @return The request ID (<code>String</code>) value.
	 */
	public String getRequestId() {
		return (String) this.dataMap.get(TelemetryModelConstants.REQUEST_ID_DATA_KEY);
	}

	/**
	 * Gets the request type (String) value.
	 * 
	 * @return The request type (<code>String</code>) value.
	 */
	public String getRequestType() {
		return (String) this.dataMap.get(TelemetryModelConstants.REQUEST_TYPE_DATA_KEY);
	}

	/**
	 * Gets the time stamp (Date) value.
	 * 
	 * @return The time stamp (<code>Date</code>) value.
	 */
	public Date getTimestamp() {
		return (Date) this.dataMap.get(TelemetryModelConstants.TIMESTAMP_DATA_KEY);
	}

}
