package net.powermatcher.stubs.miele.appliance;


import java.util.ArrayList;
import java.util.List;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;

/**
 * @author IBM
 * @version 1.0.0
 */
public abstract class AbstractMieleAppliance implements IMieleAppliance {

	private String id;
	private int classId;
	private String type;
	private String name;
	private String additionalName;
	private int state;
	private String room;
	private String roomId;
	private String roomLevel;
	private List<String> actions;

	/**
	 * @param name
	 */
	public void addAction(final String name) {
		if (this.actions == null) {
			this.actions = new ArrayList<String>();
		}
		if (!this.actions.contains(name)) {
			this.actions.add(name);
		}
	}

	@Override
	public List<String> getActions() {
		return this.actions;
	}

	@Override
	public String getAdditionalName() {
		return this.additionalName;
	}

	@Override
	public int getClassId() {
		return this.classId;
	}

	@Override
	public String getDetails() {
		// To be implemented by child class
		return null;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getRoom() {
		return this.room;
	}

	@Override
	public String getRoomId() {
		return this.roomId;
	}

	@Override
	public String getRoomLevel() {
		return this.roomLevel;
	}

	@Override
	public int getState() {
		return this.state;
	}

	@Override
	public String getType() {
		return this.type;
	}

	/**
	 * @param name
	 */
	public void removeAction(final String name) {
		if (this.actions != null) {
			this.actions.remove(name);
		}
	}

	@Override
	public void setActions(final List<String> actions) {
		this.actions = actions;

	}

	@Override
	public void setAdditionalName(final String additionalName) {
		this.additionalName = additionalName;
	}

	@Override
	public void setClassId(final int classId) {
		this.classId = classId;
	}

	@Override
	public void setId(final String id) {
		this.id = id;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public void setRoom(final String room) {
		this.room = room;
	}

	@Override
	public void setRoomId(final String roomId) {
		this.roomId = roomId;
	}

	@Override
	public void setRoomLevel(final String roomLevel) {
		this.roomLevel = roomLevel;
	}

	@Override
	public void setState(final int state) {
		this.state = state;
	}

	@Override
	public void setType(final String type) {
		this.type = type;
	}

	@Override
	public void start() {
		setState(MieleApplianceConstants.MA_STATE_ON);
	}

}
