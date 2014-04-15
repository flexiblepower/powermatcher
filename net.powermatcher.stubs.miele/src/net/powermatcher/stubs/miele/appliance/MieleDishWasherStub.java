package net.powermatcher.stubs.miele.appliance;


import java.util.Date;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleDishWasherConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;

/**
 * @author IBM
 * @version 1.0.0
 */
public class MieleDishWasherStub extends AbstractMieleAppliance implements IMieleDishWasherStub, MieleDishWasherConstants {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		MieleDishWasherStub dw = new MieleDishWasherStub();
		// Switch on the DW
		dw.start();

		// Print the initial state
		System.out.println(dw.toString());

		// Program the DW
		dw.setProgram(PROGRAM_STRONG_65C);
		dw.setSmartGridOn(true);
		// dw.setRemainingTime(DEFAULT_PROGRAM_DURATION);

		for (int i = 0; i < 32; i++) {
			// Determine new state
			dw.determineState();

			System.out.println(dw.toString());

			// Sleep for 2 seconds
			Thread.sleep(2000);
		}

		// Start the programmed DW
		dw.start();
		System.out.println(dw.toString());

		for (int i = 0; i < 35; i++) {
			// Determine new state
			dw.determineState();

			System.out.println(dw.toString());

			// Sleep for 2 seconds
			Thread.sleep(2000);
		}

		// Simulate program almost completed
		dw.setRemainingTime(1);

		// Continue forever
		do {
			// Determine new state
			dw.determineState();

			System.out.println(dw.toString());

			// Sleep for 2 seconds
			Thread.sleep(2000);
		} while (true);

	}

	// Properties
	private int remainingTime;
	private int duration;
	private String program;
	private String phase;
	private boolean smartGridOn;

	private String startTime;

	// Fields required for behaviour
	private Date lastStateChange;

	/**
	 * 
	 */
	public MieleDishWasherStub() {
		super();
		initialize();
	}

	private void determineState() {
		Date currentTime = new Date();
		int elapsedTimeMinutes = Math.round((currentTime.getTime() - this.lastStateChange.getTime()) / 1000 / 60);

		if (getState() == MieleApplianceConstants.MA_STATE_ON && getProgram() != null) {
			// DW is on and is programmed

			if (getRemainingTime() <= elapsedTimeMinutes) {

				// Previous state was ON and elapsed time longer than remaining
				// time: program completed.
				setState(MieleApplianceConstants.MA_STATE_END);
				setRemainingTime(0);
			} else {
				// Set the remaining time to complete the program
				setRemainingTime(getRemainingTime() - elapsedTimeMinutes);
			}
		} else if (getState() == MieleApplianceConstants.MA_STATE_PROGRAM && isSmartGridOn() == false) {
			// Previous state was that the DW was programmed

			if (getRemainingTime() <= elapsedTimeMinutes
					&& (getRemainingTime() + DEFAULT_PROGRAM_DURATION) > elapsedTimeMinutes) {
				// DW started but program is not completed yet.

				// The DW is on
				setState(MieleApplianceConstants.MA_STATE_ON);

				// Set the remaining time to complete the program
				setRemainingTime(DEFAULT_PROGRAM_DURATION - (elapsedTimeMinutes - getRemainingTime()));
			} else if (getRemainingTime() + DEFAULT_PROGRAM_DURATION <= elapsedTimeMinutes) {
				// DW started and program has been completed.
				setState(MieleApplianceConstants.MA_STATE_END);

				// Set remaining time to 0
				setRemainingTime(0);
			} else {
				// DW did not start yet. Update remaining time
				// Set the remaining waiting time
				setRemainingTime(getRemainingTime() - elapsedTimeMinutes);
			}
		}

		// Remember time stamp last state change (only when at least
		// one minute time elapsed).
		if (elapsedTimeMinutes > 0) {
			this.lastStateChange = currentTime;
		}

	}

	@Override
	public int getDuration() {
		return this.duration;
	}

	@Override
	public String getPhase() {
		return this.phase;
	}

	@Override
	public String getProgram() {
		return this.program;
	}

	@Override
	public int getRemainingTime() {
		return this.remainingTime;
	}

	@Override
	public String getStartTime() {
		return this.startTime;
	}

	private void initialize() {
		// Initial state of the DW is OFF
		setState(MieleApplianceConstants.MA_STATE_OFF);
		setProgram(null);
		setRemainingTime(0);
		setPhase(null);
		this.lastStateChange = new Date();
	}

	@Override
	public boolean isSmartGridOn() {
		return this.smartGridOn;
	}

	@Override
	public void setDuration(final int duration) {
		this.duration = duration;

	}

	@Override
	public void setPhase(final String phase) {
		this.phase = phase;

	}

	@Override
	public void setProgram(final String program) {
		this.program = program;

		if (program != null) {
			setState(MieleApplianceConstants.MA_STATE_PROGRAM);
			setDuration(DEFAULT_PROGRAM_DURATION);
			if (this.smartGridOn) {
				addAction(MieleApplianceConstants.APPLIANCE_ACTION_START);
			}
		}
	}

	@Override
	public void setRemainingTime(final int minutes) {
		this.remainingTime = minutes;

	}

	@Override
	public void setSmartGridOn(final boolean smartGridOn) {
		this.smartGridOn = smartGridOn;
	}

	@Override
	public void setStartTime(final String startTime) {
		this.startTime = startTime;
	}

	@Override
	public void start() {
		// First set current state since previous state update
		determineState();

		if (getState() == MieleApplianceConstants.MA_STATE_OFF) {
			// DW was off. Initialize
			this.setState(MieleApplianceConstants.MA_STATE_ON);
		} else if (getState() == MieleApplianceConstants.MA_STATE_PROGRAM) {
			// Set the new state
			setState(MieleApplianceConstants.MA_STATE_ON);

			// Setting the program duration
			// setRemainingTime(null);
			setRemainingTime(DEFAULT_PROGRAM_DURATION);

			// Set the duration
			setDuration(DEFAULT_PROGRAM_DURATION);

			// Remove the start action
			removeAction(MieleApplianceConstants.APPLIANCE_ACTION_START);
			addAction(MieleApplianceConstants.APPLIANCE_ACTION_STOP);
		} else if (getState() == MieleApplianceConstants.MA_STATE_WAITING) {
			// Set the new state
			setState(MieleApplianceConstants.MA_STATE_ON);

			// Setting the program duration
			// setRemainingTime(null);
			setRemainingTime(DEFAULT_PROGRAM_DURATION);

			// Set the duration
			setDuration(DEFAULT_PROGRAM_DURATION);

			// Remove the start action
			removeAction(MieleApplianceConstants.APPLIANCE_ACTION_START);
			addAction(MieleApplianceConstants.APPLIANCE_ACTION_STOP);
		}

		// Perform state change again after changing the properties
		determineState();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("DW ");
		sb.append("State=");
		sb.append(MieleApplianceUtil.getStateDescription(null, this.getState()));
		sb.append(" Remaining=");
		sb.append(getRemainingTime() / 60 + ":" + getRemainingTime() % 60 + "h");
		sb.append(" Program");
		sb.append(getProgram() == null ? "NONE" : getProgram());
		return sb.toString();
	}

	@Override
	public void update() {
		determineState();
	}

}
