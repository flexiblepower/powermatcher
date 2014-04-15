package net.powermatcher.core.messaging.protocol.adapter.component;


import net.powermatcher.core.messaging.protocol.adapter.config.MatcherProtocolAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a MatcherProtocolAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a MatcherProtocolAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = MatcherProtocolAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = MatcherProtocolAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface MatcherProtocolAdapterFactoryComponentConfiguration extends MatcherProtocolAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Matcher Messaging Protocol Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Messaging protocol adapter for the Matcher interface of a PowerMatcher component";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = true, deflt = PROTOCOL_PROPERTY_DEFAULT, description = PROTOCOL_DESCRIPTION, optionValues = { "INTERNAL_v1", "HAN_rev6" }, optionLabels = {
			"INTERNAL v1",
			"NXP HAN rev. 6" })
	public String messaging_protocol();

	@Override
	@Meta.AD(required = false, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = MESSAGING_ADAPTER_FACTORY_DESCRIPTION)
	public String messaging_adapter_factory();

}
