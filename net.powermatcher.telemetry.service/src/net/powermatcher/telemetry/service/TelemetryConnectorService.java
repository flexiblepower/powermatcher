package net.powermatcher.telemetry.service;

import net.powermatcher.core.adapter.service.Connectable;


/**
 * 
 * The TelemetryConnectorService defines the interface to connect to an object that
 * uses the TelemetryService interface to publish telemetry events. Through
 * this interface the adapter binds the telemetry interface to be used.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface TelemetryConnectorService extends Connectable {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "telemetry.adapter.factory";

	/**
	 * Bind the specified telemetry publisher.
	 * 
	 * @param telemetryPublisher
	 *            The telemetry publisher (<code>TelemetryService</code>)
	 *            to bind.
	 * @see #unbind(TelemetryService)
	 */
	public void bind(final TelemetryService telemetryPublisher);

	/**
	 * Unbind the specified telemetry publisher.
	 * 
	 * @param telemetryPublisher
	 *            The telemetry publisher (<code>TelemetryService</code>)
	 *            to unbind.
	 *            
	 * @see #bind(TelemetryService)
	 */
	public void unbind(final TelemetryService telemetryPublisher);

}
