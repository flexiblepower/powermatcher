package net.powermatcher.simulation.logging.sink;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.simulation.engine.logging.DataSink;
import net.powermatcher.telemetry.model.data.TelemetryData;

public class PerAgentCsvDataSink extends AbstractDataSink {

	private static class AgentMap<V> {
		private final HashMap<String, HashMap<String, V>> values = new HashMap<String, HashMap<String, V>>();

		public boolean containsKeys(String cluster, String agent) {
			return this.values.containsKey(cluster) && this.values.get(cluster).containsKey(agent);
		}

		public V get(String cluster, String agent) {
			if (!this.values.containsKey(cluster)) {
				return null;
			}
			return this.values.get(cluster).get(agent);
		}

		public List<V> getAllAsList() {
			ArrayList<V> result = new ArrayList<V>();
			for (HashMap<String, V> maps : this.values.values()) {
				result.addAll(maps.values());
			}
			return result;
		}

		public void put(String cluster, String agent, V value) {
			if (!this.values.containsKey(cluster)) {
				this.values.put(cluster, new HashMap<String, V>());
			}
			this.values.get(cluster).put(agent, value);
		}
	}

	private final AgentMap<CsvDataSink> dataSinks = new AgentMap<CsvDataSink>();
	private final File directory;

	public PerAgentCsvDataSink(List<DataDescriptor> dataDescriptors, File directory) {
		super(dataDescriptors);
		if (directory == null || !directory.isDirectory()) {
			throw new IllegalArgumentException("directory must be a directory");
		}
		this.directory = directory;
		for (DataDescriptor dataDescriptor : dataDescriptors) {
			if (!this.dataSinks.containsKeys(dataDescriptor.getClusterId(), dataDescriptor.getAgentId())) {
				List<DataDescriptor> descriptorsForAgent = filter(dataDescriptors, dataDescriptor.getClusterId(),
						dataDescriptor.getAgentId());
				File fileForAgent = new File(this.directory.getAbsolutePath() + File.separator + dataDescriptor.getClusterId()
						+ "-" + dataDescriptor.getAgentId() + ".csv");
				CsvDataSink csvDataSinkForAgent = new CsvDataSink(descriptorsForAgent, fileForAgent);
				this.dataSinks.put(dataDescriptor.getClusterId(), dataDescriptor.getAgentId(), csvDataSinkForAgent);
			}
		}
	}

	private List<DataDescriptor> filter(List<DataDescriptor> input, String clusterId, String agentId) {
		ArrayList<DataDescriptor> result = new ArrayList<DataDescriptor>();
		for (DataDescriptor d : input) {
			if (agentId.equals(d.getAgentId()) && clusterId.equals(d.getClusterId())) {
				result.add(d);
			}
		}
		return result;
	}

	@Override
	protected void processFilteredBidData(DataDescriptor descriptor, BidLogInfo bid) {
		this.dataSinks.get(descriptor.getClusterId(), descriptor.getAgentId()).processFilteredBidData(descriptor, bid);
	}

	@Override
	protected void processFilteredPriceData(DataDescriptor descriptor, PriceLogInfo price) {
		this.dataSinks.get(descriptor.getClusterId(), descriptor.getAgentId()).processFilteredPriceData(descriptor, price);
	}

	@Override
	protected void processFilteredTelemetryData(DataDescriptor descriptor, TelemetryData telemetryData) {
		this.dataSinks.get(descriptor.getClusterId(), descriptor.getAgentId()).processFilteredTelemetryData(descriptor,
				telemetryData);
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
		for (DataSink sink : this.dataSinks.getAllAsList()) {
			sink.simulationCycleBegins(timestamp);
		}
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
		for (DataSink sink : this.dataSinks.getAllAsList()) {
			sink.simulationCycleFinishes(timestamp);
		}
	}

	@Override
	public void simulationFinished() {
		for (DataSink sink : this.dataSinks.getAllAsList()) {
			sink.simulationFinished();
		}
	}

	@Override
	public void simulationStarts(long timestamp) {
		for (DataSink sink : this.dataSinks.getAllAsList()) {
			sink.simulationStarts(timestamp);
		}
	}

}
