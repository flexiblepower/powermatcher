package net.powermatcher.simulator.prototype.dependencyengine.test;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.powermatcher.simulator.prototype.dependencyengine.Activity;
import net.powermatcher.simulator.prototype.dependencyengine.DependencyEngine;
import net.powermatcher.simulator.prototype.dependencyengine.Link;

/**
 * This prototype demonstrates the possibilities and limitations for simulation
 * timed execution of tasks by dependency order.
 * 
 * @author IBM
 * @author TNO
 */
public class DependencyEngineBenchmarkTree {
	public static void main(final String[] args) throws InterruptedException {
		System.out.println("[" + new Date() + "] Starting BenchmarkTree with DependencyEngine");
		System.out.println();
		for (int levels = 1; levels < 7; levels++) {
			doBenchmark(levels);
		}
		System.out.println("[" + new Date() + "] Ending BenchmarkTree with DependencyEngine");
	}

	private static void doBenchmark(int levels) throws InterruptedException {
		doBenchmark(1, levels);
		doBenchmark(2, levels);
		doBenchmark(3, levels);
		doBenchmark(4, levels);
	}

	private static void doBenchmark(int corePoolSize, int levels) throws InterruptedException {
		CPUBoundTask.resetCounters();

		int runSeconds = 10;
		TimeUnit unit = TimeUnit.SECONDS;
		DependencyEngine engine = new DependencyEngine(new Date(0), 1, unit, corePoolSize);

		Activity root = engine.addActivity(new Activity("", new CPUBoundTask("Task 0.0")));
		root.scheduleAtFixedRate(new Date(10), 1, unit);

		int taskCount = 1 + addChildren(engine, root, 1, 8, levels, unit);

		engine.start();
		Thread.sleep(runSeconds * 1000);
		engine.stop();

		long completedTaskCount = CPUBoundTask.getCompletedTaskCount();
		long cpuBoundTaskNanos = CPUBoundTask.getAverageCPUNanos();
		long treeIterations = completedTaskCount / taskCount;
		long tasksPerSecond = completedTaskCount / runSeconds;
		long taskDuration = runSeconds * 1000000000l / completedTaskCount * corePoolSize;

		System.out.println("Stages             = " + levels);
		System.out.println("Core pool size     = " + corePoolSize);
		System.out.println("Tasks in tree      = " + taskCount);
		System.out.println("Tree iterations/sec= " + treeIterations / runSeconds);
		System.out.println("Tasks/sec          = " + tasksPerSecond);
		System.out.println("Tasks/sec/core     = " + tasksPerSecond / corePoolSize);
		System.out.println("Task cpu           = " + cpuBoundTaskNanos + " ns");
		System.out.println("Avg task duration  = " + taskDuration + " ns");
		System.out.println("Efficiency         = " + (cpuBoundTaskNanos * 100) / taskDuration + " %");
		System.out.println();

	}

	private static int addChildren(DependencyEngine engine, Activity parent, int level, int childCount, int maxLevels,
			TimeUnit unit) {
		if (level >= maxLevels) {
			return 0;
		}

		int addedCount = 0;

		for (int i = 0; i < childCount; i++) {
			Activity activity = engine.addActivity(new Activity("", new CPUBoundTask("Task " + level + "." + i)));
			activity.scheduleAtFixedRate(new Date(10), 1, unit);

			Link link = new Link();
			parent.addDependency(link);
			link.addDependency(activity);

			addedCount += 1 + addChildren(engine, activity, level + 1, childCount, maxLevels, unit);
		}

		return addedCount;
	}
}
