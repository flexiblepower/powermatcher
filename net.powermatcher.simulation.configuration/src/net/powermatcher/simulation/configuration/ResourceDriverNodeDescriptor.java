package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "resource_driver")
public class ResourceDriverNodeDescriptor extends NodeDescriptor {
	@Override
	public boolean addChild(ConfigurationElement child) {
		return false;
	}
}
