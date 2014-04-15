package net.powermatcher.simulation.engine.dependencyengine;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.engine.ComponentCreationException;
import net.powermatcher.simulation.engine.ComponentManager;

public abstract class Composer<Type> {
	// private static AtomicInteger ranking = new AtomicInteger();

	private NodeDescriptor<? extends NodeDescriptor<?>> nodeDescriptor;
	protected Type node;
	protected long startTime;

	// private ServiceRegistration registration;

	public Composer(NodeDescriptor<? extends NodeDescriptor<?>> nodeDescriptor, long startTime) {
		this.nodeDescriptor = nodeDescriptor;
		this.startTime = startTime;
	}

	public abstract void attachIncommingLink(Link link, Class<?> iface);

	public abstract void attachOutgoingLink(Link link, Class<?> iface);

	protected abstract ScheduledExecutorService getScheduledExecutorService();

	@SuppressWarnings("unchecked")
	public Type createNode(ComponentManager componentManager) throws ComponentCreationException {
		// registerAsScheduledExecutorService(componentManager);

		HashMap<String, Object> configuration = nodeDescriptor.getConfiguration();
		String factoryPid = nodeDescriptor.getFactoryPid();
		return node = (Type) componentManager.createComponent(factoryPid, configuration);
	}

	// private void registerAsScheduledExecutorService(ComponentManager componentManager) {
	// Dictionary<String, Object> properties = new Hashtable<String, Object>();
	// properties.put(Constants.SERVICE_RANKING, ranking.incrementAndGet());
	//
	// BundleContext bundleContext = componentManager.getBundleContext();
	// registration = bundleContext.registerService(ScheduledExecutorService.class.getName(),
	// getScheduledExecutorService(), properties);
	// }
	//
	// // TODO this should be invoked once the component is destroyed!
	// private void unregisterAsScheduledExecutorService() {
	// if (registration != null) {
	// registration.unregister();
	// ranking.decrementAndGet();
	// }
	// }

	public abstract Activity[] getActivities();

	public Type getNode() {
		return node;
	}

	public NodeDescriptor<? extends NodeDescriptor<?>> getNodeDescriptor() {
		return nodeDescriptor;
	}
}
