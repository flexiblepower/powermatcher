package net.powermatcher.simulator.prototype.pmcore;

import java.util.concurrent.atomic.AtomicLong;

import net.powermatcher.simulator.prototype.TimeSource;


public abstract class ActiveObject implements Runnable {
	private static AtomicLong sequence = new AtomicLong();

	private TimeSource timeSource;
	protected String name;

	public ActiveObject() {
		this("ActiveObject-" + sequence.getAndIncrement());
	}

	public ActiveObject(String name) {
		this.name = name;
	}

	public void setTimeSource(TimeSource timeSource) {
		this.timeSource = timeSource;
	}

	public void run() {
		doUpdate();
	}

	public void doUpdate() {
	}

	public long getCurrentTimeMillis() {
		return this.timeSource.getCurrentTimeMillis();
	}
}
