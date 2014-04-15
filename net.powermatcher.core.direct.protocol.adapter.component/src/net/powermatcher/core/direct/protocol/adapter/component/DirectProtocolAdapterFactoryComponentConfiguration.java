package net.powermatcher.core.direct.protocol.adapter.component;


import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.direct.protocol.adapter.config.DirectProtocolAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a DirectProtocolAdapterFactoryComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a DirectProtocolAdapterFactoryComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see DirectProtocolAdapterFactoryComponent
 */
@OCD(name = DirectProtocolAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = DirectProtocolAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface DirectProtocolAdapterFactoryComponentConfiguration extends DirectProtocolAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Direct Protocol Connection Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Adapter factory for directly connecting a PowerMatcher agent to a matcher";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, deflt = AgentConfiguration.AGENT_ADAPTER_FACTORY_DEFAULT, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = false, deflt = SCHEDULER_ADAPTER_FACTORY_DEFAULT, description = SCHEDULER_ADAPTER_FACTORY_DESCRIPTION)
	public String scheduler_adapter_factory();


}
