package net.powermatcher.agent.telemetry.logging;



import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.core.agent.logging.LogRecord;

/**
 * @author IBM
 * @version 0.9.0
 */
public class StatusLogRecord extends LogRecord {
	/**
	 * Define the header row (String[]) constant.
	 */
	private static final String[] HEADER_ROW = new String[] {
			"logTime",
			"clusterId",
			"agentId",
			"statusName",
			"statusValue",
			"lastUpdateTime" };

	/**
	 * Gets the header row (String[]) value.
	 * 
	 * @return The header row (<code>String[]</code>) value.
	 */
	public static String[] getHeaderRow() {
		return HEADER_ROW;
	}

	/**
	 * Define the last update time (Date) field.
	 * This is always the real time, not the possibly simulated time of the event stored in logTime.
	 */
	protected Date lastUpdateTime = new Date();
	/**
	 * Define the value name (String) field.
	 */
	private String valueName;

	/**
	 * Define the value (String) field.
	 */
	private String value;

	/**
	 * Constructs an instance of this class from the specified parameters.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param id
	 *            The agent ID (<code>String</code>) parameter.
	 * @param statusName
	 *            The status name (<code>String</code>) parameter.
	 */
	public StatusLogRecord(final String clusterId, final String id, final String statusName) {
		super(clusterId, id, null);
		this.valueName = statusName;
	}

	/**
	 * Clear the log record to indicate that there is no new data for this
	 * equipment id.
	 */
	@Override
	public void clear() {
		this.lastUpdateTime = null;
	}

	/**
	 * Get data row with the specified log time parameter and return the
	 * String[] result.
	 * 
	 * @param dateFormat
	 *            The date format (<code>DateFormat</code>) parameter.
	 * @param logTime
	 *            The log time (<code>Date</code>) parameter.
	 * @return Results of the get data row (<code>String[]</code>) value.
	 */
	@Override
	public String[] getDataRow(final DateFormat dateFormat, final Date logTime) {
		if (this.lastUpdateTime == null) {
			return new String[] {
					dateFormat.format(logTime),
					this.clusterId,
					this.id,
					this.valueName,
					null,
					null };
		} else {
			return new String[] {
					dateFormat.format(logTime),
					this.clusterId,
					this.id,
					this.valueName,
					this.value,
					dateFormat.format(this.lastUpdateTime) };
		}
	}

	/**
	 * Gets the key (String) value.
	 * 
	 * @return The key (<code>String</code>) value.
	 */
	@Override
	public String getKey() {
		return super.getKey() + '_' + this.valueName;
	}

	/**
	 * Gets the value (String) value.
	 * 
	 * @return The value (<code>String</code>) value.
	 * @see #setValue(String)
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * Gets the value name (String) value.
	 * 
	 * @return The value name (<code>String</code>) value.
	 */
	public String getValueName() {
		return this.valueName;
	}

	/**
	 * Sets the value value.
	 * 
	 * @param statusValue
	 *            The status value (<code>String</code>) parameter.
	 * @see #getValue()
	 */
	public void setValue(final String statusValue) {
		this.value = statusValue;
	}

}
