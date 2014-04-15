package net.powermatcher.stubs.miele.appliance;


import java.util.List;

/**
 * @author IBM
 * @version 1.0.0
 */
public interface IMieleAppliance {

	/**
	 * @return TODO
	 */
	public List<String> getActions();

	/**
	 * @return TODO
	 */
	public String getAdditionalName();

	/**
	 * @return TODO
	 */
	public int getClassId();

	/**
	 * @return TODO
	 */
	public String getDetails();

	/**
	 * @return TODO
	 */
	public String getId();

	/**
	 * @return TODO
	 */
	public String getName();

	/**
	 * @return TODO
	 */
	public String getRoom();

	/**
	 * @return TODO
	 */
	public String getRoomId();

	/**
	 * @return TODO
	 */
	public String getRoomLevel();

	/**
	 * @return TODO
	 */
	public int getState();

	/**
	 * @return TODO
	 */
	public String getType();

	/**
	 * @param actions
	 */
	public void setActions(List<String> actions);

	/**
	 * @param name
	 */
	public void setAdditionalName(String name);

	/**
	 * @param classId
	 */
	public void setClassId(int classId);

	/**
	 * @param id
	 */
	public void setId(String id);

	/**
	 * @param name
	 */
	public void setName(String name);

	/**
	 * @param room
	 */
	public void setRoom(String room);

	/**
	 * @param id
	 */
	public void setRoomId(String id);

	/**
	 * @param Level
	 */
	public void setRoomLevel(String Level);

	/**
	 * @param state
	 */
	public void setState(int state);

	/**
	 * @param type
	 */
	public void setType(String type);

	// Action(s)
	/**
	 * 
	 */
	public void start();
}
