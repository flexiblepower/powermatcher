package net.powermatcher.expeditor.broker.manager.component;


import java.util.Map;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.expeditor.broker.manager.Pipe;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import com.ibm.micro.admin.AdminException;
import com.ibm.micro.admin.Broker;

/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = PipeComponent.COMPONENT_NAME, designateFactory = PipeConfiguration.class)
public class PipeComponent extends Pipe {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.expeditor.broker.manager.Pipe";

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
			startPipe();
		} catch (final AdminException e) {
			/* ignore exception */
		}
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	void deactivate() {
		stopPipe();
	}

	/**
	 * Sets the broker value.
	 * 
	 * @param broker
	 *            The broker (<code>Broker</code>) parameter.
	 */
	@Reference
	void setBroker(final Broker broker) {
		String name = "";
		try {
			name = broker.getName();
			setBridge(broker.getBridge());
		} catch (final AdminException e) {
			logger.error("Failed to acquire bridge of broker " + name, e);
		}
	}

}
