package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;

public class AddConcentratorOperation extends AddAgentOperation {
	public AddConcentratorOperation(NodeDescriptor parent, String factoryId, HashMap<String, Object> configuration) {
		super("Add concentrator operation", parent, factoryId, configuration);
	}

	@Override
	protected NodeDescriptor createDescriptor() {
		return new ConcentratorNodeDescriptor();
	}
}