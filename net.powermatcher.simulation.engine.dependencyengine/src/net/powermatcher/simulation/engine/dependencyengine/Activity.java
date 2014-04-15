package net.powermatcher.simulation.engine.dependencyengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class Activity implements Link.ReadyListener, ScheduledExecutorService {

	public interface ReadyListener {
		void notifyIsReady(Activity activity);
	}

	private final List<Link> dependencies = new CopyOnWriteArrayList<Link>();

	private final List<ExecutedListener> executedListeners = new CopyOnWriteArrayList<ExecutedListener>();
	private final String name;

	private long next = Long.MAX_VALUE;

	private long period;
	private final AtomicLong readyCount = new AtomicLong();

	private ReadyListener readyListener;

	private List<Runnable> runnables = new ArrayList<Runnable>(1);

	public Activity(String name) {
		this.name = name;
	}

	/** makes this activity dependent on the given link */
	public void setDependentOn(Link link) {
		dependencies.add(link);
		link.setReadyListener(this);
	}

	public void addExecutedListener(ExecutedListener listener) {
		executedListeners.add(listener);
	}

	void execute(long timestamp) {
		for (Link link : dependencies) {
			link.release();
		}

		if (runnables != null && next <= timestamp) {
			// System.out.println("Activity " + this.name +
			// " executed with runnable");
			for (Runnable runnable : runnables) {
				runnable.run();
			}

			next += period;
		} else {
			// System.out.println("Activity " + this.name + " executed");
		}

		readyCount.set(0);

		for (ExecutedListener executedListener : executedListeners) {
			executedListener.notifyIsExecuted(this);
		}
	}

	boolean isWithoutDependencies() {
		return dependencies.size() == 0;
	}

	@Override
	public void notifyIsReady(Link link) {
		if (readyListener != null && readyCount.incrementAndGet() == dependencies.size()) {
			readyListener.notifyIsReady(this);
		}
	}

	public void setReadyListener(ReadyListener listener) {
		if (this.readyListener != null) {
			throw new IllegalStateException();
		}

		this.readyListener = listener;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, long initialDelay, long period, TimeUnit unit) {
		this.runnables.add(command);

		// TODO initialDelay is incorrectly interpreted
		// in previous versions a start time was used (i.e. this.startTime + initialDelay)
		this.next = initialDelay;
		this.period = unit.toMillis(period);

		return new ScheduledFuture<Void>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return runnables.remove(command);
			}

			@Override
			public boolean isCancelled() {
				return runnables.contains(command);
			}

			@Override
			public long getDelay(TimeUnit unit) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int compareTo(Delayed other) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isDone() {
				return isCancelled();
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				throw new UnsupportedOperationException();
			}

			@Override
			public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
					TimeoutException {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTerminated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Future<?> submit(Runnable task) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void execute(Runnable command) {
		throw new UnsupportedOperationException();
	}
}
