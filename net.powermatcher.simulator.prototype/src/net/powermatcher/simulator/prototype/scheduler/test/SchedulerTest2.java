package net.powermatcher.simulator.prototype.scheduler.test;

import java.util.concurrent.TimeUnit;

import net.powermatcher.simulator.prototype.Command;
import net.powermatcher.simulator.prototype.scheduler.Scheduler;

public class SchedulerTest2 {
	public static void main(String[] args) throws InterruptedException {
		final Scheduler scheduler = new Scheduler();
		TimeUnit unit = TimeUnit.MINUTES;

		scheduler.scheduleAtFixedRate(new Command("A", scheduler), 15, 1, unit, 0);
		scheduler.scheduleAtFixedRate(new Command("B", scheduler), 15, 2, unit, 2);
		scheduler.scheduleAtFixedRate(new Command("C", scheduler), 15, 3, unit, 3);

		scheduler.start();
		Thread.sleep(10 * 60 * 1000);
		scheduler.interrupt();
	}
}
