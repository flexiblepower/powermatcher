package net.powermatcher.simulator.prototype.dependencyengine;

public interface SimulationControl {
	void start();

	void pause();

	void step();

	void stop();
}
