package net.powermatcher.core.agent.template.config;

import net.powermatcher.core.agent.template.ExampleAgent1;
import net.powermatcher.core.agent.template.ExampleAgent2;
import net.powermatcher.core.agent.template.service.ExampleConnectorService;



/**
 * This interface defines the configuration properties and their default values (if any) for
 * PowerMatcher agent <code>ExampleAgent2</code>, which is an extension of <code>ExampleAgent1</code>.
 * As <code>ExampleAgent2</code> does not define any new properties, this interface is empty.
 * 
 * @see ExampleAgent1
 * @see ExampleAgent2
 * `see ExampleConnectorService
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleAgent2Configuration extends ExampleAgent1Configuration {

	/**
	 * Define the example adapter factory property (String) constant.
	 */
	public static final String EXAMPLE_ADAPTER_FACTORY_PROPERTY = ExampleConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the example adapter factory description (String) constant.
	 */
	public static final String EXAMPLE_ADAPTER_FACTORY_DESCRIPTION = "The adapter factory for creating the example adapter";

	/**
	 * Define the example property (String) constant.
	 * This is a configuration property that is used by the adapter for ExampleConnectorService.
	 */
	public static final String EXAMPLE_PROPERTY = "example.setting";
	/**
	 * Define the example default (String) constant.
	 */
	public static final String EXAMPLE_DEFAULT = "default value";
	/**
	 * Define the example description (String) constant.
	 */
	public static final String EXAMPLE_DESCRIPTION = "Example setting";

	/**
	 * Example_adapter_factory and return the String result.
	 * 
	 * @return Results of the agent_adaexample_adapter_factorypter_factory (<code>String</code>) value.
	 */
	public String example_adapter_factory();
	/**
	 * Access method for the <code>example.setting</code> property.
	 * This method is not implemented anywhere, but provides the signature for OSGi metatype annotations.
	 * 
	 * @return The value configured for the <code>example.setting</code> property.
	 */
	public String example_setting();

}
