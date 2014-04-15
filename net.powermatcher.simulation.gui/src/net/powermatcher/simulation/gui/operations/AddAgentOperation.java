package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.ComponentCountManager;
import net.powermatcher.simulation.gui.GUIUtils;

public class AddAgentOperation extends AddNodeOperation {
	public AddAgentOperation(NodeDescriptor parent, String factoryId, HashMap<String, Object> configuration) {
		this("Add agent operation", parent, factoryId, configuration);
	}

	public AddAgentOperation(String title, NodeDescriptor parent, String factoryId,
			HashMap<String, Object> configuration) {
		super(title, parent, factoryId, configuration);
	}

	@Override
	protected NodeDescriptor createNode(NodeDescriptor parent, String factoryPid) {
		NodeDescriptor node = super.createNode(parent, factoryPid);

		HashMap<String, Object> configuration = node.getConfiguration();
		if (configuration == null) {
			configuration = new HashMap<String, Object>();
			node.setConfiguration(configuration);
		}

		configuration.put(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, parent.getClusterId());
		configuration.put(IdentifiableObjectConfiguration.ID_PROPERTY, getId(node));

		return node;
	}

	protected String getId(NodeDescriptor node) {
		return GUIUtils.getInstance().getDisplayableName(node.getFactoryPid()) + "-"
				+ String.valueOf(ComponentCountManager.getInstance().getNextCount(node.getClass()));
	}

	@Override
	protected NodeDescriptor createDescriptor() {
		return new DeviceAgentNodeDescriptor();
	}

}
