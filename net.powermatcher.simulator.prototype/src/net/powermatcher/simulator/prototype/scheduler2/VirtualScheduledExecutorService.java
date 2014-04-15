package net.powermatcher.simulator.prototype.scheduler2;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutor that runs in virtual time accelerated by a factor or
 * accelerated infinitely.
 * 
 * @author IBM
 */
public interface VirtualScheduledExecutorService extends ScheduledExecutorService {

	public void execute(Runnable command, int stage);

	public Future<?> submit(Runnable task, int stage);

	public <T> Future<T> submit(Runnable task, T result, int stage);

	public <T> Future<T> submit(Callable<T> task, int stage);

	/**
	 * @param <V>
	 * @param callable
	 * @param delay
	 * @param unit
	 * @param stage
	 * @return
	 */
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit, final int stage);

	/**
	 * @param command
	 * @param delay
	 * @param unit
	 * @param stage
	 * @return
	 */
	public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit, final int stage);

	/**
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @param stage
	 * @return
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
			final TimeUnit unit, final int stage);

	/**
	 * @param command
	 * @param initialDelay
	 * @param delay
	 * @param unit
	 * @param stage
	 * @return
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
			final TimeUnit unit, final int stage);

	/**
	 * @param period
	 * @param unit
	 */
	public void setTimeGrid(final long period, final TimeUnit unit);

	/**
	 * @return
	 */
	public Date simulatedTime();

	/**
	 * @return
	 */
	public long simulatedTimeMillis();

	/**
	 * 
	 */
	public void start();

	/**
	 * 
	 */
	public void start(long beginTimeMillis);

}
