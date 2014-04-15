package net.powermatcher.simulation.engine.simulationclock;

/**
 * A SimulationClock which lets the simulation run in real time.
 */
public class RealtimeSimulationClock implements SimulationClock {

	/** The start timestamp. */
	private long startTimestamp;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.powermatcher.simulation.engine.simulationclock.SimulationClock#
	 * getNextTimestamp()
	 */
	@Override
	public long getNextTimestamp() {
		return System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.powermatcher.simulation.engine.simulationclock.SimulationClock#
	 * getStartTimestamp()
	 */
	@Override
	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.simulation.engine.simulationclock.SimulationClock#initialize
	 * ()
	 */
	@Override
	public void initialize() {
		this.startTimestamp = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.powermatcher.simulation.engine.simulationclock.SimulationClock#
	 * simulationIsFinished()
	 */
	@Override
	public boolean simulationIsFinished() {
		// Real time never finishes
		// (and if it does, it probably doesn't matter what this function
		// returns)
		return false;
	}

}
