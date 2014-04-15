package net.powermatcher.simulation.engine.dependencyengine;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class DelegatingScheduledExecutorService implements ScheduledExecutorService {
	protected abstract ScheduledExecutorService getDelegateForCommand(Object command);

	@Override
	public void shutdown() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Runnable> shutdownNow() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isTerminated() {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return getDelegateForCommand(task).submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return getDelegateForCommand(task).submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return getDelegateForCommand(task).submit(task);
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
	public void execute(Runnable command) {
		getDelegateForCommand(command).execute(command);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return getDelegateForCommand(command).schedule(command, delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return getDelegateForCommand(callable).schedule(callable, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return getDelegateForCommand(command).scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return getDelegateForCommand(command).scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}
}
