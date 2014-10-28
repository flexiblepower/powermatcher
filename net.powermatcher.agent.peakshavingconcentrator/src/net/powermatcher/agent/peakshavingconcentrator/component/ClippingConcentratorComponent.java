package net.powermatcher.agent.peakshavingconcentrator.component;


import java.util.Map;

import net.powermatcher.agent.peakshavingconcentrator.ClippingConcentrator;
import net.powermatcher.core.adapter.component.ConnectableRegistration;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.ConnectableObject;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;


/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = ClippingConcentratorComponent.COMPONENT_NAME, designateFactory = ClippingConcentratorComponentConfiguration.class)
public class ClippingConcentratorComponent extends ClippingConcentrator {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.agent.peakshavingconcentrator.ClippingConcentrator";
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
		ConfigurationService configuration = new BaseConfiguration(properties);
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
