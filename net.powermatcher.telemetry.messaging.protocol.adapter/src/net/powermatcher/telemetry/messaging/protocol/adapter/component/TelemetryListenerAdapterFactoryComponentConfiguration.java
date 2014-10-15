package net.powermatcher.telemetry.messaging.protocol.adapter.component;


import net.powermatcher.telemetry.messaging.protocol.adapter.config.TelemetryListenerAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a TelemetryListenerAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a TelemetryListenerAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see TelemetryListenerAdapterFactoryComponent
 * @see TelemetryListenerAdapterConfiguration
 */
@OCD(name = TelemetryListenerAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = TelemetryListenerAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface TelemetryListenerAdapterFactoryComponentConfiguration extends TelemetryListenerAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Telemetry Listener Adapter";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Messaging adapter for handling telemetry events from PowerMatcher components";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT)
	public String cluster_id();

	@Override
	@Meta.AD(required = true)
	public String id();

	@Override
	@Meta.AD(required = false, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = MESSAGING_ADAPTER_FACTORY_DESCRIPTION)
	public String messaging_adapter_factory();

}
