package net.powermatcher.simulator.prototype.scheduler;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import net.powermatcher.simulator.prototype.TimeSource;

public class Scheduler extends Thread implements TimeSource {
	private PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<Task>();
	private Queue<Future<?>> futures = new ConcurrentLinkedQueue<Future<?>>();

	private ExecutorService executor;

	private long currentTime;
	private int currentStage;
	private AtomicLong cycleCount = new AtomicLong();

	public Scheduler() {
		this(Runtime.getRuntime().availableProcessors() + 1);
	}

	public Scheduler(int threadCount) {
		executor = Executors.newFixedThreadPool(threadCount);
	}

	public void submit(Runnable runnable) {
		futures.add(executor.submit(runnable));
	}

	public void scheduleAtFixedRate(Runnable runnable, int delay, int period, TimeUnit unit, int stage) {
		queue.add(new Task(runnable, unit.toMillis(period), stage, unit.toMillis(delay)));
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				Task task = queue.take();

				if (task.executionTime != this.currentTime || task.stage != currentStage) {
					while (!futures.isEmpty()) {
						Future<?> future = futures.poll();
						try {
							future.get();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					queue.add(task);
					task = queue.take();

					currentTime = task.executionTime;
					currentStage = task.stage;

					cycleCount.incrementAndGet();
				}

				futures.add(executor.submit(task));
			}
		} catch (InterruptedException e) {
			// we're done
		} finally {
			executor.shutdown();
		}
	}

	public long getCurrentTimeMillis() {
		return currentTime;
	}

	public Date getCurrentTime() {
		return new Date(getCurrentTimeMillis());
	}

	private class Task implements Comparable<Task>, Runnable {
		private Runnable runnable;
		private int stage = -1;
		private long period;
		private long executionTime;

		public Task(Runnable runnable, long period, int stage, long startTime) {
			this.runnable = runnable;
			this.period = period;
			this.stage = stage;
			this.executionTime = startTime;
		}

		@Override
		public int compareTo(Task o) {
			int result;
			if (o == this) {
				result = 0;
			} else {
				Task other = (Task) o;
				long diff = this.executionTime - other.executionTime;
				if (diff < 0) {
					result = -1;
				} else if (diff > 0) {
					result = 1;
				} else if (this.stage > other.stage) {
					result = -1;
				} else if (this.stage < other.stage) {
					result = 1;
				} else {
					result = 1;
				}
			}
			return result;
		}

		@Override
		public void run() {
			runnable.run();
			this.executionTime += period;
			Scheduler.this.queue.add(this);
		}

		@Override
		public String toString() {
			return new Date(executionTime) + " : " + stage + " > " + runnable;
		}
	}

	public long getCycleCount() {
		return this.cycleCount.get();
	}
}
