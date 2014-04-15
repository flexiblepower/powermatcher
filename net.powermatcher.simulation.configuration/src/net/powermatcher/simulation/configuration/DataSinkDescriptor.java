package net.powermatcher.simulation.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public abstract class DataSinkDescriptor {

	private final List<DataDescriptor> dataDescriptors = new ArrayList<DataDescriptor>();
	private String sinkId;

	public void addDataDescriptor(DataDescriptor dd) {
		this.dataDescriptors.add(dd);
	}

	@XmlElement(name = "data_descriptor")
	@XmlElementWrapper(name = "data")
	public List<DataDescriptor> getDataDescriptors() {
		return this.dataDescriptors;
	}

	@XmlAttribute(name = "sink_id")
	public String getSinkId() {
		return this.sinkId;
	}

	public void setSinkId(String sinkId) {
		this.sinkId = sinkId;
	}

}
