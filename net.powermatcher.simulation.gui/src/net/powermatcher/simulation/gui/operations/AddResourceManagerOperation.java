package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceManagerNodeDescriptor;

public class AddResourceManagerOperation extends AddAgentOperation {
	public AddResourceManagerOperation(NodeDescriptor parent, String factoryId, HashMap<String, Object> configuration) {
		super("Add resource manager operation", parent, factoryId, configuration);
	}

	@Override
	protected NodeDescriptor createDescriptor() {
		return new ResourceManagerNodeDescriptor();
	}
}
