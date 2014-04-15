package net.powermatcher.simulation.gui.menus;

import net.powermatcher.core.agent.auctioneer.Auctioneer;
import net.powermatcher.simulation.configuration.NodeDescriptor;

public class AddAuctioneerMenuContribution extends AddNodeMenuContribution {
	public AddAuctioneerMenuContribution() {
	}

	public AddAuctioneerMenuContribution(String id) {
		super(id);
	}

	@Override
	protected boolean filterComponentClass(Class<?> componentClass) {
		return Auctioneer.class.isAssignableFrom(componentClass);
	}

	@Override
	protected void createComponent(String componentFactoryPid, NodeDescriptor parent) {
		throw new UnsupportedOperationException();
	}
}
