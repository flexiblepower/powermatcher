package net.powermatcher.simulator.prototype.dependencyengine;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import net.powermatcher.simulator.prototype.TimeSource;

public class DependencyEngine implements Runnable, TimeSource, Activity.ReadyListener, SimulationControl {
	private Queue<Activity> executed = new ConcurrentLinkedQueue<Activity>();

	private AtomicLong cycleCount = new AtomicLong();
	private ThreadPoolExecutor executor;
	private AtomicLong excutorCount = new AtomicLong(0);

	private final long cyclePeriod;
	private long timestamp;

	private Object lifecycleLock = new Object();
	private Future<?> engineFuture;
	private AtomicBoolean pause = new AtomicBoolean(false);

	public DependencyEngine(Date simulationStartDateTime, long cyclePeriod, TimeUnit cyclePeriodUnit) {
		this(simulationStartDateTime, cyclePeriod, cyclePeriodUnit, Runtime.getRuntime().availableProcessors() + 1);
	}

	public DependencyEngine(Date simulationStartDateTime, long cyclePeriod, TimeUnit cyclePeriodUnit, int threadCount) {
		this.cyclePeriod = cyclePeriodUnit.toMillis(cyclePeriod);
		this.timestamp = simulationStartDateTime.getTime();

		BlockingQueue<Runnable> queue = threadCount == 1 ? new SynchronousQueue<Runnable>()
				: new ArrayBlockingQueue<Runnable>(threadCount);
		this.executor = new ThreadPoolExecutor(threadCount, threadCount, 0, TimeUnit.MILLISECONDS, queue);
		this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public void run() {
		try {
			while (Thread.interrupted() == false) {
				this.waitIfPaused();

				this.cycleCount.incrementAndGet();

				Queue<Activity> start = executed;
				executed = new ConcurrentLinkedQueue<Activity>();
				for (Activity activity : start) {
					if (activity.isWithoutDependencies()) {
						this.execute(activity);
					}
				}

				while (excutorCount.get() > 0) {
					Thread.yield();
				}

				this.timestamp += this.cyclePeriod;
			}
		} catch (InterruptedException e) {
			// interrupted, so stop
		}
	}

	private void waitIfPaused() throws InterruptedException {
		if (this.pause.get()) {
			synchronized (this.lifecycleLock) {
				lifecycleLock.wait();
			}
		}
	}

	public Activity addActivity(Activity activity) {
		executed.add(activity);
		activity.setReadyListener(this);
		return activity;
	}

	@Override
	public void notifyIsReady(Activity activity) {
		execute(activity);
	}

	private void execute(final Activity activity) {
		excutorCount.incrementAndGet();

		executor.execute(new Runnable() {
			public void run() {
				try {
					activity.execute(timestamp);
				} finally {
					executed.add(activity);
					excutorCount.decrementAndGet();
				}
			}
		});
	}

	@Override
	public long getCurrentTimeMillis() {
		return timestamp;
	}

	public Date getCurrentTime() {
		return new Date(getCurrentTimeMillis());
	}

	public long getCycleCount() {
		return cycleCount.get();
	}

	private boolean isAlive() {
		synchronized (lifecycleLock) {
			return this.engineFuture != null && this.engineFuture.isCancelled() == false
					&& this.engineFuture.isDone() == false;
		}
	}

	@Override
	public void start() {
		synchronized (lifecycleLock) {
			if (isAlive()) {
				boolean wasPaused = this.pause.getAndSet(false);

				if (wasPaused) {
					synchronized (this.lifecycleLock) {
						this.lifecycleLock.notifyAll();
					}
				}

				return;
			} else {
				this.pause.set(false);
				this.engineFuture = this.executor.submit(this);
			}
		}
	}

	@Override
	public void pause() {
		if (this.pause.compareAndSet(false, true) == false) {

			this.pause.set(false);
			synchronized (this.lifecycleLock) {
				this.lifecycleLock.notifyAll();
			}
		}
	}

	@Override
	public void step() {
		synchronized (this.lifecycleLock) {
			this.lifecycleLock.notifyAll();
		}
	}

	@Override
	public void stop() {
		synchronized (lifecycleLock) {
			this.engineFuture.cancel(true);
			this.engineFuture = null;

			// TODO reset simulation?
		}
	}
}
