package net.powermatcher.simulation.testagents;

import java.util.Date;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.telemetry.framework.TelemetryDataPublisher;
import net.powermatcher.telemetry.service.TelemetryConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;

public class RandomMustRunDeviceAgent extends Agent implements TelemetryConnectorService {

	private double demand = Math.random() * 100;
	private TelemetryDataPublisher telemetryDataPublisher = null;

	public RandomMustRunDeviceAgent() {
		super();
	}

	public RandomMustRunDeviceAgent(Configurable configuration) {
		super(configuration);
	}

	@Override
	public void bind(TelemetryService telemetryPublisher) {
		this.telemetryDataPublisher = new TelemetryDataPublisher(getConfiguration(), telemetryPublisher);
	}

	@Override
	protected void doBidUpdate() {
		BidInfo bid = generateBid();
		logInfo("Generated bid: " + bid);
		this.publishBidUpdate(bid);
	}

	protected BidInfo generateBid() {

		this.demand += Math.random() * 200 - 100;

		if (this.demand > 1000) {
			this.demand = 1000 - Math.random() * 200;
		} else if (this.demand < 0) {
			this.demand = Math.random() * 200;
		}

		if (this.telemetryDataPublisher != null) {
			Date now = new Date(this.getTimeSource().currentTimeMillis());
			this.telemetryDataPublisher.publishStatusData("demand", String.valueOf(this.demand), now);
		} else {
			logWarning("No telemetry connected");
		}
		return new BidInfo(this.getCurrentMarketBasis(), new PricePoint(0, this.demand));
	}

	@Override
	public void unbind(TelemetryService telemetryPublisher) {
		this.telemetryDataPublisher = null;
	}

}
