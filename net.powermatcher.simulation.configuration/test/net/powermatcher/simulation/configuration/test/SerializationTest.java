package net.powermatcher.simulation.configuration.test;

import java.util.Date;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.simulation.configuration.AsFastAsPossibleSimulationClockDescriptor;
import net.powermatcher.simulation.configuration.AuctioneerNodeDescriptor;
import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.SingleFileCsvDataSinkDescriptor;
import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.simulation.configuration.DataDescriptor.DataType;
import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.MarketBasisDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.configuration.XmlSerializer;

import org.junit.Test;

public class SerializationTest {

	private static HashMap<String, Object> createConfiguration(ClusterDescriptor clusterDescriptor, String nodeId) {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, clusterDescriptor.getClusterId());
		properties.put(IdentifiableObjectConfiguration.ID_PROPERTY, nodeId);
		return properties;
	}

	private static void createDeviceAgent(ClusterDescriptor clusterDescriptor, ConcentratorNodeDescriptor concentrator,
			String agentComponentName, String idPostfix) {
		DeviceAgentNodeDescriptor deviceAgent = new DeviceAgentNodeDescriptor();
		deviceAgent.setFactoryPid(agentComponentName);
		deviceAgent.setParent(concentrator);
		concentrator.addChild(deviceAgent);
		deviceAgent.setConfiguration(createConfiguration(clusterDescriptor, "DeviceAgent-" + idPostfix));
	}

	private static ScenarioDescriptor getExample1() {
		ScenarioDescriptor scenario = new ScenarioDescriptor();
		ClusterDescriptor cluster = new ClusterDescriptor();
		MarketBasisDescriptor marketBasis = new MarketBasisDescriptor();
		marketBasis.setCommodity("electricity");
		marketBasis.setCurrency("EUR");
		marketBasis.setPriceSteps(50);
		marketBasis.setMinimumPrice(0);
		marketBasis.setMaximumPrice(50);
		scenario.setMarketBasisDescriptor(marketBasis);

		// DataSink
		SingleFileCsvDataSinkDescriptor csv = new SingleFileCsvDataSinkDescriptor();
		csv.setOutputFile("c:\\Data\\powermatcher.csv");
		csv.setSinkId("csv");
		DataDescriptor dataDescriptor = new DataDescriptor();
		dataDescriptor.setAgentId("Auctioneer-0");
		dataDescriptor.setClusterId("cluster1");
		dataDescriptor.setType(DataType.PRICE);
		csv.addDataDescriptor(dataDescriptor);
		scenario.addDataSink(csv);

		AuctioneerNodeDescriptor auctioneer = new AuctioneerNodeDescriptor();
		auctioneer.setFactoryPid("net.powermatcher.core.agent.auctioneer.Auctioneer");
		auctioneer.setConfiguration(createConfiguration(cluster, "Auctioneer-0"));
		cluster.setRoot(auctioneer);

		int concentratorCount = 2;
		int agentCount = 4;

		for (int i = 0; i < concentratorCount; i++) {
			ConcentratorNodeDescriptor concentrator = new ConcentratorNodeDescriptor();
			concentrator.setFactoryPid("net.powermatcher.core.agent.concentrator.Concentrator");
			concentrator.setConfiguration(createConfiguration(cluster, "Concentrator-" + i));
			auctioneer.addChild(concentrator);

			for (int j = 0; j < agentCount / 2; j++) {
				createDeviceAgent(cluster, concentrator, "net.powermatcher.simulation.testagents.VariableDeviceAgent", i + "."
						+ j);
			}

			for (int j = 0; j < agentCount / 2; j++) {
				createDeviceAgent(cluster, concentrator, "net.powermatcher.simulation.testagents.RandomMustRunDeviceAgent", i
						+ "." + j + agentCount);
			}
		}

		scenario.addChild(cluster);
		AsFastAsPossibleSimulationClockDescriptor clock = new AsFastAsPossibleSimulationClockDescriptor();
		clock.setStartTime(new Date(0));
		clock.setTimestepIntervalMillis(30000);
		scenario.setSimulationClockDescriptor(clock);
		return scenario;
	}

	@Test
	public void serialization() throws JAXBException {
		// TODO actually test something
		ScenarioDescriptor s = getExample1();
		XmlSerializer.printScenario(s);
	}

}
