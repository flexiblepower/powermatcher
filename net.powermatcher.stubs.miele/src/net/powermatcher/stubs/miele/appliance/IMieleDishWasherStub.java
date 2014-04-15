package net.powermatcher.stubs.miele.appliance;


/**
 * @author IBM
 * @version 1.0.0
 */
public interface IMieleDishWasherStub extends IMieleAppliance {

	/**
	 * @return TODO
	 */
	public int getDuration();

	/**
	 * @return TODO
	 */
	public String getPhase();

	/**
	 * @return TODO
	 */
	public String getProgram();

	/**
	 * @return TODO
	 */
	public int getRemainingTime();

	/**
	 * @return TODO
	 */
	public String getStartTime();

	/**
	 * @return TODO
	 */
	public boolean isSmartGridOn();

	/**
	 * @param minutes 
	 */
	public void setDuration(int minutes);

	/**
	 * @param phase
	 */
	public void setPhase(String phase);

	/**
	 * @param program
	 */
	public void setProgram(String program);

	/**
	 * @param minutes
	 */
	public void setRemainingTime(int minutes);

	/**
	 * @param on
	 */
	public void setSmartGridOn(boolean on);

	/**
	 * @param time
	 */
	public void setStartTime(String time);

	/**
	 * 
	 */
	public void update();
}
