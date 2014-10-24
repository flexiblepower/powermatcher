package net.powermatcher.agent.peakshavingconcentrator.component;

import java.util.Map;

import net.powermatcher.agent.peakshavingconcentrator.PeakShavingConcentrator;
import net.powermatcher.core.adapter.component.ConnectableRegistration;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.ConnectableObject;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

/**
 * @author TNO, IBM
 * @version 0.9.0
 */
@Component(name = PeakShavingConcentratorComponent.COMPONENT_NAME, designateFactory = PeakShavingConcentratorComponentConfiguration.class)
public class PeakShavingConcentratorComponent extends PeakShavingConcentrator {

	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.agent.peakshavingconcentrator.PeakShavingConcentrator";
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