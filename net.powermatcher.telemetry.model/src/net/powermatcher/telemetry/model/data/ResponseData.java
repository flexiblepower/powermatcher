package net.powermatcher.telemetry.model.data;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;



/**
 * @author IBM
 * @version 0.9.0
 */
public class ResponseData extends RequestResponseData {

	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see 
	 *      #ResponseData(String,String,Set,Date)
	 */
	public ResponseData(Map<String, Object> dataMap) {
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
	 * @see #ResponseData(Map)
	 */
	public ResponseData(String requestType, String requestId, Set<RequestResponseProperty> propertySet, Date ts) {
		super(requestType, requestId, propertySet, ts);
	}

	/**
	 * Gets the key (String) value for the data type.
	 * 
	 * @return The data type key (<code>String</code>).
	 */
	@Override
	public String getKey() {
		return TelemetryModelConstants.REQUEST_DATA_KEY;
	}


}
