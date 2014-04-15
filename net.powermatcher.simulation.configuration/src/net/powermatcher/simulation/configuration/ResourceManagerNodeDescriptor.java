package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "resource_manager")
public class ResourceManagerNodeDescriptor extends NodeDescriptor {
	@Override
	public boolean addChild(ConfigurationElement child) {
		if (child instanceof ResourceDriverNodeDescriptor) {
			return super.addChild(child);
		}

		return false;
	}
}
