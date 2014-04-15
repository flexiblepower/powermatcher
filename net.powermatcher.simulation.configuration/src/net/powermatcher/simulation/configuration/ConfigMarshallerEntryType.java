package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class ConfigMarshallerEntryType {
	@XmlAttribute(name = "name")
	public String key;

	@XmlValue
	public String value;

	public ConfigMarshallerEntryType() {
	}

	public ConfigMarshallerEntryType(String key, String value) {
		this.key = key;
		this.value = value;
	}
}
