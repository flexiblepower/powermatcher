package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "per_agent_csv_data_sink")
public class PerAgentCsvDataSinkDescriptor extends DataSinkDescriptor {

	private String outputDirectory;

	@XmlElement(name = "output_directory")
	public String getOutputDirectory() {
		return this.outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}
