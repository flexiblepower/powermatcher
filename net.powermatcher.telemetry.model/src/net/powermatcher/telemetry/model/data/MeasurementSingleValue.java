package net.powermatcher.telemetry.model.data;


import java.util.Date;
import java.util.Map;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MeasurementSingleValue extends Data {
	/**
	 * Constructs an instance of this class from the specified value, period and
	 * ts parameters.
	 * 
	 * @param value
	 *            The value (<code>Float</code>) parameter.
	 * @param period
	 *            The period (<code>Integer</code>) parameter.
	 * @param ts
	 *            The ts (<code>Date</code>) parameter.
	 * @see #MeasurementSingleValue(Map)
	 */
	public MeasurementSingleValue(final Float value, final Integer period, final Date ts) {
		this.dataMap.put(TelemetryModelConstants.VALUE_DATA_KEY, value);
		this.dataMap.put(TelemetryModelConstants.PERIOD_DATA_KEY, period);
		this.dataMap.put(TelemetryModelConstants.TIMESTAMP_DATA_KEY, ts);
	}

	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #MeasurementSingleValue(Float,Integer,Date)
	 */
	public MeasurementSingleValue(final Map<String, Object> dataMap) {
		super(dataMap);
	}

	/**
	 * Gets the period (Integer) value.
	 * 
	 * @return The period (<code>Integer</code>) value.
	 */
	public Integer getPeriod() {
		return (Integer) this.dataMap.get(TelemetryModelConstants.PERIOD_DATA_KEY);
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
		return TelemetryModelConstants.SINGLE_VALUE_DATA_KEY;
	}

	/**
	 * Gets the value (Float) value.
	 * 
	 * @return The value (<code>Float</code>) value.
	 * @see #MeasurementSingleValue(Map)
	 * @see #MeasurementSingleValue(Float,Integer,Date)
	 */
	public Float getValue() {
		return (Float) this.dataMap.get(TelemetryModelConstants.VALUE_DATA_KEY);
	}

}
