package net.powermatcher.simulation.engine.dependencyengine;

import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import net.powermatcher.core.scheduler.service.TimeServicable;
import net.powermatcher.simulation.engine.SimulationControl;
import net.powermatcher.simulation.engine.SimulationCycleListener;
import net.powermatcher.simulation.engine.simulationclock.SimulationClock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyEngine implements Runnable, TimeServicable, Activity.ReadyListener, SimulationControl {
	private static Logger logger = LoggerFactory.getLogger(DependencyEngine.class);

	/** delay between cycles in milliseconds */
	private volatile long delay;
	private Future<?> engineFuture;

	private final AtomicLong excutorCount = new AtomicLong(0);
	private Queue<Activity> executed = new ConcurrentLinkedQueue<Activity>();

	private final ThreadPoolExecutor executor;
	private final Object lifecycleLock = new Object();
	private final AtomicBoolean pause = new AtomicBoolean(false);
	private final SimulationClock simulationClock;

	private final Set<SimulationCycleListener> simulationCycleListeners = new CopyOnWriteArraySet<SimulationCycleListener>();

	private final AtomicLong stepCount = new AtomicLong();

	private long timestamp;

	public DependencyEngine(SimulationClock simulationClock) {
		this(simulationClock, Runtime.getRuntime().availableProcessors() + 1);
	}

	public DependencyEngine(SimulationClock simulationClock, int threadCount) {
		this.simulationClock = simulationClock;
		this.simulationClock.initialize();
		this.timestamp = simulationClock.getStartTimestamp();

		BlockingQueue<Runnable> queue = threadCount == 1 ? new SynchronousQueue<Runnable>()
				: new ArrayBlockingQueue<Runnable>(threadCount);
		this.executor = new ThreadPoolExecutor(threadCount, threadCount, 0, TimeUnit.MILLISECONDS, queue);
		this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		this.executor.setThreadFactory(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "simulation-thread-" + (executor.getPoolSize() + 1));
				thread.setPriority(Thread.MIN_PRIORITY);
				return thread;
			}
		});
	}

	public Activity addActivity(Activity activity) {
		this.executed.add(activity);
		activity.setReadyListener(this);
		return activity;
	}

	@Override
	public void addSimulationCycleListener(SimulationCycleListener l) {
		this.simulationCycleListeners.add(l);
	}

	@Override
	public boolean removeSimulationCycleListener(SimulationCycleListener l) {
		return this.simulationCycleListeners.remove(l);
	}

	@Override
	public long currentTimeMillis() {
		return this.timestamp;
	}

	private void execute(final Activity activity) {
		this.excutorCount.incrementAndGet();

		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					activity.execute(DependencyEngine.this.timestamp);
				} finally {
					DependencyEngine.this.executed.add(activity);
					DependencyEngine.this.excutorCount.decrementAndGet();
				}
			}
		});
	}

	public long getCycleCount() {
		return this.stepCount.get();
	}

	@Override
	// TODO add? or remove?
	public int getRate() {
		return 0;
	}

	public long getStartTime() {
		return this.simulationClock.getStartTimestamp();
	}

	private boolean isAlive() {
		synchronized (this.lifecycleLock) {
			return this.engineFuture != null && this.engineFuture.isCancelled() == false
					&& this.engineFuture.isDone() == false;
		}
	}

	@Override
	public void notifyIsReady(Activity activity) {
		execute(activity);
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
	public void run() {
		try {
			for (SimulationCycleListener l : this.simulationCycleListeners) {
				l.simulationStarts(this.timestamp);
			}
			while (!Thread.interrupted() && !this.simulationClock.simulationIsFinished()) {
				this.waitIfPaused();

				this.stepCount.incrementAndGet();

				if (logger.isInfoEnabled()) {
					logger.info("Simulation step #{} at {}", this.stepCount.get(), new Date(this.timestamp));
				}

				for (SimulationCycleListener l : this.simulationCycleListeners) {
					l.simulationCycleBegins(this.timestamp);
				}

				Queue<Activity> start = this.executed;
				this.executed = new ConcurrentLinkedQueue<Activity>();
				for (Activity activity : start) {
					if (activity.isWithoutDependencies()) {
						this.execute(activity);
					}
				}

				while (this.excutorCount.get() > 0) {
					Thread.yield();
				}

				for (SimulationCycleListener l : this.simulationCycleListeners) {
					l.simulationCycleFinishes(this.timestamp);
				}

				this.timestamp = this.simulationClock.getNextTimestamp();
			}
		} catch (InterruptedException e) {
			// interrupted, so stop
		} finally {
			for (SimulationCycleListener l : this.simulationCycleListeners) {
				l.simulationFinished();
			}
		}
	}

	@Override
	public void setDelay(long delay, TimeUnit unit) {
		this.delay = unit.toMillis(delay);
	}

	@Override
	public void start() {
		synchronized (this.lifecycleLock) {
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
	public void step() {
		synchronized (this.lifecycleLock) {
			this.lifecycleLock.notifyAll();
		}
	}

	@Override
	public void stop() {
		synchronized (this.lifecycleLock) {
			if (this.engineFuture == null) {
				return;
			}

			this.engineFuture.cancel(true);
			this.engineFuture = null;
			// TODO reset simulation?
		}
	}

	private void waitIfPaused() throws InterruptedException {
		if (this.pause.get()) {
			synchronized (this.lifecycleLock) {
				this.lifecycleLock.wait();
			}
		} else if (delay > 0) {
			Thread.sleep(delay);
		}
	}

	@Override
	public long getDelay() {
		return delay;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public SimulationState getState() {
		if (this.engineFuture == null) {
			return SimulationState.STOPPED;
		} else if (this.pause.get()) {
			return SimulationState.PAUSED;
		} else if (this.isAlive()) {
			return SimulationState.RUNNING;
		} else {
			return null;
		}
	}

}
