package net.powermatcher.core.agent.template.component;


import java.util.Map;

import net.powermatcher.core.adapter.component.ConnectableRegistration;
import net.powermatcher.core.agent.template.ExampleAgent1;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.ConnectableObject;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

/**
 * This class exports a PowerMatcher agent as an OSGi managed factory service component.
 * An instance of this component is created and activated by the OSGi runtime for each factory configuration
 * that is created in the OSGi Configuration Admin service.
 * 
 * @see ExampleAgent1
 * @see ExampleAgent1ComponentConfiguration
 *  
 * @author IBM
 * @version 0.9.0
 */
@Component(name = ExampleAgent1Component.COMPONENT_NAME, designateFactory = ExampleAgent1ComponentConfiguration.class)
public class ExampleAgent1Component extends ExampleAgent1 {
	/**
	 * Define the component name (String) constant.
	 * The fully qualified name of the PowerMatcher agent is used, as it is also the basis for the
	 * persistent identifier in the OSGi configuration repository.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.agent.example.ExampleAgent1";
	/**
	 * Define the service registration (ConnectableRegistration) constant.
	 */
	private ConnectableRegistration<ConnectableObject> serviceRegistration  = new ConnectableRegistration<ConnectableObject>(this);

	/**
	 * Activate an instance of the agent with the specified configuration properties.
	 * This method is called from OSGi whenever a configuration item is created or updated in the
	 * OSGi configuration repository.
	 * 
	 * @param context OSGi bundle context.
	 * @param properties
	 *            The configuration properties (<code>Map<String,Object></code>) for the agent.
	 */
	@Activate
	void activate(final BundleContext context, final Map<String, Object> properties) {
		ConfigurationService configuration = new BaseConfiguration(properties);
		setConfiguration(configuration);
		this.serviceRegistration.register(context);
	}


	/**
	 * Deactivate an instance of the agent.
	 * This method is called from OSGi whenever a configuration item is deleted or about
	 * to be updated in the OSGi configuration repository.
	 */
	@Deactivate
	void deactivate() {
		this.serviceRegistration.unregister();
	}

}
