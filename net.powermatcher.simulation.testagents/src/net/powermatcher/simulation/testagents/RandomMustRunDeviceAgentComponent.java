package net.powermatcher.simulation.testagents;

import java.util.Map;

import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;
import net.powermatcher.core.scheduler.service.TimeConnectorService;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component(name = RandomMustRunDeviceAgentComponent.COMPONENT_NAME, designateFactory = RandomMustRunDeviceAgentComponentConfiguration.class)
public class RandomMustRunDeviceAgentComponent extends RandomMustRunDeviceAgent implements AgentConnectorService,
		TelemetryConnectorService, LoggingConnectorService, TimeConnectorService, SchedulerConnectorService {

	/*
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.simulation.testagents.RandomMustRunDeviceAgent";

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
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	void deactivate() {
		/* do nothing */
	}

}
