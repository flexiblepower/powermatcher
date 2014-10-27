package net.powermatcher.simulation.engine.logging;

import net.powermatcher.core.agent.framework.log.Logable;
import net.powermatcher.simulation.engine.SimulationCycleListener;
import net.powermatcher.telemetry.service.TelemetryService;

public interface DataSink extends Logable, TelemetryService, SimulationCycleListener {
}
