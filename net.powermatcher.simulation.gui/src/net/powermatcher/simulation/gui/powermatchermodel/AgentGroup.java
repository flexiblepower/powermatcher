package net.powermatcher.simulation.gui.powermatchermodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;

public class AgentGroup extends AgentInfo {
	private List<AgentInfo> entries;

	private AgentGroup parent;

	private String name;

	private ListenerList listeners;
	private String id;

	public AgentGroup(AgentGroup parent, String name, String id) {
		this.parent = parent;
		this.name = name;
		this.id = id;
		fireContactsChanged(null);
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

	public void rename(String newName) {
		this.name = newName;
		fireContactsChanged(null);
	}

	// @SuppressWarnings("unchecked")
	// public void addEntry(Object entry) {
	// if(entry instanceof AgentGroup && entries == null){
	// entries.add((AgentGroup) entry);
	// } else if(entry instanceof AgentEntry){
	// if(entries == null || entries.get(0) instanceof AgentEntry){
	// entries.add((AgentEntry) entry);
	// } else{
	// System.out.println("Cannot add agent entry in this Group");
	// }
	// }
	//
	// fireContactsChanged(null);
	// }
	//
	// public void removeEntry(Object entry) {
	// if(entry instanceof AgentGroup && entries != null){
	// entries.remove((AgentGroup) entry);
	// if(entries.isEmpty()){
	// entries = null;
	// }
	// } else if(entry instanceof AgentEntry){
	// if(entries != null || entries.get(0) instanceof AgentEntry){
	// entries.remove((AgentEntry) entry);
	// if(entries.isEmpty()){
	// entries = null;
	// }
	// } else{
	// System.out.println("Cannot remove agent entry in this Group");
	// }
	// }
	// fireContactsChanged(null);
	// }
	public void addEntry(AgentInfo entry) {
		if (entries == null)
			entries = new ArrayList<AgentInfo>();
		entries.add(entry);
		fireContactsChanged(null); 
	}

	public void removeEntry(AgentInfo entry) {
		if (entries != null) {
			entries.remove(entry);
			if (entries.isEmpty())
				entries = null;
		}
		fireContactsChanged(null);
	}

	public AgentInfo[] getEntries() {
		if (entries != null)
			return (AgentInfo[]) entries.toArray(new AgentInfo[entries.size()]);
		return new AgentInfo[0];
	}

	public void addAgentChangeListener(AgentsListener listener) {
		if (parent != null)
			parent.addAgentChangeListener(listener);
		else {
			if (listeners == null)
				listeners = new ListenerList();
			listeners.add(listener);
		}
	}

	public void removeContactsListener(AgentsListener listener) {
		if (parent != null)
			parent.removeContactsListener(listener);
		else {
			if (listeners != null) {
				listeners.remove(listener);
				if (listeners.isEmpty())
					listeners = null;
			}
		}
	}

	protected void fireContactsChanged(AgentEntry entry) {
		if (parent != null)
			parent.fireContactsChanged(entry);
		else {
			if (listeners == null)
				return;
			Object[] rls = listeners.getListeners();
			for (int i = 0; i < rls.length; i++) {
				AgentsListener listener = (AgentsListener) rls[i];
				listener.agentInfoChanged(this, entry);
			}
		}
	}

	public List<AgentInfo> getChildren() {
		// TODO Auto-generated method stub
		return entries;
	}

	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return entries!=null? true:false;
	}
	
	public boolean hasGroup(){
		if(entries == null){
			return false;
		}
		for (AgentInfo info: entries){
			if(info instanceof AgentGroup){
				return true;
			}
		}
		return false; 
	}
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	

}
