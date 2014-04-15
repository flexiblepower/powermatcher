package net.powermatcher.core.agent.logging;


import java.text.DateFormat;
import java.util.Date;

/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class LogRecord {
	/**
	 * Define the cluster ID (String) field.
	 */
	protected String clusterId;
	/**
	 * Define the ID (String) field.
	 */
	protected String id;
	/**
	 * Define the qualifier (String) field.
	 */
	protected String qualifier;

	/**
	 * Constructs an instance of this class from the specified equipment ID
	 * parameter.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param id
	 *            The ID (<code>String</code>) parameter.
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 */
	protected LogRecord(final String clusterId, final String id, final String qualifier) {
		this.clusterId = clusterId;
		this.id = id;
		this.qualifier = qualifier;
	}

	/**
	 * Clear the log record to indicate that there is no new data for this
	 * equipment id.
	 */
	public abstract void clear();

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
	public abstract String[] getDataRow(final DateFormat dateFormat, final Date logTime);

	/**
	 * Gets the equipment ID (String) value.
	 * 
	 * @return The equipment ID (<code>String</code>) value.
	 */
	public String getEquipmentId() {
		return this.id;
	}

	/**
	 * Gets the key (String) value.
	 * 
	 * @return The key (<code>String</code>) value.
	 */
	public String getKey() {
		return this.id;
	}

}
