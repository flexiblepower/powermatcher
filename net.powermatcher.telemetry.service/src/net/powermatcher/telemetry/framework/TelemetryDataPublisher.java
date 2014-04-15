package net.powermatcher.telemetry.framework;


import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.telemetry.model.data.TelemetryData;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * @author IBM
 * @version 0.9.0
 */
public class TelemetryDataPublisher extends AbstractTelemetryDataPublisher {
	/**
	 * Define the telemetry data service (TelemetryDataService) field.
	 */
	private TelemetryService telemetryDataService;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * service and telemetry data service parameters.
	 * 
	 * @param configurationService
	 *            The configuration service (<code>ConfigurationService</code>)
	 *            parameter.
	 * @param telemetryDataService
	 *            The telemetry data service (<code>TelemetryService</code>)
	 *            parameter.
	 */
	public TelemetryDataPublisher(final ConfigurationService configurationService, final TelemetryService telemetryDataService) {
		super(configurationService);
		this.telemetryDataService = telemetryDataService;
	}

	/**
	 * Publish telemetry data object to the telemetry service associated with this publisher.
	 * 
	 * @param telemetryData The constructed telemetry data object.
	 */
	@Override
	protected void publishTelemetryData(TelemetryData telemetryData) {
		this.telemetryDataService.processTelemetryData(telemetryData);
	}

}
