package net.powermatcher.simulator.prototype.scenario;

import java.util.ArrayList;
import java.util.List;

import net.powermatcher.core.configurable.BaseConfiguration;

public class NodeDescriptor {
	private String agentComponentName;
	private BaseConfiguration configuration;

	private NodeDescriptor parent;
	private List<NodeDescriptor> children = new ArrayList<NodeDescriptor>();

	public NodeDescriptor(String agentComponentName) {
		this.agentComponentName = agentComponentName;
	}

	public String getAgentComponentName() {
		return agentComponentName;
	}

	public BaseConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(BaseConfiguration configuration) {
		this.configuration = configuration;
	}

	public NodeDescriptor getParent() {
		return parent;
	}

	public void setParent(NodeDescriptor parent) {
		this.parent = parent;
	}

	public List<NodeDescriptor> getChildren() {
		return children;
	}

	public void addChild(NodeDescriptor nodeDescriptor) {
		this.children.add(nodeDescriptor);
	}
}
