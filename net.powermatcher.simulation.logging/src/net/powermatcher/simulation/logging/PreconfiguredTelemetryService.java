package net.powermatcher.simulation.logging;

import net.powermatcher.core.object.ConnectableObject;
import net.powermatcher.telemetry.model.data.TelemetryData;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;

public class PreconfiguredTelemetryService extends ConnectableObject implements TelemetryService, TelemetryConnectorService {

	// TODO allow multiple publishers?
	private TelemetryService telemetryPublisher;

	@Override
	public void bind(TelemetryService telemetryPublisher) {
		this.telemetryPublisher = telemetryPublisher;
	}

	@Override
	public void processTelemetryData(TelemetryData data) {
		if (this.telemetryPublisher != null) {
			this.telemetryPublisher.processTelemetryData(data);
		}
	}

	@Override
	public void unbind(TelemetryService telemetryPublisher) {
		this.telemetryPublisher = null;
	}

}
