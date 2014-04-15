package net.powermatcher.agent.template;


import java.util.Date;

import net.powermatcher.core.agent.template.ExampleAgent1;
import net.powermatcher.telemetry.framework.TelemetryDataPublisher;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;


/**
 * This class is an extension of <code>ExampleAgent1</code> that also publishes an update count as telemetry measurement data.<br>
 * The unmodified behavior that this class inherits from <code>ExampleAgent1</code> is that it publishes a static step-shaped bid 
 * for a configurable demand and price value.
 * 
 * This agent implements the <code>TelemetryConnectorService</code>, which is the connection point for a telemetry adapter.
 * The telemetry adapter will provide the telemetry service to publish telemetry events to this agent via
 * the connector.
 * 
 * @author IBM
 * @version 0.9.0
 */
public class ExampleAgent3 extends ExampleAgent1 implements TelemetryConnectorService {

	/**
	 * The update count that is published is a dimensionless measurement, and therefore the
	 * unit that is reported is empty.
	 */
	private static final String NO_UNIT = "";

	/**
	 * The telemetry data publisher publishes the telemetry events to the telemetry adapter.
	 * The publisher is a utility class that wraps the telemetry service interface that
	 * is provided by the adapter that connects to this agent.
	 */
	private TelemetryDataPublisher telemetryDataPublisher;

	/**
	 * The update count is incremented for each update event and published as telemetry measurement value. 
	 */
	private int updateCount;

	/**
	 * Bind the specified telemetry publisher.
	 * 
	 * @param telemetryPublisher
	 *            The telemetry publisher (<code>TelemetryService</code>)
	 *            to bind.
	 * @see #unbind(TelemetryService)
	 */
	@Override
	public void bind(TelemetryService telemetryPublisher) {
		this.telemetryDataPublisher = new TelemetryDataPublisher(getConfiguration(), telemetryPublisher);
	}

	/**
	 * Do the periodic bid update. This method is intended for updating the agents status
	 * and publish a new bid reflecting that status. It is periodically invoked
	 * by the framework at the configured update interval.
	 * 
	 * This method extends the behavior in the superclass by publishing and incrementing the
	 * updat count.
	 * 
	 * @see net.powermatcher.core.agent.template.ExampleAgent1#doBidUpdate()
	 */
	@Override
	protected void doBidUpdate() {
		/*
		 * Copy the field to a local variable for lock-free thread safety.
		 * This avoids the need for synchronization around references to this.exampleAdapter
		 */
		TelemetryDataPublisher telemetryDataPublisher = this.telemetryDataPublisher;

		/*
		 * If a telemetry adapter has connected to the agent, publish a status event for
		 * the update and publish the update count as a measurement. 
		 */
		if (telemetryDataPublisher != null) {
			logInfo("Publishing status update " + this.updateCount);
			Date now = new Date(getCurrentTimeMillis());
			/*
			 * Publish a status event.
			 * A status event has a valueName (the key), a string value and a timestamp.
			 */
			telemetryDataPublisher.publishStatusData("action", "updating", now);
			/*
			 * Publish a measurement event.
			 * A measurement event has a valueName (the key), a numeric value, a unit and a timestamp.
			 */
			telemetryDataPublisher.publishMeasurementData("updateCount", NO_UNIT, Float.valueOf(this.updateCount), null, now);
		}
		this.updateCount += 1;
		super.doBidUpdate();
	}

	/**
	 * Unbind the specified telemetry publisher.
	 * 
	 * @param telemetryPublisher
	 *            The telemetry publisher (<code>TelemetryService</code>)
	 *            to unbind.
	 *            
	 * @see #bind(TelemetryService)
	 */
	@Override
	public void unbind(TelemetryService telemetryPublisher) {
		this.telemetryDataPublisher = null;
	}

}
