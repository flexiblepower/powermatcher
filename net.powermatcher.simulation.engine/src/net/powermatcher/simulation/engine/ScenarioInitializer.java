package net.powermatcher.simulation.engine;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.engine.logging.DataSink;

public interface ScenarioInitializer {
	DataSink getBroker();

	SimulationControl initializeScenario(ScenarioDescriptor scenario, ComponentManager componentManager)
			throws ComponentCreationException;
}
