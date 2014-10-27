package net.powermatcher.simulation.engine.dependencyengine;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.scheduler.service.TimeServicable;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.engine.ComponentManager;
import net.powermatcher.simulation.logging.Broker;
import net.powermatcher.telemetry.service.TelemetryConnectorService;

public abstract class AgentComposer<Type extends Agent> extends Composer<Type> {
	public AgentComposer(NodeDescriptor nodeDescriptor, long startTime) {
		super(nodeDescriptor, startTime);
	}

	public void initializeNode(ComponentManager componentManager, TimeServicable timeService, MarketBasis marketBasis, Broker broker) {
		this.node.setConfiguration(new BaseConfiguration(getNodeDescriptor().getConfiguration()));
		this.node.updateMarketBasis(marketBasis);
		
		this.node.bind(timeService);
		this.node.bind(this.getScheduledExecutorService());
		
		if (this.node instanceof TelemetryConnectorService) {
			((TelemetryConnectorService) this.node).bind(broker);
		}
		
		this.node.bind(broker);
	}
}
