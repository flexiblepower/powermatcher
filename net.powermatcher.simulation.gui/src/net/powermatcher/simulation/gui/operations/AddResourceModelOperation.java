package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceDriverNodeDescriptor;

public class AddResourceModelOperation extends AddAgentOperation {
	public AddResourceModelOperation(NodeDescriptor parent, String factoryId, HashMap<String, Object> configuration) {
		super("Add resource model operation", parent, factoryId, configuration);
	}

	@Override
	protected NodeDescriptor createDescriptor() {
		return new ResourceDriverNodeDescriptor();
	}
}
