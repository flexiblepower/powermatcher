package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "csv_data_sink")
public class SingleFileCsvDataSinkDescriptor extends DataSinkDescriptor {

	private String outputFile;

	@XmlElement(name = "output_file")
	public String getOutputFile() {
		return this.outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
}
