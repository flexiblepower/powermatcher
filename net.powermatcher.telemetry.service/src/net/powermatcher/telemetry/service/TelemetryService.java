package net.powermatcher.telemetry.service;


import net.powermatcher.telemetry.model.data.TelemetryData;

/**
 * Defines the interface for processing telemetry data.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface TelemetryService {
	/**
	 * Process telemetry data with the specified data parameter. The data can
	 * contain any conbination of Alert, Control, Measurement, Request, Status,
	 * Response and Topology data.
	 * 
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 */
	public void processTelemetryData(final TelemetryData data);

}
