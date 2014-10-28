package net.powermatcher.simulation.logging.sink;

import java.util.List;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.simulation.engine.logging.DataSink;
import net.powermatcher.telemetry.model.data.TelemetryData;

public class Filter extends AbstractDataSink implements DataSink {

	private final DataSink sink;

	public Filter(DataSink dataSink, DataDescriptor... dataDescriptors) {
		super(dataDescriptors);
		this.sink = dataSink;
	}

	public Filter(DataSink dataSink, List<DataDescriptor> dataDescriptors) {
		super(dataDescriptors);
		this.sink = dataSink;
	}

	@Override
	protected void processFilteredBidData(DataDescriptor descriptor, BidLogInfo bid) {
		sink.handleBidLogInfo(bid);
	}

	@Override
	protected void processFilteredPriceData(DataDescriptor descriptor, PriceLogInfo price) {
		sink.handlePriceLogInfo(price);
	}

	@Override
	protected void processFilteredTelemetryData(DataDescriptor descriptor, TelemetryData telemetryData) {
		sink.processTelemetryData(telemetryData);
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
		sink.simulationCycleBegins(timestamp);
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
		sink.simulationCycleFinishes(timestamp);
	}

	@Override
	public void simulationFinished() {
		sink.simulationFinished();
	}

	@Override
	public void simulationStarts(long timestamp) {
		sink.simulationStarts(timestamp);
	}
}
