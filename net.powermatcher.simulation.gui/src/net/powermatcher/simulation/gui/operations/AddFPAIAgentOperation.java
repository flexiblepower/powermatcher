package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.simulation.configuration.FPAIDeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;

public class AddFPAIAgentOperation extends AddAgentOperation {
	public AddFPAIAgentOperation(NodeDescriptor parent, String factoryId, HashMap<String, Object> configuration) {
		super("Add fpai agent operation", parent, factoryId, configuration);
	}

	@Override
	protected NodeDescriptor createDescriptor() {
		return new FPAIDeviceAgentNodeDescriptor();
	}
}
