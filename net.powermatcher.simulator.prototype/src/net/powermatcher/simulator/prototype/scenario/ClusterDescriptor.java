package net.powermatcher.simulator.prototype.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.powermatcher.core.configurable.BaseConfiguration;

public class ClusterDescriptor {
	private NodeDescriptor root;
	private List<NodeDescriptor> nodeDescriptors = new ArrayList<NodeDescriptor>();

	public NodeDescriptor getRoot() {
		return root;
	}

	public void setRoot(NodeDescriptor root) {
		this.root = root;
	}

	public void addNodeDescriptor(NodeDescriptor nodeDescriptor) {
		nodeDescriptors.add(nodeDescriptor);
	}

	public List<NodeDescriptor> getNodeDescriptors() {
		return nodeDescriptors;
	}

	public static ClusterDescriptor getExample1() {
		ClusterDescriptor scenario = new ClusterDescriptor();

		AuctioneerNodeDescriptor auctioneer = new AuctioneerNodeDescriptor(
				"net.powermatcher.simulator.prototype.pmcore.Auctioneer");
		auctioneer.setConfiguration(createConfiguration("Auctioneer - 0"));
		scenario.setRoot(auctioneer);
		scenario.addNodeDescriptor(auctioneer);

		int concentratorCount = 10;
		int agentCount = 200;

		for (int i = 0; i < concentratorCount; i++) {
			ConcentratorNodeDescriptor concentrator = new ConcentratorNodeDescriptor(
					"net.powermatcher.simulator.prototype.pmcore.Concentrator");
			concentrator.setConfiguration(createConfiguration("Concentrator - " + i));
			scenario.addNodeDescriptor(concentrator);
			auctioneer.addChild(concentrator);

			for (int j = 0; j < agentCount / 2; j++) {
				createDeviceAgent(scenario, concentrator,
						"net.powermatcher.simulator.prototype.pmcore.VariableDeviceAgent", i + "." + j);
			}

			for (int j = 0; j < agentCount / 2; j++) {
				createDeviceAgent(scenario, concentrator,
						"net.powermatcher.simulator.prototype.pmcore.RandomMustRunDeviceAgent", i + "." + j
								+ agentCount);
			}
		}

		return scenario;
	}

	private static BaseConfiguration createConfiguration(String id) {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("id", id);
		return new BaseConfiguration(properties);
	}

	private static void createDeviceAgent(ClusterDescriptor scenario, ConcentratorNodeDescriptor concentrator,
			String agentComponentName, String idPostfix) {
		DeviceAgentNodeDescriptor deviceAgent = new DeviceAgentNodeDescriptor(agentComponentName, null, null);
		deviceAgent.setParent(concentrator);
		concentrator.addChild(deviceAgent);
		scenario.addNodeDescriptor(deviceAgent);
		deviceAgent.setConfiguration(createConfiguration("DeviceAgent - " + idPostfix));
	}
}
