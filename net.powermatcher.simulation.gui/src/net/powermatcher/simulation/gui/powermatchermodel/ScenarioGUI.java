package net.powermatcher.simulation.gui.powermatchermodel;


public class ScenarioGUI {
	private AgentGroup rootGroup;

	private String name;

	private String id;

	public ScenarioGUI() {
	}

	public void setSessionDescription(String name, String id) {
		this.name = name;
		this.id = id;
	}

	public AgentGroup getRoot() {
		if (rootGroup == null)
			rootGroup = new AgentGroup(null, "RootGroup", "-1");
		return rootGroup;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}
}
