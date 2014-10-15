package net.powermatcher.core.messaging.adapter.template.component;


import net.powermatcher.core.messaging.adapter.template.config.ExampleAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * This interface defines the OSGi metatype Object Class Definition that specifies the
 * configuration properties for the <code>ExampleAdapterFactoryComponent</code>. The interface defines a
 * method for each configuration property that must be included in the generated metatype 
 * information. The name of the method must be the same as the configuration property, where
 * a '.' in the property name is replaced by a '_' to conform to Java naming constraints.
 * 
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = ExampleAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = ExampleAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface ExampleAdapterFactoryComponentConfiguration extends ExampleAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Example Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Example adapter factory for the Example service interface of the Example agent";

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
