package net.powermatcher.simulation.engine.dependencyengine;

import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.agent.auctioneer.Auctioneer;
import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.engine.ComponentCreationException;
import net.powermatcher.simulation.engine.ComponentManager;

public class AuctioneerComposer extends AgentComposer<Auctioneer> {
	private Activity priceUpdateActivity;

	public AuctioneerComposer(NodeDescriptor nodeDescriptor, long startTime) {
		super(nodeDescriptor, startTime);
	}

	@Override
	public void attachIncommingLink(Link link, Class<?> iface) {
		if (UpMessagable.class.equals(iface)) {
			this.priceUpdateActivity.setDependentOn(link);
		} else {
			throw new IllegalArgumentException("unsupported interface");
		}
	}

	@Override
	public void attachOutgoingLink(Link link, Class<?> iface) {
		if (DownMessagable.class.equals(iface)) {
			link.setDependentOn(this.priceUpdateActivity);
		} else {
			throw new IllegalArgumentException("unsupported interface");
		}
	}

	@Override
	public Auctioneer createNode(ComponentManager componentManager) throws ComponentCreationException {
		Auctioneer auctioneer = super.createNode(componentManager);

		this.priceUpdateActivity = new Activity("price-update " + auctioneer.getName());

		return auctioneer;
	}

	@Override
	public Activity[] getActivities() {
		return new Activity[] { this.priceUpdateActivity };
	}

	@Override
	protected ScheduledExecutorService getScheduledExecutorService() {
		return new DelegatingScheduledExecutorService() {
			@Override
			protected ScheduledExecutorService getDelegateForCommand(Object command) {
				// TODO we will assume a PriceUpdateTask for now
				return priceUpdateActivity;
			}
		};
	}
}
