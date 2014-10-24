package net.powermatcher.core.agent.test.component;


import java.util.Map;

import net.powermatcher.core.adapter.component.ConnectableRegistration;
import net.powermatcher.core.agent.test.TestAgent;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.ConnectableObject;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;


/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = TestAgentComponent.COMPONENT_NAME, designateFactory = TestAgentComponentConfiguration.class)
public class TestAgentComponent extends TestAgent {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.agent.test.TestAgent";
	/**
	 * Define the service registration (ConnectableRegistration) constant.
	 */
	private ConnectableRegistration<ConnectableObject> serviceRegistration  = new ConnectableRegistration<ConnectableObject>(this);

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param context OSGi bundle context.
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	void activate(final BundleContext context, final Map<String, Object> properties) {
		Configurable configuration = new BaseConfiguration(properties);
		setConfiguration(configuration);
		this.serviceRegistration.register(context);
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	void deactivate() {
		this.serviceRegistration.unregister();
	}

}
