package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "device_agent")
public class DeviceAgentNodeDescriptor extends AgentNodeDescriptor<NodeDescriptor<?>> {
	@Override
	public boolean addChild(NodeDescriptor<?> child) {
		return false;
	}
}
