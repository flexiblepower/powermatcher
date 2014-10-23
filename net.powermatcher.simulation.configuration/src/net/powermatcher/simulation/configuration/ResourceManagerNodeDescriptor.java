package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "resource_manager")
public class ResourceManagerNodeDescriptor extends NodeDescriptor<NodeDescriptor<?>> {
	@Override
	public boolean addChild(NodeDescriptor<?> child) {
		if (child instanceof ResourceDriverNodeDescriptor) {
			return super.addChild(child);
		}

		return false;
	}
}
