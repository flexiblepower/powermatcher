package net.powermatcher.simulator.prototype.dependencyengine.test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 *
 * 
 * @author IBM
 */
public class CPUBoundTask implements Runnable {

	public static final int DEFAULT_ITERATIONS = 10000;
	private static AtomicLong totalDuration = new AtomicLong();
	private static AtomicInteger runCount = new AtomicInteger();
	public static long lastCounter;
	
	protected String name;
	private int iterations;
	private long counter;

	public CPUBoundTask(final String name) {
		this(name, DEFAULT_ITERATIONS);
	}

	public CPUBoundTask(final String name, final int iterations) {
		this.name = name;
		this.iterations = iterations;
	}

	@Override
	public void run() {
		long startTime = System.nanoTime();
		doWork();
		runCount.incrementAndGet();
		totalDuration.getAndAdd(System.nanoTime() - startTime);
	}

	private void doWork() {
		for (int i = 0 ; i < iterations ; i++) {
			counter += i * 3;
		}
		 // Without the counter the IBM JIT will take out the loop as a result of escape analysis.
		lastCounter = counter;
	}

	@Override
	public String toString() {
		return this.name;
	}

	public static void resetCounters() {
		runCount.set(0);
		totalDuration.set(0);
	}

	public static int getCompletedTaskCount() {
		return runCount.get();		
	}
	
	public static long getAverageCPUNanos() {
		return totalDuration.get() / runCount.get();		
	}

}
