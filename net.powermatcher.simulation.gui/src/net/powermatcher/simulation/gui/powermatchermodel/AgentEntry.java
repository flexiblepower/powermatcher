package net.powermatcher.simulation.gui.powermatchermodel;

public class AgentEntry extends AgentInfo{

	private AgentGroup parent;
	private String name;
	private String id;

	public AgentEntry(AgentGroup group, String name, String ID){
		this.parent = group;
		this.name = name; 
		this.id = ID; 
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public AgentGroup getParent() {
		// TODO Auto-generated method stub
		return this.parent;
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	

}
