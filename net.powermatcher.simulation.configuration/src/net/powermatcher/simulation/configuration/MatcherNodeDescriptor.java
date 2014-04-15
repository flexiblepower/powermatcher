package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "auctioneer")
public class MatcherNodeDescriptor extends NodeDescriptor<NodeDescriptor<?>> {
	@Override
	public boolean addChild(NodeDescriptor<?> child) {
		if (child instanceof AgentNodeDescriptor || child instanceof ConcentratorNodeDescriptor) {
			return super.addChild(child);
		}

		return false;
	}
}
