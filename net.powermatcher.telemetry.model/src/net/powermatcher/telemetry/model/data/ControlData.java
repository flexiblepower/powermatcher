package net.powermatcher.telemetry.model.data;


import java.util.Date;
import java.util.Map;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ControlData extends Data {
	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #ControlData(String, String, String, Date)
	 */
	public ControlData(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Constructs an instance of this class from the specified value name, value
	 * and ts parameters.
	 * 
	 * @param valueName
	 *            The value name (<code>String</code>) parameter.
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @param units
	 *            The units (<code>String</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #ControlData(Map)
	 */
	public ControlData(final String valueName, final String value, final String units, final Date ts) {
		this.dataMap.put(TelemetryModelConstants.VALUE_NAME_DATA_KEY, valueName);
		this.dataMap.put(TelemetryModelConstants.VALUE_DATA_KEY, value);
		this.dataMap.put(TelemetryModelConstants.UNITS_DATA_KEY, units);
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
		return TelemetryModelConstants.CONTROL_DATA_KEY;
	}

	/**
	 * Gets the value (String) value.
	 * 
	 * @return The value (<code>String</code>) value.
	 */
	public String getValue() {
		return (String) this.dataMap.get(TelemetryModelConstants.VALUE_DATA_KEY);
	}

	/**
	 * Gets the value name (String) value.
	 * 
	 * @return The value name (<code>String</code>) value.
	 */
	public String getValueName() {
		return (String) this.dataMap.get(TelemetryModelConstants.VALUE_NAME_DATA_KEY);
	}

	/**
	 * Gets the units (String) value.
	 * 
	 * @return The units (<code>String</code>) value.
	 */
	public String getUnits() {
		return (String) this.dataMap.get(TelemetryModelConstants.UNITS_DATA_KEY);
	}
}
