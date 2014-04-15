package net.powermatcher.telemetry.model.data;


import java.util.Date;
import java.util.Map;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MeasurementData extends Data {
	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #MeasurementData(String,String)
	 */
	public MeasurementData(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Constructs an instance of this class from the specified value name and
	 * units parameters.
	 * 
	 * @param valueName
	 *            The value name (<code>String</code>) parameter.
	 * @param units
	 *            The units (<code>String</code>) parameter.
	 * @see #MeasurementData(Map)
	 */
	public MeasurementData(final String valueName, final String units) {
		this.dataMap.put(TelemetryModelConstants.VALUE_NAME_DATA_KEY, valueName);
		this.dataMap.put(TelemetryModelConstants.UNITS_DATA_KEY, units);
	}

	/**
	 * Gets the key (String) value for the data type.
	 * 
	 * @return The data type key (<code>String</code>).
	 */
	@Override
	public String getKey() {
		return TelemetryModelConstants.MEASUREMENT_DATA_KEY;
	}

	/**
	 * Add single value with the specified value, period and ts parameters.
	 * 
	 * @param value
	 *            The value (<code>Float</code>) parameter.
	 * @param period
	 *            The period (<code>Integer</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 */
	public void addSingleValue(final Float value, final Integer period, final Date ts) {
		MeasurementSingleValue singleValue = new MeasurementSingleValue(value, period, ts);
		addData(singleValue);
	}

	/**
	 * Gets the single values (MeasurementSingleValue[]) value.
	 * 
	 * @return The single values (<code>MeasurementSingleValue[]</code>) value.
	 */
	public MeasurementSingleValue[] getSingleValues() {
		Map<String, Object>[] singleValueMaps = getChildren(TelemetryModelConstants.SINGLE_VALUE_DATA_KEY);
		MeasurementSingleValue[] singleValues = new MeasurementSingleValue[singleValueMaps.length];
		for (int i = 0; i < singleValues.length; i++) {
			singleValues[i] = new MeasurementSingleValue(singleValueMaps[i]);
		}
		return singleValues;
	}

	/**
	 * Gets the units (String) value.
	 * 
	 * @return The units (<code>String</code>) value.
	 */
	public String getUnits() {
		return (String) this.dataMap.get(TelemetryModelConstants.UNITS_DATA_KEY);
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
