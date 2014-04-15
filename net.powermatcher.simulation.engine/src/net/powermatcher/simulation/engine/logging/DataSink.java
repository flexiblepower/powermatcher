package net.powermatcher.simulation.engine.logging;

import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.simulation.engine.SimulationCycleListener;
import net.powermatcher.telemetry.service.TelemetryService;

public interface DataSink extends LogListenerService, TelemetryService, SimulationCycleListener {
}
