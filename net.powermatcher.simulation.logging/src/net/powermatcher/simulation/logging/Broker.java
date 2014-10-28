package net.powermatcher.simulation.logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.engine.logging.DataSink;
import net.powermatcher.telemetry.model.data.TelemetryData;

public class Broker implements DataSink {
	private final List<DataSink> dataSinks = new CopyOnWriteArrayList<DataSink>();

	public void addDataSink(DataSink sink) {
		this.dataSinks.add(sink);
	}

	@Override
	public void handleBidLogInfo(BidLogInfo bidLogInfo) {
		for (DataSink sink : this.dataSinks) {
			sink.handleBidLogInfo(bidLogInfo);
		}
	}

	@Override
	public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
		for (DataSink sink : this.dataSinks) {
			sink.handlePriceLogInfo(priceLogInfo);
		}
	}

	@Override
	public void processTelemetryData(TelemetryData data) {
		for (DataSink sink : this.dataSinks) {
			sink.processTelemetryData(data);
		}
	}

	public boolean removeDataSink(DataSink sink) {
		return this.removeDataSink(sink);
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
		for (DataSink sink : this.dataSinks) {
			sink.simulationCycleBegins(timestamp);
		}
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
		for (DataSink sink : this.dataSinks) {
			sink.simulationCycleFinishes(timestamp);
		}
	}

	@Override
	public void simulationFinished() {
		for (DataSink sink : this.dataSinks) {
			sink.simulationFinished();
		}
	}

	@Override
	public void simulationStarts(long timestamp) {
		for (DataSink sink : this.dataSinks) {
			sink.simulationStarts(timestamp);
		}
	}
}
