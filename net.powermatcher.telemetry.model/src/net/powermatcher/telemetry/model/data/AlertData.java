package net.powermatcher.telemetry.model.data;


import java.util.Date;
import java.util.Map;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public class AlertData extends Data {
	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #AlertData(String,Date)
	 */
	public AlertData(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Constructs an instance of this class from the specified value and ts
	 * parameters.
	 * 
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #AlertData(Map)
	 */
	public AlertData(final String value, final Date ts) {
		this.dataMap.put(TelemetryModelConstants.VALUE_DATA_KEY, value);
		this.dataMap.put(TelemetryModelConstants.TIMESTAMP_DATA_KEY, ts);
	}

	/**
	 * Gets the time stamp (Date) value.
	 * 
	 * @return The time stamp (<code>Date</code>) value.
	 */
	public Date getTimestamp() {
		return (Date) this.dataMap.get(TelemetryModelConstants.TIMESTAMP_DATA_KEY);
	}

	/**
	 * Gets the key (String) value for the data type.
	 * 
	 * @return The data type key (<code>String</code>).
	 */
	@Override
	public String getKey() {
		return TelemetryModelConstants.ALERT_DATA_KEY;
	}

	/**
	 * Gets the value (String) value.
	 * 
	 * @return The value (<code>String</code>) value.
	 */
	public String getValue() {
		return (String) this.dataMap.get(TelemetryModelConstants.VALUE_DATA_KEY);
	}

}
