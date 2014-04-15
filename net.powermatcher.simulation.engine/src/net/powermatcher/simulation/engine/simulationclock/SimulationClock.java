package net.powermatcher.simulation.engine.simulationclock;

/**
 * Interface for simulation clocks, which are used as a virtual time source for
 * the simulation.
 * 
 * The simulation engine works with cycles. Every cycle all components in the
 * simulation are updated with a new timestamp. The SimulationClock controls the
 * time. All values are in milliseconds.
 * 
 * Every simulation has only one SimulationClock, so it is not necessary to
 * coordinate things in a distributed environment.
 */
public interface SimulationClock {

	/**
	 * Gets the timestamp for the next cycle.
	 * 
	 * @return the timestamp for the next cycle
	 */
	public long getNextTimestamp();

	/**
	 * Gets the first timestamp for the simulation. This function can be called
	 * several times and should always return the same value.
	 * 
	 * The StartTimestamp is used for components who schedule themselves.
	 * 
	 * @return the first timestamp
	 */
	public long getStartTimestamp();

	/**
	 * Initialize the Simulation clock. This function is called only once and
	 * right before the first call to getStartTimestamp();
	 */
	public void initialize();

	/**
	 * Indicates if the simulation is finished. It is not necessary for
	 * SimulationClocks to finish.
	 * 
	 * @return whether the simulation should stop
	 */
	public boolean simulationIsFinished();
}
