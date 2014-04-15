package net.powermatcher.simulation.logging;

import net.powermatcher.telemetry.model.data.TelemetryData;
import net.powermatcher.telemetry.service.TelemetryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For testing
 */
public class LoggerTelemetryDataPublisher implements TelemetryService {

	private static Logger logger = LoggerFactory.getLogger(LoggerTelemetryDataPublisher.class);

	@Override
	public void processTelemetryData(TelemetryData data) {
		logger.info("Received telemetry data from " + data.getAgentId() + ": "
				+ data.getStatusData()[0].getSingleValues()[0].getValue());
	}
}
