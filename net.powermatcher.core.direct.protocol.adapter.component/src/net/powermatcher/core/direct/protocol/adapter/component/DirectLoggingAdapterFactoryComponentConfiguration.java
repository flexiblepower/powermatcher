package net.powermatcher.core.direct.protocol.adapter.component;


import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.direct.protocol.adapter.config.DirectLoggingAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a DirectAdapterFactoryComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a DirectAdapterFactoryComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectLoggingAdapterFactoryComponent
 */
@OCD(name = DirectLoggingAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = DirectLoggingAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface DirectLoggingAdapterFactoryComponentConfiguration extends DirectLoggingAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Direct Logging Connection Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Adapter factory for directly connecting a PowerMatcher agent to a log listener";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, deflt = AgentConfiguration.LOGGING_ADAPTER_FACTORY_DEFAULT, description = ID_DESCRIPTION)
	public String id();

}
