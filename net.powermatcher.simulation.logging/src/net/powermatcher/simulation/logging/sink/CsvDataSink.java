package net.powermatcher.simulation.logging.sink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.telemetry.model.data.MeasurementData;
import net.powermatcher.telemetry.model.data.StatusData;
import net.powermatcher.telemetry.model.data.TelemetryData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvDataSink extends AbstractDataSink {

	private static final String DELIMITER = ";";

	private static final Logger logger = LoggerFactory.getLogger(CsvDataSink.class);

	protected static String format(DataDescriptor dataDescriptor, Object o) {
		// TODO something with locale?
		if (o == null) {
			return null;
		} else if (o instanceof PriceLogInfo) {
			PriceLogInfo price = (PriceLogInfo) o;
			return String.valueOf(price.getCurrentPrice());
		} else if (o instanceof BidLogInfo) {
			StringBuilder b = new StringBuilder();
			double demand[] = ((BidLogInfo) o).getBidInfo().getDemand();
			if (demand != null) {
				b.append('{');
				for (int i = 0; i < demand.length; i++) {
					if (i > 0) {
						b.append(',');
					}
					b.append(MarketBasis.DEMAND_FORMAT.format(demand[i]));
				}
				b.append('}');
			}
			return b.toString();
		} else if (o instanceof StatusData) {
			StatusData d = (StatusData) o;
			return d.getSingleValues()[0].getValue(); // TODO
		} else if (o instanceof MeasurementData) {
			MeasurementData d = (MeasurementData) o;
			return String.valueOf(d.getSingleValues()[0].getValue()); // TODO
		} else {
			return o.toString();
		}
	}

	protected static void quotifyAndAppend(String input, StringBuilder sb) {
		if (input == null) {
			sb.append("null");
		} else if (input.contains("\"") || input.contains(DELIMITER)) {
			sb.append('"').append(input.replace("\"", "\"\"")).append("'");
		} else {
			sb.append(input);
		}
	}

	private long currentTimestamp;
	private final File file;

	private final AtomicBoolean shouldWriteLine = new AtomicBoolean(false);

	private final Map<Long, Map<DataDescriptor, Object>> values = new ConcurrentHashMap<Long, Map<DataDescriptor, Object>>();
	private BufferedWriter writer;

	public CsvDataSink(List<DataDescriptor> dataDescriptors, File file) {
		super(dataDescriptors);
		this.file = file;
	}

	@Override
	protected void processFilteredBidData(DataDescriptor descriptor, BidLogInfo bid) {
		this.writeLine();
		this.values.get(this.currentTimestamp).put(descriptor, bid);
	}

	@Override
	protected void processFilteredPriceData(DataDescriptor descriptor, PriceLogInfo price) {
		this.writeLine();
		this.values.get(this.currentTimestamp).put(descriptor, price);
	}

	@Override
	protected void processFilteredTelemetryData(DataDescriptor descriptor, TelemetryData telemetryData) {
		this.writeLine();
		this.values.get(this.currentTimestamp).put(descriptor, descriptor.getDescribedTelemetryData(telemetryData));
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
		this.currentTimestamp = timestamp;
		this.values.put(timestamp, new ConcurrentHashMap<DataDescriptor, Object>());
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
		// indicate that the next agent that writes input to this file should
		// first write all the data from the previous cycle to the CSV file
		this.shouldWriteLine.set(true);
	}

	@Override
	public void simulationFinished() {
		// write last line if necessary
		this.writeLine();
		try {
			this.writer.close();
		} catch (IOException e) {
			logger.error("Could not close CSV file", e);
		}
	}

	@Override
	public void simulationStarts(long timestamp) {
		try {
			this.writer = new BufferedWriter(new FileWriter(this.file));
			this.writeHeader();
		} catch (IOException e) {
			logger.error("Could not create CSV file", e);
		}
	}

	protected void writeHeader() {
		StringBuilder sb = new StringBuilder();
		Iterator<DataDescriptor> it = this.dataDescriptors.iterator();
		quotifyAndAppend("Time", sb);
		if (it.hasNext()) {
			sb.append(DELIMITER);
		}
		while (it.hasNext()) {
			quotifyAndAppend(it.next().toString(), sb);
			if (it.hasNext()) {
				sb.append(DELIMITER);
			}
		}
		try {
			this.writer.write(sb.toString());
			this.writer.newLine();
		} catch (IOException e) {
			logger.error("Could not write to CSV file", e);
		}
	}

	protected void writeLine() {
		if (this.shouldWriteLine.getAndSet(false)) {
			for (Long timestamp : this.values.keySet()) {
				if (timestamp < this.currentTimestamp) {
					StringBuilder sb = new StringBuilder();
					Iterator<DataDescriptor> it = this.dataDescriptors.iterator();
					// Write timestamp
					quotifyAndAppend((new Date(timestamp)).toString(), sb);
					if (it.hasNext()) {
						sb.append(DELIMITER);
					}
					// write values
					while (it.hasNext()) {
						DataDescriptor next = it.next();
						Object o = this.values.get(timestamp).get(next);
						quotifyAndAppend(CsvDataSink.format(next, o), sb);
						if (it.hasNext()) {
							sb.append(DELIMITER);
						}
					}
					try {
						this.writer.write(sb.toString());
						this.writer.newLine();
					} catch (IOException e) {
						logger.error("Could not write to CSV file", e);
					}

					this.values.remove(timestamp);
				}
			}
		}
	}

}
