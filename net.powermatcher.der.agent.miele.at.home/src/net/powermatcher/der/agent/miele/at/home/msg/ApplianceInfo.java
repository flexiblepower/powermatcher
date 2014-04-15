package net.powermatcher.der.agent.miele.at.home.msg;


import java.util.Map;

/**
 * @author IBM
 * @version 0.9.0
 */
public class ApplianceInfo {

	private String id;
	private int classId;
	private String type;
	private String name;
	private String additionalName;
	private int state;
	private String room;
	private String roomId;
	private String roomLevel;

	private Map<String, String> information;
	private Map<String, String> actions;

	/**
	 * @return TODO
	 */
	public Map<String, String> getActions() {
		return this.actions;
	}

	/**
	 * @return TODO
	 */
	public String getAdditionalName() {
		return this.additionalName;
	}

	/**
	 * @return TODO
	 */
	public int getClassId() {
		return this.classId;
	}

	/**
	 * @return TODO
	 */
	public String getDetails() {
		// To be implemented by child class
		return null;
	}

	/**
	 * @return TODO
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return TODO
	 */
	public Map<String, String> getInformation() {
		return this.information;
	}

	/**
	 * @return TODO
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return TODO
	 */
	public String getRoom() {
		return this.room;
	}

	/**
	 * @return TODO
	 */
	public String getRoomId() {
		return this.roomId;
	}

	/**
	 * @return TODO
	 */
	public String getRoomLevel() {
		return this.roomLevel;
	}

	/**
	 * @return TODO
	 */
	public int getState() {
		return this.state;
	}

	/**
	 * @return TODO
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @param actions
	 */
	public void setActions(final Map<String, String> actions) {
		this.actions = actions;
	}

	/**
	 * @param additionalName
	 */
	public void setAdditionalName(final String additionalName) {
		this.additionalName = additionalName;
	}

	/**
	 * @param classId
	 */
	public void setClassId(final int classId) {
		this.classId = classId;
	}

	/**
	 * @param id
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @param information
	 */
	public void setInformation(final Map<String, String> information) {
		this.information = information;
	}

	/**
	 * @param name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param room
	 */
	public void setRoom(final String room) {
		this.room = room;
	}

	/**
	 * @param roomId
	 */
	public void setRoomId(final String roomId) {
		this.roomId = roomId;
	}

	/**
	 * @param roomLevel
	 */
	public void setRoomLevel(final String roomLevel) {
		this.roomLevel = roomLevel;
	}

	/**
	 * @param state
	 */
	public void setState(final int state) {
		this.state = state;
	}

	/**
	 * @param type
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Appliance info: ");
		for (String key : this.information.keySet()) {
			sb.append(key);
			sb.append("=");
			sb.append(this.information.get(key));
			sb.append(";");

		}
		return sb.toString();
	}

}
