package net.powermatcher.simulation.configuration;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "simulated_time")
@XmlType(propOrder = { "startTime", "endTime", "timestepIntervalMillis" })
public class AsFastAsPossibleSimulationClockDescriptor extends SimulationClockDescriptor {

	private Date endTime = null;
	private Date startTime = null;
	private long timestepIntervalMillis;

	@XmlElement(name = "end")
	public Date getEndTime() {
		return this.endTime;
	}

	@XmlElement(name = "start")
	public Date getStartTime() {
		return this.startTime;
	}

	@XmlElement(name = "timestep_interval_millis")
	public long getTimestepIntervalMillis() {
		return this.timestepIntervalMillis;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setTimestepIntervalMillis(long timestepIntervalMillis) {
		this.timestepIntervalMillis = timestepIntervalMillis;
	}
}
