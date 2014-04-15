package net.powermatcher.simulation.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class ConfigMarshallerType {
	private final List<ConfigMarshallerEntryType> entry = new ArrayList<ConfigMarshallerEntryType>();

	@XmlElement(name = "property")
	public List<ConfigMarshallerEntryType> getEntry() {
		return this.entry;
	}
}
