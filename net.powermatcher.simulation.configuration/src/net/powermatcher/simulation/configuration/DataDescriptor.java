package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.telemetry.model.data.Data;
import net.powermatcher.telemetry.model.data.MeasurementData;
import net.powermatcher.telemetry.model.data.StatusData;
import net.powermatcher.telemetry.model.data.TelemetryData;

@XmlRootElement(name = "data_descriptor")
public class DataDescriptor {

	public enum DataType {
		BID, PRICE, TELEMETRY_ALERT, TELEMETRY_MEASUREMENT, TELEMETRY_STATUS, TELEMTRY_CONTROL,
	}

	private String agentId;
	private String clusterId;
	private String key; // TODO maybe make a special TelemetryDataDescriptor?

	private DataType type;

	public DataDescriptor() {
		// TODO Auto-generated constructor stub
	}

	public DataDescriptor(String agentId, String clusterId, DataType type) {
		this.agentId = agentId;
		this.clusterId = clusterId;
		this.type = type;
	}

	public DataDescriptor(String agentId, String clusterId, DataType type, String key) {
		this.agentId = agentId;
		this.clusterId = clusterId;
		this.key = key;
		this.type = type;
	}

	public boolean describes(BidLogInfo bid) {
		if (!this.type.equals(DataType.BID)) {
			return false;
		}
		return bid.getAgentId().equals(this.agentId) && bid.getClusterId().equals(this.clusterId);
	}

	public boolean describes(PriceLogInfo price) {
		if (!this.type.equals(DataType.PRICE)) {
			return false;
		}
		return price.getAgentId().equals(this.agentId) && price.getClusterId().equals(this.clusterId);
	}

	public boolean describes(TelemetryData data) {
		if (!(data.getAgentId().equals(this.agentId) && data.getClusterId().equals(this.clusterId))) {
			return false;
		}

		switch (this.type) {
		case TELEMETRY_MEASUREMENT:
			for (MeasurementData measurementData : data.getMeasurementData()) {
				if (this.key.equals(measurementData)) {
					return true;
				}
			}
			return false;
		case TELEMETRY_STATUS:
			for (StatusData statusData : data.getStatusData()) {
				if (this.key.equals(statusData.getValueName())) {
					return true;
				}
			}
			return false;
		default:
			return false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataDescriptor other = (DataDescriptor) obj;
		if (this.agentId == null) {
			if (other.agentId != null)
				return false;
		} else if (!this.agentId.equals(other.agentId))
			return false;
		if (this.clusterId == null) {
			if (other.clusterId != null)
				return false;
		} else if (!this.clusterId.equals(other.clusterId))
			return false;
		if (this.key == null) {
			if (other.key != null)
				return false;
		} else if (!this.key.equals(other.key))
			return false;
		if (this.type != other.type)
			return false;
		return true;
	}

	@XmlAttribute(name = "agent_id")
	public String getAgentId() {
		return this.agentId;
	}

	@XmlAttribute(name = "cluster_id")
	public String getClusterId() {
		return this.clusterId;
	}

	public Data getDescribedTelemetryData(TelemetryData data) {
		switch (this.type) {
		case BID:
		case PRICE:
			return null;
		case TELEMETRY_STATUS:
			for (StatusData statusData : data.getStatusData()) {
				if (this.key.equals(statusData.getValueName())) {
					return statusData;
				}
			}
			return null;
		case TELEMETRY_MEASUREMENT:
			for (MeasurementData measurementData : data.getMeasurementData()) {
				if (this.key.equals(measurementData.getValueName())) {
					return measurementData;
				}
			}
			return null;
		}
		return null;
	}

	@XmlAttribute(name = "key")
	public String getKey() {
		return key;
	}

	@XmlAttribute(name = "type")
	public DataType getType() {
		return this.type;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.clusterId);
		sb.append(" - ");
		sb.append(this.agentId);
		sb.append(": ");
		switch (this.type) {
		case BID:
			sb.append("Bid");
			break;
		case PRICE:
			sb.append("Price");
			break;
		case TELEMETRY_MEASUREMENT:
			sb.append(this.key + " (measurement)");
		case TELEMETRY_STATUS:
			sb.append(this.key + " (status)");
		}
		return sb.toString();
	}
}