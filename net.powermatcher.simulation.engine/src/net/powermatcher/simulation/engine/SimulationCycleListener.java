package net.powermatcher.simulation.engine;

// TODO add note that warns the programmer to only perform lightweight tasks
public interface SimulationCycleListener {

	public void simulationCycleBegins(long timestamp);

	public void simulationCycleFinishes(long timestamp);

	public void simulationFinished();

	public void simulationStarts(long timestamp);
}
