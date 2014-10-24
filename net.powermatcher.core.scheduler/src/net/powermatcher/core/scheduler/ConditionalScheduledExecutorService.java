package net.powermatcher.core.scheduler;


import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.powermatcher.core.scheduler.service.TimeServicable;

/**
 * ThreadPoolExecutor that runs in virtual time accelerated by a factor or
 * accelerated infinitely.
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ConditionalScheduledExecutorService extends ScheduledExecutorService, TimeServicable {

	/**
	 * 
	 */
	public static final int NO_AFFINITY = -1;

	/*
	 *  The standard scheduling methods create a task is initially ready to run.
	 *  The ready to run status can be changed at any time before the execution
	 *  of the task.
	 */
	/**
	 * @param command
	 * @param affinity
	 * @return
	 */
	public RunnableScheduledFuture<?> execute(Runnable command, int affinity);

	/**
	 * @param task
	 * @param affinity
	 * @return
	 */
	public Future<?> submit(Runnable task, int affinity);

	/**
	 * @param <T>
	 * @param task
	 * @param result
	 * @param affinity
	 * @return
	 */
	public <T> Future<T> submit(Runnable task, T result, int affinity);

	/**
	 * @param <T>
	 * @param task
	 * @param affinity
	 * @return
	 */
	public <T> Future<T> submit(Callable<T> task, int affinity);

	/**
	 * @param <V>
	 * @param callable
	 * @param delay
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public <V> ConditionalRunnableScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit, final int affinity);

	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit, final int affinity);

	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit, final int affinity);

	/**
	 * @param command
	 * @param initialDelay
	 * @param delay
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit, final int affinity);


	/*
	 *  The conditional scheduling methods create a task is initially not ready to run.
	 *  The ready to run status can be changed at any time.
	 */
	/**
	 * @param command
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> executeConditional(Runnable command, int affinity);

	/**
	 * @param task
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> submitConditional(Runnable task, int affinity);

	/**
	 * @param <T>
	 * @param task
	 * @param result
	 * @param affinity
	 * @return
	 */
	public <T> ConditionalRunnableScheduledFuture<T> submitConditional(Runnable task, T result, int affinity);

	/**
	 * @param <T>
	 * @param task
	 * @param affinity
	 * @return
	 */
	public <T> ConditionalRunnableScheduledFuture<T> submitConditional(Callable<T> task, int affinity);

	/**
	 * @param <V>
	 * @param callable
	 * @param delay
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public <V> ConditionalRunnableScheduledFuture<V> scheduleConditional(final Callable<V> callable, final long delay, final TimeUnit unit, final int affinity);

	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> scheduleConditional(final Runnable command, final long delay, final TimeUnit unit, final int affinity);

	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> scheduleConditionalAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit, final int affinity);

	/**
	 * @param command
	 * @param initialDelay
	 * @param delay
	 * @param unit
	 * @param affinity
	 * @return
	 */
	public ConditionalRunnableScheduledFuture<?> scheduleConditionalWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit, final int affinity);

	/* Simulated time and simulation rate */

	/* (non-Javadoc)
	 * @see net.powermatcher.core.scheduler.service.TimeService#currentTimeMillis()
	 */
	public long currentTimeMillis();

	/* (non-Javadoc)
	 * @see net.powermatcher.core.scheduler.service.TimeService#getRate()
	 */
	public int getRate();
}
