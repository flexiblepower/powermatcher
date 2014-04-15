package net.powermatcher.telemetry.model.data;


import java.util.Date;
import java.util.Map;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public class StatusData extends Data {
	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #StatusData(String)
	 */
	public StatusData(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Constructs an instance of this class from the specified value name
	 * parameter.
	 * 
	 * @param valueName
	 *            The value name (<code>String</code>) parameter.
	 * @see #StatusData(Map)
	 */
	public StatusData(final String valueName) {
		this.dataMap.put(TelemetryModelConstants.VALUE_NAME_DATA_KEY, valueName);
	}

	/**
	 * Add single value with the specified value and ts parameters.
	 * 
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 */
	public void addSingleValue(final String value, final Date ts) {
		StatusSingleValue singleValue = new StatusSingleValue(value, ts);
		addData(singleValue);
	}

	/**
	 * Gets the key (String) value for the data type.
	 * 
	 * @return The data type key (<code>String</code>).
	 */
	@Override
	public String getKey() {
		return TelemetryModelConstants.STATUS_DATA_KEY;
	}

	/**
	 * Gets the single values (StatusSingleValue[]) value.
	 * 
	 * @return The single values (<code>StatusSingleValue[]</code>) value.
	 */
	public StatusSingleValue[] getSingleValues() {
		Map<String, Object>[] singleValueMaps = getChildren(TelemetryModelConstants.SINGLE_VALUE_DATA_KEY);
		StatusSingleValue[] singleValues = new StatusSingleValue[singleValueMaps.length];
		for (int i = 0; i < singleValues.length; i++) {
			singleValues[i] = new StatusSingleValue(singleValueMaps[i]);
		}
		return singleValues;
	}

	/**
	 * Gets the value name (String) value.
	 * 
	 * @return The value name (<code>String</code>) value.
	 */
	public String getValueName() {
		return (String) this.dataMap.get(TelemetryModelConstants.VALUE_NAME_DATA_KEY);
	}

}
