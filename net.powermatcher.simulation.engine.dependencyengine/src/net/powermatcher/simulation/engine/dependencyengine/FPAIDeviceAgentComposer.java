package net.powermatcher.simulation.engine.dependencyengine;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.scheduler.service.TimeService;
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceDriverNodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceManagerNodeDescriptor;
import net.powermatcher.simulation.engine.ComponentCreationException;
import net.powermatcher.simulation.engine.ComponentManager;
import net.powermatcher.simulation.logging.Broker;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.Reference;
import org.apache.felix.scr.ScrService;
import org.flexiblepower.ral.ResourceDriver;
import org.flexiblepower.ral.ResourceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentConstants;

public class FPAIDeviceAgentComposer extends AgentComposer<Agent> {
	private Activity bidUpdateActivity;
	private Activity modelUpdateActivity;

	private ResourceDriver<?, ?> resourceDriver;

	private ResourceManager resourceManager;

	public FPAIDeviceAgentComposer(NodeDescriptor nodeDescriptor, long startTime) {
		super(nodeDescriptor, startTime);
	}

	@Override
	public void attachIncommingLink(Link link, Class<?> iface) {
		if (AgentService.class.equals(iface)) {
			// no activity dependnet on this link
		} else {
			throw new IllegalArgumentException("unsupported interface");
		}
	}

	@Override
	public void attachOutgoingLink(Link link, Class<?> iface) {
		if (MatcherService.class.equals(iface)) {
			link.setDependentOn(this.bidUpdateActivity);
		} else {
			throw new IllegalArgumentException("unsupported interface");
		}
	}

	@Override
	public Agent createNode(ComponentManager componentManager) throws ComponentCreationException {
		Agent agent = super.createNode(componentManager);

		this.bidUpdateActivity = new Activity("bid-update " + agent.getName());
		this.modelUpdateActivity = new Activity("model-update " + agent.getName());

		Link link = new Link();
		this.bidUpdateActivity.setDependentOn(link);
		link.setDependentOn(this.modelUpdateActivity);

		this.resourceManager = createResourceManager(componentManager);
		this.resourceDriver = createResourceDriver(componentManager);

		return agent;
	}

	private ResourceManager createResourceManager(ComponentManager componentManager) throws ComponentCreationException {
		NodeDescriptor managerNodeDescriptor = getManagerNodeDescriptor();

		// add appliance id to configuration (required for fp-ai)
		HashMap<String, Object> configuration = managerNodeDescriptor.getConfiguration();
		// TODO id may be null
		configuration.put("applianceId", getNode().getId() + "-appliance");

		// create the actual resource manager
		return (ResourceManager) componentManager.createComponent(managerNodeDescriptor.getFactoryPid(), configuration);
	}

	private ResourceDriver<?, ?> createResourceDriver(ComponentManager componentManager)
			throws ComponentCreationException {
		BundleContext bundleContext = componentManager.getBundleContext();

		// register activity as scheduled executor for driver
		ServiceRegistration schedulerRegistration = bundleContext.registerService(
				ScheduledExecutorService.class.getName(), modelUpdateActivity, null);
		Object schedulerServiceId = schedulerRegistration.getReference().getProperty(Constants.SERVICE_ID);

		// get driver descriptor and configuration
		ResourceDriverNodeDescriptor driverNodeDescriptor = getDriverNodeDescriptor();
		HashMap<String, Object> configuration = driverNodeDescriptor.getConfiguration();

		// add appliance id (required for fp-ai)
		// TODO id may be null
		configuration.put("applianceId", getNode().getId() + "-appliance");

		// figure out what the name of the reference to the scheduler is
		String schedulerReferenceName = getReferenceNameTo(bundleContext, driverNodeDescriptor.getFactoryPid(),
				ScheduledExecutorService.class);

		// adjust configuration so that the driver uses the right scheduler (using a reference-name.target property)
		String targetPropertyKey = schedulerReferenceName + ComponentConstants.REFERENCE_TARGET_SUFFIX;
		String targetFilter = "(" + Constants.SERVICE_ID + "=" + schedulerServiceId + ")";
		configuration.put(targetPropertyKey, targetFilter);

		// create the actual component and return it
		return (ResourceDriver<?, ?>) componentManager.createComponent(driverNodeDescriptor.getFactoryPid(),
				configuration);
	}

	private String getReferenceNameTo(BundleContext bundleContext, String componentName, Class<?> clazz) {
		// get the Service Component Runtime service (from felix scr)
		ScrService scrService = (ScrService) bundleContext.getService(bundleContext
				.getServiceReference(ScrService.class.getName()));

		// loop over all components and their references to find the reference name
		for (Component component : scrService.getComponents()) {
			// match component name with factory pid
			if (component.getName().equals(componentName)) {
				for (Reference reference : component.getReferences()) {
					if (clazz.getName().equals(reference.getServiceName())) {
						return reference.getName();
					}
				}
			}
		}
		return null;
	}

	private ResourceManagerNodeDescriptor getManagerNodeDescriptor() {
		// TODO check if there
		return (ResourceManagerNodeDescriptor) this.getNodeDescriptor().getChildren().get(0);
	}

	private ResourceDriverNodeDescriptor getDriverNodeDescriptor() {
		// TODO check if there
		return (ResourceDriverNodeDescriptor) this.getManagerNodeDescriptor().getChildren().get(0);
	}

	@Override
	public void initializeNode(ComponentManager componentManager, TimeService timeService, MarketBasis marketBasis,
			Broker broker) {
		super.initializeNode(componentManager, timeService, marketBasis, broker);

		FPAIAgent agent = (FPAIAgent) getNode();
		agent.bind(this.resourceManager);
		this.resourceManager.registerDriver(this.resourceDriver);
	}

	@Override
	public Activity[] getActivities() {
		return new Activity[] { this.bidUpdateActivity, this.modelUpdateActivity };
	}

	@Override
	protected ScheduledExecutorService getScheduledExecutorService() {
		return new DelegatingScheduledExecutorService() {
			@Override
			protected ScheduledExecutorService getDelegateForCommand(Object command) {
				return bidUpdateActivity;
			}
		};
	}
}
