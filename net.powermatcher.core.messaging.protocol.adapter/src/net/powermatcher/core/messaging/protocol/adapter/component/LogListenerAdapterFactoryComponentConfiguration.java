package net.powermatcher.core.messaging.protocol.adapter.component;


import net.powermatcher.core.messaging.protocol.adapter.config.LogListenerAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a LogListenerAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a LogListenerAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see LogListenerAdapterFactoryComponent
 * @see LogListenerAdapterConfiguration
 */
@OCD(name = LogListenerAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = LogListenerAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface LogListenerAdapterFactoryComponentConfiguration extends LogListenerAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Log Listener Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Messaging adapter for handling logging events from PowerMatcher components";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = false, deflt = MESSAGING_ADAPTER_FACTORY_DEFAULT, description = MESSAGING_ADAPTER_FACTORY_DESCRIPTION)
	public String messaging_adapter_factory();

}
