package net.powermatcher.core.messaging.protocol.adapter.component;


import net.powermatcher.core.messaging.protocol.adapter.config.LoggingAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a LoggingAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a LoggingAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see LoggingAdapterFactoryComponent
 * @see LoggingAdapterConfiguration
 */
@OCD(name = LoggingAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = LoggingAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface LoggingAdapterFactoryComponentConfiguration extends LoggingAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Logging Messaging Adapter Factgory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Messaging adapter for the logging interface of a PowerMatcher component";

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
