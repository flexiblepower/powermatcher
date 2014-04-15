package net.powermatcher.simulator.prototype.dependencyengine.test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.powermatcher.simulator.prototype.Command;
import net.powermatcher.simulator.prototype.dependencyengine.Activity;
import net.powermatcher.simulator.prototype.dependencyengine.DependencyEngine;

public class DependencyEngineTest1 {
	public static void main(String[] args) throws Exception {
		TimeUnit unit = TimeUnit.MINUTES;
		final DependencyEngine engine = new DependencyEngine(new Date(0), 1, unit);

		schedule("A", unit, 1, engine);
		schedule("B", unit, 2, engine);
		schedule("C", unit, 3, engine);

		engine.start();
		Thread.sleep(100);
		engine.stop();
	}

	private static void schedule(String name, TimeUnit unit, int period, final DependencyEngine engine) {
		Activity activity = new Activity(name, new Command(name, engine));
		activity.scheduleAtFixedRate(engine.getCurrentTime(), period, unit);
		engine.addActivity(activity);
	}
}
