package net.powermatcher.simulator.prototype;

public class Command implements Runnable {
	private String name;
	private final TimeSource scheduler;

	public Command(String name, TimeSource scheduler) {
		super();
		this.name = name;
		this.scheduler = scheduler;
	}

	public void run() {
		 System.out.println(name + " @ " + scheduler.getCurrentTime());
	}
}