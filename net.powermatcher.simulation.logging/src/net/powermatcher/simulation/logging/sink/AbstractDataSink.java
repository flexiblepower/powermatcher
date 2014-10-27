package net.powermatcher.simulation.logging.sink;

import java.util.Arrays;
import java.util.List;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.simulation.engine.logging.DataSink;
import net.powermatcher.telemetry.model.data.TelemetryData;

public abstract class AbstractDataSink implements DataSink {

	protected final List<DataDescriptor> dataDescriptors;

	public AbstractDataSink(DataDescriptor... dataDescriptors) {
		this.dataDescriptors = Arrays.asList(dataDescriptors);
	}

	public AbstractDataSink(List<DataDescriptor> dataDescriptors) {
		this.dataDescriptors = dataDescriptors;
	}

	@Override
	public void logBidLogInfo(BidLogInfo bid) {
		for (DataDescriptor descriptor : this.dataDescriptors) {
			if (descriptor.describes(bid)) {
				this.processFilteredBidData(descriptor, bid);
			}
		}
	}

	@Override
	public void logPriceLogInfo(PriceLogInfo price) {
		for (DataDescriptor descriptor : this.dataDescriptors) {
			if (descriptor.describes(price)) {
				this.processFilteredPriceData(descriptor, price);
			}
		}
	}

	protected abstract void processFilteredBidData(DataDescriptor descriptor, BidLogInfo bid);

	protected abstract void processFilteredPriceData(DataDescriptor descriptor, PriceLogInfo price);

	protected abstract void processFilteredTelemetryData(DataDescriptor descriptor, TelemetryData telemetryData);

	@Override
	public void processTelemetryData(TelemetryData telemetryData) {
		for (DataDescriptor descriptor : this.dataDescriptors) {
			if (descriptor.describes(telemetryData)) {
				this.processFilteredTelemetryData(descriptor, telemetryData);
			}
		}
	}

}
