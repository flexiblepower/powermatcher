package net.powermatcher.simulation.gui.powermatchermodel;

import org.eclipse.core.runtime.PlatformObject;

public abstract class AgentInfo extends PlatformObject {

	public abstract String getName();
	public abstract AgentGroup getParent();
	public abstract String getId();
	
}
