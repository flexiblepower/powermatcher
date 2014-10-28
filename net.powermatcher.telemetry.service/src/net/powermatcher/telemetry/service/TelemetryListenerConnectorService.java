package net.powermatcher.telemetry.service;

import net.powermatcher.core.adapter.service.ConnectorService;


/**
 * 
 * The TelemetryListenerConnectorService defines the interface to connect to an object that
 * implements a listener for the TelemetryService interface. Through
 * this interface the adapter can retrieve the telemetry listener interface.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface TelemetryListenerConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "telemetry.listener.adapter.factory";

	/**
	 * Bind to the connector.
	 * 
	 * @throws Exception
	 *             Exception.
	 * @see #unbind()
	 */
	public void bind() throws Exception;
	
	/**
	 * Gets the telemetry listener (TelemetryService) value.
	 * 
	 * @return The telemetry listener (<code>TelemetryService</code>) value.
	 */
	public TelemetryService getTelemetryListener();

	/**
	 * Unbind from the connector.
	 * 
	 * @see #bind()
	 */
	public void unbind();

}
