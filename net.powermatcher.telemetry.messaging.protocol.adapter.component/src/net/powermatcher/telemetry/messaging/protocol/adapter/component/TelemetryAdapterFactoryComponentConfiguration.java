package net.powermatcher.telemetry.messaging.protocol.adapter.component;


import net.powermatcher.telemetry.messaging.protocol.adapter.config.TelemetryAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a TelemetryAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a TelemetryAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryAdapterFactoryComponent
 * @see TelemetryAdapterConfiguration
 */
@OCD(name = TelemetryAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = TelemetryAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface TelemetryAdapterFactoryComponentConfiguration extends TelemetryAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Telemetry Messaging Adapter";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Messaging adapter for the telemetry interface of a PowerMatcher component";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = false, description = CONNECTOR_ID_DESCRIPTION)
	public String connector_id();

	@Override
	@Meta.AD(required = false, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = MESSAGING_ADAPTER_FACTORY_DESCRIPTION)
	public String messaging_adapter_factory();

}
