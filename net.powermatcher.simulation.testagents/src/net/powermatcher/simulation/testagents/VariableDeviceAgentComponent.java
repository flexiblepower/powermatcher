package net.powermatcher.simulation.testagents;

import java.util.Map;

import net.powermatcher.core.agent.framework.log.LogPublishable;
import net.powermatcher.core.agent.framework.service.ChildConnectable;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.scheduler.service.SchedulerConnectorService;
import net.powermatcher.core.scheduler.service.TimeConnectorService;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;

@Component(name = VariableDeviceAgentComponent.COMPONENT_NAME, designateFactory = VariableDeviceAgentComponentConfiguration.class)
public class VariableDeviceAgentComponent extends VariableDeviceAgent implements ChildConnectable,
		LogPublishable, TimeConnectorService, SchedulerConnectorService {

	public final static String COMPONENT_NAME = "net.powermatcher.simulation.testagents.VariableDeviceAgent";

	@Activate
	void activate(final Map<String, Object> properties) {
		setConfiguration(new BaseConfiguration(properties));
	}
}
