package net.powermatcher.simulation.configuration;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;

public class XmlSerializer {

	private static JAXBContext constructContext() throws JAXBException {
		// TODO there is probably a better way to do this...
		return JAXBContext.newInstance(ScenarioDescriptor.class, ClusterDescriptor.class,
				AuctioneerNodeDescriptor.class, ConcentratorNodeDescriptor.class, DeviceAgentNodeDescriptor.class,
				FPAIDeviceAgentNodeDescriptor.class, ResourceManagerNodeDescriptor.class,
				ResourceDriverNodeDescriptor.class, NodeDescriptor.class, ObjectiveAgentNodeDescriptor.class,
				SimulationClockDescriptor.class, RealTimeSimulationClockDescriptor.class,
				AsFastAsPossibleSimulationClockDescriptor.class, DataSinkDescriptor.class,
				SingleFileCsvDataSinkDescriptor.class, PerAgentCsvDataSinkDescriptor.class, DataDescriptor.class,
				ConfigMarshallerEntryType.class, ConfigMarshallerType.class);
	}

	public static ScenarioDescriptor loadScenario(File inputFile) throws JAXBException {
		Unmarshaller m = constructContext().createUnmarshaller();
		ScenarioDescriptor scenario = (ScenarioDescriptor) m.unmarshal(inputFile);
		postProcess(scenario);
		scenario.setFile(inputFile);
		return scenario;
	}

	@SuppressWarnings("rawtypes")
	public static ConfigurationElement unmarshal(InputStream inputStream) throws JAXBException {
		Unmarshaller m = constructContext().createUnmarshaller();
		return (ConfigurationElement) m.unmarshal(inputStream);
	}

	private static void postProcess(ScenarioDescriptor scenario) {
		for (ClusterDescriptor cd : scenario.getChildren()) {
			AuctioneerNodeDescriptor auctioneer = cd.getRoot();
			postProcessNodeDescriptor(auctioneer, cd, scenario);
		}
	}

	@SuppressWarnings("unchecked")
	private static void postProcessNodeDescriptor(NodeDescriptor<?> parent, ClusterDescriptor descriptor,
			ScenarioDescriptor scenarioDescriptor) {
		if (parent.hasChildren()) {
			String clusterIdKey = IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY;
			String clusterId = descriptor.getClusterId();

			parent.changeConfigurationParameter(clusterIdKey, clusterId);
			for (NodeDescriptor<?> c : (List<NodeDescriptor<?>>) parent.getChildren()) {
				c.setParent(parent);
				c.changeConfigurationParameter(clusterIdKey, clusterId);
				postProcessNodeDescriptor(c, descriptor, scenarioDescriptor);
			}
		}
	}

	public static void printScenario(ScenarioDescriptor scenario) throws JAXBException {
		Marshaller m = constructContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(scenario, System.out);
	}

	public static void saveScenario(ScenarioDescriptor scenario, File outputFile) throws JAXBException {
		Marshaller m = constructContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(scenario, outputFile);
	}

	public static void marshal(ConfigurationElement<?> element, OutputStream stream) throws JAXBException {
		Marshaller m = constructContext().createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(element, stream);
	}
}
