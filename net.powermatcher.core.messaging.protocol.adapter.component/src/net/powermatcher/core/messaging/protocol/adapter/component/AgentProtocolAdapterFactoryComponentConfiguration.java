package net.powermatcher.core.messaging.protocol.adapter.component;


import net.powermatcher.core.messaging.protocol.adapter.config.AgentProtocolAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a IndirectProtocolAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a IndirectProtocolAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see AgentProtocolAdapterFactoryComponent
 * @see AgentProtocolAdapterConfiguration
 */
@OCD(name = AgentProtocolAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = AgentProtocolAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface AgentProtocolAdapterFactoryComponentConfiguration extends AgentProtocolAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Agent Messaging Protocol Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Messaging protocol adapter for the Agent interface of a PowerMatcher component";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = true, deflt = PROTOCOL_PROPERTY_DEFAULT, description = PROTOCOL_DESCRIPTION, optionValues = { "INTERNAL_v1", "HAN_rev6" }, optionLabels = {
			"INTERNAL v1",
			"NXP HAN rev.6" })
	public String messaging_protocol();

	@Override
	@Meta.AD(required = false, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = MESSAGING_ADAPTER_FACTORY_DESCRIPTION)
	public String messaging_adapter_factory();

}
