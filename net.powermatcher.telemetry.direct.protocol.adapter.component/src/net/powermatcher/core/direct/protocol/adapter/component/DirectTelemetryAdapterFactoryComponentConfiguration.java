package net.powermatcher.core.direct.protocol.adapter.component;


import net.powermatcher.telemetry.config.TelemetryConfiguration;
import net.powermatcher.telemetry.direct.protocol.adapter.config.DirectTelemetryAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a DirectTelemetryAdapterFactoryComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a DirectTelemetryAdapterFactoryComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectTelemetryAdapterFactoryComponent
 */
@OCD(name = DirectTelemetryAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = DirectTelemetryAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface DirectTelemetryAdapterFactoryComponentConfiguration extends DirectTelemetryAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Direct Telemetry Connection Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Adapter factory for directly connecting a PowerMatcher agent to a telemetry listener";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, deflt = TelemetryConfiguration.TELEMETRY_ADAPTER_FACTORY_DEFAULT, description = ID_DESCRIPTION)
	public String id();

}
