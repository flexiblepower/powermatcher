package net.powermatcher.expeditor.broker.manager.component;


import java.util.Map;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.expeditor.broker.manager.BrokerManager;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

import com.ibm.micro.admin.AdminException;

/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = BrokerComponent.COMPONENT_NAME, designate = BrokerConfiguration.class)
public class BrokerComponent extends BrokerManager {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.expeditor.broker.manager.BrokerManager";

	/**
	 * Activate with the specified properties parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Activate
	void activate(final Map<String, Object> properties) {
		ConfigurationService configuration = new BaseConfiguration(properties);
		setConfiguration(configuration);
		try {
			startBroker();
		} catch (final AdminException e) {
			/* ignore exception */
		}
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	void deactivate() {
		stopBroker();
	}

}
