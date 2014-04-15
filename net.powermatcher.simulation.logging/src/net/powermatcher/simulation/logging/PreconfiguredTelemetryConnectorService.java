package net.powermatcher.simulation.logging;

import net.powermatcher.telemetry.framework.TelemetryDataPublisher;

public interface PreconfiguredTelemetryConnectorService {

	public void bind(final PreconfiguredTelemetryService telemetryPublisher);

	public void unbind(final PreconfiguredTelemetryService telemetryPublisher);

}