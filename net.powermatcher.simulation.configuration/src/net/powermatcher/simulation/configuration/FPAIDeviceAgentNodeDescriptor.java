package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "fpai_device_agent")
public class FPAIDeviceAgentNodeDescriptor extends AgentNodeDescriptor<ResourceManagerNodeDescriptor> {
	@Override
	public boolean addChild(ResourceManagerNodeDescriptor child) {
		if (child instanceof ResourceManagerNodeDescriptor) {
			return super.addChild(child);
		}

		return false;
	}
}
