package net.powermatcher.der.agent.miele.at.home.component;

/********************************************
 * Copyright (c) 2012, 2013 Alliander.      *
 * All rights reserved.                     *
 *                                          *
 * Contributors:                            *
 *     IBM - initial API and implementation *
 *******************************************/

import java.util.Map;

import net.powermatcher.core.adapter.component.ConnectableRegistration;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.ConnectableObject;
import net.powermatcher.der.agent.miele.at.home.MieleDishWasherDeviceAgent;

import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = MieleDishWasherDeviceAgentComponent.COMPONENT_NAME, designateFactory = MieleDishWasherDeviceAgentComponentConfiguration.class)
public class MieleDishWasherDeviceAgentComponent extends MieleDishWasherDeviceAgent {

	/**
	 * 
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.der.agent.miele.at.home.MieleDishWasherDeviceAgent";
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