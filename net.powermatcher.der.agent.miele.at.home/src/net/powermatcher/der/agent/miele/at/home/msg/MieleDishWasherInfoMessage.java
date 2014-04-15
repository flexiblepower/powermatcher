package net.powermatcher.der.agent.miele.at.home.msg;


import java.util.Date;

/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleDishWasherInfoMessage extends MieleApplianceInfoMessage {

	protected Date startTime;
	protected int remainingTime;
	protected int duration;
	protected String program;
	protected String phase;

	/**
	 * Default constructor.
	 */
	public MieleDishWasherInfoMessage() {
		super();
	}

	/**
	 * Constructor to support downcasting. It creates a
	 * MieleDishWasherInfoMessage from a MieleApplianceInfoMessage object.
	 * 
	 * @param a
	 *            The MieleApplianceInfoMessage instance to create a new
	 *            MieleDishWasherInfoMessage object.
	 */
	public MieleDishWasherInfoMessage(final MieleApplianceInfoMessage a) {
		super();

		// Set attributes from parameter
		setApplianceId(a.getApplianceId());
		setApplianceType(a.getApplianceType());
		setApplianceState(a.getApplianceState());
		setApplianceClass(a.getApplianceClass());
		setActions(a.getActions());
	}

	/**
	 * @return TODO
	 */
	public int getDuration() {
		return this.duration;
	}

	/**
	 * @return TODO
	 */
	public String getPhase() {
		return this.phase;
	}

	/**
	 * @return TODO
	 */
	public String getProgram() {
		return this.program;
	}

	/**
	 * @return TODO
	 */
	public int getRemainingTime() {
		return this.remainingTime;
	}

	/**
	 * @return TODO
	 */
	public Date getStartTime() {
		return this.startTime;
	}

	/**
	 * @param duration
	 */
	public void setDuration(final int duration) {
		this.duration = duration;
	}

	/**
	 * @param phase
	 */
	public void setPhase(final String phase) {
		this.phase = phase;
	}

	/**
	 * @param program
	 */
	public void setProgram(final String program) {
		this.program = program;
	}

	/**
	 * @param remainingTime
	 */
	public void setRemainingTime(final int remainingTime) {
		this.remainingTime = remainingTime;
	}

	/**
	 * @param startTime
	 */
	public void setStartTime(final Date startTime) {
		this.startTime = startTime;
	}
}
