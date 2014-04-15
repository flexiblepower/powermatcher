package net.powermatcher.simulation.engine;

import java.util.concurrent.TimeUnit;

public interface SimulationControl {
	void start();

	void pause();

	void step();

	void stop();

	void setDelay(long delay, TimeUnit unit);

	long getDelay();

	long getDelay(TimeUnit unit);

	void addSimulationCycleListener(SimulationCycleListener listener);

	boolean removeSimulationCycleListener(SimulationCycleListener listener);

	SimulationState getState();

	public enum SimulationState {
		/*INITIALIZING,*/ RUNNING, PAUSED, STOPPED
	}
}
