package net.powermatcher.telemetry.messaging.protocol.adapter.constants;


import net.powermatcher.core.messaging.framework.Topic;

/**
 * Defines the constants for telemetry data adapters. 
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface TelemetryConstants {
	/**
	 * Define the telemetry data message prefix (Topic) constant. The telemetry
	 * data message topic is used to send a message with any type or combination
	 * of Alert, Control, Measurement, Request, Status, Response and Topology.
	 */
	public static final Topic TELEMETRY_DATA_MESSAGE_PREFIX = Topic.create("Telemetry"); //$NON-NLS-1$

	/**
	 * Define the legacy telemetry data message prefix (Topic) constant.
	 * The legacy prefix supports logging of messages from external sources in the release 0.5 format.
	 * @deprecated
	 */
	public static final Topic LEGACY_TELEMETRY_DATA_MESSAGE_PREFIX = Topic.create("CPSS"); //$NON-NLS-1$
}
