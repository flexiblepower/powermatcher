package net.powermatcher.simulation.telemetry.metadata;

import org.osgi.framework.Bundle;

public interface TelemetryMetaDataService {
	public static final String DOCUMENTS_LOCATION = "META-INF/telemetry-metatype";

	TelemetryMetaData getTelemetryMetaData(Bundle bundle);
}
