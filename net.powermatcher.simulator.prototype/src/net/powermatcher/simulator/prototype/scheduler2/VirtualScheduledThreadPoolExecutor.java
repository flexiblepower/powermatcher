package net.powermatcher.simulator.prototype.scheduler2;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VirtualScheduledThreadPoolExecutor extends ThreadPoolExecutor implements VirtualScheduledExecutorService {

	/*
	 * This class specializes ThreadPoolExecutor implementation by
	 * 
	 * 1. Using a custom task type, ScheduledFutureTask for tasks, even those
	 * that don't require scheduling (i.e., those submitted using
	 * ExecutorService execute, not ScheduledExecutorService methods) which are
	 * treated as delayed tasks with a delay of zero.
	 * 
	 * 2. Using a custom queue (DelayedWorkQueue), a variant of unbounded
	 * DelayQueue. The lack of capacity constraint and the fact that
	 * corePoolSize and maximumPoolSize are effectively identical simplifies
	 * some execution mechanics (see delayedExecute) compared to
	 * ThreadPoolExecutor.
	 * 
	 * 3. Supporting optional run-after-shutdown parameters, which leads to
	 * overrides of shutdown methods to remove and cancel tasks that should NOT
	 * be run after shutdown, as well as different recheck logic when task
	 * (re)submission overlaps with a shutdown.
	 * 
	 * 4. Task decoration methods to allow interception and instrumentation,
	 * which are needed because subclasses cannot otherwise override submit
	 * methods to get this effect. These don't have any impact on pool control
	 * logic though.
	 */

	/**
	 * False if should cancel/suppress periodic tasks on shutdown.
	 */
	private volatile boolean continueExistingPeriodicTasksAfterShutdown;

	/**
	 * False if should cancel non-periodic tasks on shutdown.
	 */
	private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

	/**
	 * True if ScheduledFutureTask.cancel should remove from queue
	 */
	private volatile boolean removeOnCancel = false;

	/**
	 * Sequence number to break scheduling ties, and in turn to guarantee FIFO
	 * order among tied entries.
	 */
	private static final AtomicLong sequencer = new AtomicLong(0);

	/**
	 * Returns current nanosecond time.
	 */
	// @final long now() {
	// @return System.nanoTime();
	// @}

	private class ScheduledFutureTask<V> extends FutureTask<V> implements StagedRunnableScheduledFuture<V> {

		/** Sequence number to break ties FIFO */
		private final long sequenceNumber;

		/** The time the task is enabled to execute in nanoTime units */
		private long time;

		private int stage; // @
		/**
		 * Period in nanoseconds for repeating tasks. A positive value indicates
		 * fixed-rate execution. A negative value indicates fixed-delay
		 * execution. A value of 0 indicates a non-repeating task.
		 */
		private final long period;

		/** The actual task to be re-enqueued by reExecutePeriodic */
		StagedRunnableScheduledFuture<V> outerTask = this;

		/**
		 * Index into delay queue, to support faster cancellation.
		 */
		int heapIndex;

		/**
		 * Creates a one-shot action with given nanoTime-based trigger time.
		 */
		ScheduledFutureTask(Runnable r, V result, long ns, int stage) {
			super(r, result);
			this.time = snapToGrid(timeGridNanos, ns); // @
			this.stage = stage;
			this.period = 0;
			this.sequenceNumber = sequencer.getAndIncrement();
		}

		/**
		 * Creates a periodic action with given nano time and period.
		 */
		ScheduledFutureTask(Runnable r, V result, long ns, long period, int stage) {
			super(r, result);
			this.time = snapToGrid(timeGridNanos, ns); // @
			this.stage = stage;
			this.period = period;
			this.sequenceNumber = sequencer.getAndIncrement();
		}

		/**
		 * Creates a one-shot action with given nanoTime-based trigger.
		 */
		ScheduledFutureTask(Callable<V> callable, long ns, int stage) {
			super(callable);
			this.time = snapToGrid(timeGridNanos, ns); // @
			this.stage = stage;
			this.period = 0;
			this.sequenceNumber = sequencer.getAndIncrement();
		}

		public long getDelay(TimeUnit unit) {
			return unit.convert(time - now(), TimeUnit.NANOSECONDS);
		}

		public int compareTo(Delayed other) {
			if (other == this) // compare zero ONLY if same object
				return 0;
			if (other instanceof ScheduledFutureTask) {
				ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
				long diff = time - x.time;
				if (diff < 0)
					return -1;
				else if (diff > 0)
					return 1;
				else if (this.stage > x.stage) // @
					return -1;
				else if (this.stage < x.stage) // @
					return 1;
				else if (sequenceNumber < x.sequenceNumber)
					return -1;
				else
					return 1;
			}
			long d = (getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS));
			return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
		}

		/**
		 * Returns true if this is a periodic (not a one-shot) action.
		 * 
		 * @return true if periodic
		 */
		public boolean isPeriodic() {
			return period != 0;
		}

		/**
		 * Sets the next time to run for a periodic task.
		 */
		private void setNextRunTime() {
			long p = period;
			if (p > 0)
				time += p;
			else
				time = triggerTime(-p);
			time = snapToGrid(timeGridNanos, time); // @
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			boolean cancelled = super.cancel(mayInterruptIfRunning);
			if (cancelled && removeOnCancel && heapIndex >= 0)
				remove(this);
			return cancelled;
		}

		/**
		 * Overrides FutureTask version so as to reset/requeue if periodic.
		 */
		public void run() {
			boolean periodic = isPeriodic();
			if (!canRunInCurrentRunState(periodic))
				cancel(false);
			else if (!periodic)
				ScheduledFutureTask.super.run();
			else if (ScheduledFutureTask.super.runAndReset()) {
				setNextRunTime();
				reExecutePeriodic(outerTask);
			}
		}

		// @
		@Override
		public int getStage() {
			return stage;
		}

		// @
		@Override
		public long getTime() {
			return time;
		}
	}

	/**
	 * Returns true if can run a task given current run state and
	 * run-after-shutdown parameters.
	 * 
	 * @param periodic
	 *            true if this task periodic, false if delayed
	 */
	boolean canRunInCurrentRunState(boolean periodic) {
		return isRunningOrShutdown(periodic ? continueExistingPeriodicTasksAfterShutdown
				: executeExistingDelayedTasksAfterShutdown);
	}

	/**
	 * Main execution method for delayed or periodic tasks. If pool is shut
	 * down, rejects the task. Otherwise adds task to queue and starts a thread,
	 * if necessary, to run it. (We cannot prestart the thread to run the task
	 * because the task (probably) shouldn't be run yet,) If the pool is shut
	 * down while the task is being added, cancel and remove it if required by
	 * state and run-after-shutdown parameters.
	 * 
	 * @param task
	 *            the task
	 */
	private void delayedExecute(RunnableScheduledFuture<?> task) {
		if (isShutdown())
			reject(task);
		else {
			super.getQueue().add(task);
			if (isShutdown() && !canRunInCurrentRunState(task.isPeriodic()) && remove(task))
				task.cancel(false);
			else
				ensurePrestart();
		}
	}

	/**
	 * Requeues a periodic task unless current run state precludes it. Same idea
	 * as delayedExecute except drops task rather than rejecting.
	 * 
	 * @param task
	 *            the task
	 */
	void reExecutePeriodic(RunnableScheduledFuture<?> task) {
		if (canRunInCurrentRunState(true)) {
			super.getQueue().add(task);
			if (!canRunInCurrentRunState(true) && remove(task))
				task.cancel(false);
			else
				ensurePrestart();
		}
	}

	/**
	 * Cancels and clears the queue of all tasks that should not be run due to
	 * shutdown policy. Invoked within super.shutdown.
	 */
	@Override
	void onShutdown() {
		BlockingQueue<Runnable> q = super.getQueue();
		boolean keepDelayed = getExecuteExistingDelayedTasksAfterShutdownPolicy();
		boolean keepPeriodic = getContinueExistingPeriodicTasksAfterShutdownPolicy();
		if (!keepDelayed && !keepPeriodic) {
			for (Object e : q.toArray())
				if (e instanceof RunnableScheduledFuture<?>)
					((RunnableScheduledFuture<?>) e).cancel(false);
			q.clear();
		} else {
			// Traverse snapshot to avoid iterator exceptions
			for (Object e : q.toArray()) {
				if (e instanceof RunnableScheduledFuture) {
					RunnableScheduledFuture<?> t = (RunnableScheduledFuture<?>) e;
					if ((t.isPeriodic() ? !keepPeriodic : !keepDelayed) || t.isCancelled()) { // also
																								// remove
																								// if
																								// already
																								// cancelled
						if (q.remove(t))
							t.cancel(false);
					}
				}
			}
		}
		tryTerminate();
	}

	/**
	 * Modifies or replaces the task used to execute a runnable. This method can
	 * be used to override the concrete class used for managing internal tasks.
	 * The default implementation simply returns the given task.
	 * 
	 * @param runnable
	 *            the submitted Runnable
	 * @param task
	 *            the task created to execute the runnable
	 * @return a task that can execute the runnable
	 * @since 1.6
	 */
	protected <V> StagedRunnableScheduledFuture<V> decorateTask(Runnable runnable, StagedRunnableScheduledFuture<V> task) {
		return task;
	}

	/**
	 * Modifies or replaces the task used to execute a callable. This method can
	 * be used to override the concrete class used for managing internal tasks.
	 * The default implementation simply returns the given task.
	 * 
	 * @param callable
	 *            the submitted Callable
	 * @param task
	 *            the task created to execute the callable
	 * @return a task that can execute the callable
	 * @since 1.6
	 */
	protected <V> StagedRunnableScheduledFuture<V> decorateTask(Callable<V> callable,
			StagedRunnableScheduledFuture<V> task) {
		return task;
	}

	/**
	 * Creates a new {@code ScheduledThreadPoolExecutor} with the given core
	 * pool size.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param rate
	 * @throws IllegalArgumentException
	 *             if {@code corePoolSize < 0}
	 */
	public VirtualScheduledThreadPoolExecutor(int corePoolSize, int rate) {
		super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new DelayedWorkQueue(rate));
	}

	/**
	 * Creates a new {@code ScheduledThreadPoolExecutor} with the given initial
	 * parameters.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param threadFactory
	 *            the factory to use when the executor creates a new thread
	 * @throws IllegalArgumentException
	 *             if {@code corePoolSize < 0}
	 * @throws NullPointerException
	 *             if {@code threadFactory} is null
	 */
	public VirtualScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, int rate) {
		super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new DelayedWorkQueue(rate), threadFactory);
	}

	/**
	 * Creates a new ScheduledThreadPoolExecutor with the given initial
	 * parameters.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param handler
	 *            the handler to use when execution is blocked because the
	 *            thread bounds and queue capacities are reached
	 * @throws IllegalArgumentException
	 *             if {@code corePoolSize < 0}
	 * @throws NullPointerException
	 *             if {@code handler} is null
	 */
	public VirtualScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler, int rate) {
		super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new DelayedWorkQueue(rate), handler);
	}

	/**
	 * Creates a new ScheduledThreadPoolExecutor with the given initial
	 * parameters.
	 * 
	 * @param corePoolSize
	 *            the number of threads to keep in the pool, even if they are
	 *            idle, unless {@code allowCoreThreadTimeOut} is set
	 * @param threadFactory
	 *            the factory to use when the executor creates a new thread
	 * @param handler
	 *            the handler to use when execution is blocked because the
	 *            thread bounds and queue capacities are reached
	 * @throws IllegalArgumentException
	 *             if {@code corePoolSize < 0}
	 * @throws NullPointerException
	 *             if {@code threadFactory} or {@code handler} is null
	 */
	public VirtualScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory,
			RejectedExecutionHandler handler, int rate) {
		super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new DelayedWorkQueue(rate), threadFactory,
				handler);
	}

	/**
	 * Returns the trigger time of a delayed action.
	 */
	private long triggerTime(long delay, TimeUnit unit) {
		return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
	}

	/**
	 * Returns the trigger time of a delayed action.
	 */
	long triggerTime(long delay) {
		return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
	}

	/**
	 * Constrains the values of all delays in the queue to be within
	 * Long.MAX_VALUE of each other, to avoid overflow in compareTo. This may
	 * occur if a task is eligible to be dequeued, but has not yet been, while
	 * some other task is added with a delay of Long.MAX_VALUE.
	 */
	private long overflowFree(long delay) {
		Delayed head = (Delayed) super.getQueue().peek();
		if (head != null) {
			long headDelay = head.getDelay(TimeUnit.NANOSECONDS);
			if (headDelay < 0 && (delay - headDelay < 0))
				delay = Long.MAX_VALUE + headDelay;
		}
		return delay;
	}

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit, int stage) {
		if (command == null || unit == null)
			throw new NullPointerException();
		RunnableScheduledFuture<?> t = decorateTask(command,
				new ScheduledFutureTask<Void>(command, null, triggerTime(delay, unit), stage));
		delayedExecute(t);
		return t;
	}

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit, int stage) {
		if (callable == null || unit == null)
			throw new NullPointerException();
		RunnableScheduledFuture<V> t = decorateTask(callable,
				new ScheduledFutureTask<V>(callable, triggerTime(delay, unit), stage));
		delayedExecute(t);
		return t;
	}

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit,
			int stage) {
		if (command == null || unit == null)
			throw new NullPointerException();
		if (period <= 0)
			throw new IllegalArgumentException();
		ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit),
				unit.toNanos(period), stage);
		StagedRunnableScheduledFuture<Void> t = decorateTask(command, sft);
		sft.outerTask = t;
		delayedExecute(t);
		return t;
	}

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 * @throws IllegalArgumentException
	 *             {@inheritDoc}
	 */
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit,
			int stage) {
		if (command == null || unit == null)
			throw new NullPointerException();
		if (delay <= 0)
			throw new IllegalArgumentException();
		ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit),
				unit.toNanos(-delay), stage);
		StagedRunnableScheduledFuture<Void> t = decorateTask(command, sft);
		sft.outerTask = t;
		delayedExecute(t);
		return t;
	}

	/**
	 * Executes {@code command} with zero required delay. This has effect
	 * equivalent to {@link #schedule(Runnable,long,TimeUnit) schedule(command,
	 * 0, anyUnit)}. Note that inspections of the queue and of the list returned
	 * by {@code shutdownNow} will access the zero-delayed
	 * {@link ScheduledFuture}, not the {@code command} itself.
	 * 
	 * <p>
	 * A consequence of the use of {@code ScheduledFuture} objects is that
	 * {@link ThreadPoolExecutor#afterExecute afterExecute} is always called
	 * with a null second {@code Throwable} argument, even if the
	 * {@code command} terminated abruptly. Instead, the {@code Throwable}
	 * thrown by such a task can be obtained via {@link Future#get}.
	 * 
	 * @throws RejectedExecutionException
	 *             at discretion of {@code RejectedExecutionHandler}, if the
	 *             task cannot be accepted for execution because the executor
	 *             has been shut down
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	public void execute(Runnable command, int stage) {
		schedule(command, 0, TimeUnit.NANOSECONDS, stage);
	}

	// Override AbstractExecutorService methods

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	public Future<?> submit(Runnable task, int stage) {
		return schedule(task, 0, TimeUnit.NANOSECONDS, stage);
	}

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	public <T> Future<T> submit(Runnable task, T result, int stage) {
		return schedule(Executors.callable(task, result), 0, TimeUnit.NANOSECONDS, stage);
	}

	/**
	 * @throws RejectedExecutionException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 */
	public <T> Future<T> submit(Callable<T> task, int stage) {
		return schedule(task, 0, TimeUnit.NANOSECONDS, stage);
	}

	/**
	 * Sets the policy on whether to continue executing existing periodic tasks
	 * even when this executor has been {@code shutdown}. In this case, these
	 * tasks will only terminate upon {@code shutdownNow} or after setting the
	 * policy to {@code false} when already shutdown. This value is by default
	 * {@code false}.
	 * 
	 * @param value
	 *            if {@code true}, continue after shutdown, else don't.
	 * @see #getContinueExistingPeriodicTasksAfterShutdownPolicy
	 */
	public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
		continueExistingPeriodicTasksAfterShutdown = value;
		if (!value && isShutdown())
			onShutdown();
	}

	/**
	 * Gets the policy on whether to continue executing existing periodic tasks
	 * even when this executor has been {@code shutdown}. In this case, these
	 * tasks will only terminate upon {@code shutdownNow} or after setting the
	 * policy to {@code false} when already shutdown. This value is by default
	 * {@code false}.
	 * 
	 * @return {@code true} if will continue after shutdown
	 * @see #setContinueExistingPeriodicTasksAfterShutdownPolicy
	 */
	public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
		return continueExistingPeriodicTasksAfterShutdown;
	}

	/**
	 * Sets the policy on whether to execute existing delayed tasks even when
	 * this executor has been {@code shutdown}. In this case, these tasks will
	 * only terminate upon {@code shutdownNow}, or after setting the policy to
	 * {@code false} when already shutdown. This value is by default
	 * {@code true}.
	 * 
	 * @param value
	 *            if {@code true}, execute after shutdown, else don't.
	 * @see #getExecuteExistingDelayedTasksAfterShutdownPolicy
	 */
	public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
		executeExistingDelayedTasksAfterShutdown = value;
		if (!value && isShutdown())
			onShutdown();
	}

	/**
	 * Gets the policy on whether to execute existing delayed tasks even when
	 * this executor has been {@code shutdown}. In this case, these tasks will
	 * only terminate upon {@code shutdownNow}, or after setting the policy to
	 * {@code false} when already shutdown. This value is by default
	 * {@code true}.
	 * 
	 * @return {@code true} if will execute after shutdown
	 * @see #setExecuteExistingDelayedTasksAfterShutdownPolicy
	 */
	public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
		return executeExistingDelayedTasksAfterShutdown;
	}

	/**
	 * Sets the policy on whether cancelled tasks should be immediately removed
	 * from the work queue at time of cancellation. This value is by default
	 * {@code false}.
	 * 
	 * @param value
	 *            if {@code true}, remove on cancellation, else don't
	 * @see #getRemoveOnCancelPolicy
	 * @since 1.7
	 */
	public void setRemoveOnCancelPolicy(boolean value) {
		removeOnCancel = value;
	}

	/**
	 * Gets the policy on whether cancelled tasks should be immediately removed
	 * from the work queue at time of cancellation. This value is by default
	 * {@code false}.
	 * 
	 * @return {@code true} if cancelled tasks are immediately removed from the
	 *         queue
	 * @see #setRemoveOnCancelPolicy
	 * @since 1.7
	 */
	public boolean getRemoveOnCancelPolicy() {
		return removeOnCancel;
	}

	/**
	 * Initiates an orderly shutdown in which previously submitted tasks are
	 * executed, but no new tasks will be accepted. Invocation has no additional
	 * effect if already shut down.
	 * 
	 * <p>
	 * This method does not wait for previously submitted tasks to complete
	 * execution. Use {@link #awaitTermination awaitTermination} to do that.
	 * 
	 * <p>
	 * If the {@code ExecuteExistingDelayedTasksAfterShutdownPolicy} has been
	 * set {@code false}, existing delayed tasks whose delays have not yet
	 * elapsed are cancelled. And unless the
	 * {@code ContinueExistingPeriodicTasksAfterShutdownPolicy} has been set
	 * {@code true}, future executions of existing periodic tasks will be
	 * cancelled.
	 * 
	 * @throws SecurityException
	 *             {@inheritDoc}
	 */
	public void shutdown() {
		super.shutdown();
	}

	/**
	 * Attempts to stop all actively executing tasks, halts the processing of
	 * waiting tasks, and returns a list of the tasks that were awaiting
	 * execution.
	 * 
	 * <p>
	 * This method does not wait for actively executing tasks to terminate. Use
	 * {@link #awaitTermination awaitTermination} to do that.
	 * 
	 * <p>
	 * There are no guarantees beyond best-effort attempts to stop processing
	 * actively executing tasks. This implementation cancels tasks via
	 * {@link Thread#interrupt}, so any task that fails to respond to interrupts
	 * may never terminate.
	 * 
	 * @return list of tasks that never commenced execution. Each element of
	 *         this list is a {@link ScheduledFuture}, including those tasks
	 *         submitted using {@code execute}, which are for scheduling
	 *         purposes used as the basis of a zero-delay
	 *         {@code ScheduledFuture}.
	 * @throws SecurityException
	 *             {@inheritDoc}
	 */
	public List<Runnable> shutdownNow() {
		return super.shutdownNow();
	}

	/**
	 * Returns the task queue used by this executor. Each element of this queue
	 * is a {@link ScheduledFuture}, including those tasks submitted using
	 * {@code execute} which are for scheduling purposes used as the basis of a
	 * zero-delay {@code ScheduledFuture}. Iteration over this queue is
	 * <em>not</em> guaranteed to traverse tasks in the order in which they will
	 * execute.
	 * 
	 * @return the task queue
	 */
	public BlockingQueue<Runnable> getQueue() {
		return super.getQueue();
	}

	/**
	 * Specialized delay queue. To mesh with TPE declarations, this class must
	 * be declared as a BlockingQueue<Runnable> even though it can only hold
	 * RunnableScheduledFutures.
	 */
	static class DelayedWorkQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

		/*
		 * A DelayedWorkQueue is based on a heap-based data structure like those
		 * in DelayQueue and PriorityQueue, except that every
		 * ScheduledFutureTask also records its index into the heap array. This
		 * eliminates the need to find a task upon cancellation, greatly
		 * speeding up removal (down from O(n) to O(log n)), and reducing
		 * garbage retention that would otherwise occur by waiting for the
		 * element to rise to top before clearing. But because the queue may
		 * also hold RunnableScheduledFutures that are not ScheduledFutureTasks,
		 * we are not guaranteed to have such indices available, in which case
		 * we fall back to linear search. (We expect that most tasks will not be
		 * decorated, and that the faster cases will be much more common.)
		 * 
		 * All heap operations must record index changes -- mainly within siftUp
		 * and siftDown. Upon removal, a task's heapIndex is set to -1. Note
		 * that ScheduledFutureTasks can appear at most once in the queue (this
		 * need not be true for other kinds of tasks or work queues), so are
		 * uniquely identified by heapIndex.
		 */

		private static final int INITIAL_CAPACITY = 16;
		private StagedRunnableScheduledFuture[] queue = new StagedRunnableScheduledFuture[INITIAL_CAPACITY];
		private final ReentrantLock lock = new ReentrantLock();
		private int size = 0;
		private int rate; // @
		private long timeGridNanos; // @
		private int activeThreadCount; // @
		private int currentStage;

		/**
		 * Thread designated to wait for the task at the head of the queue. This
		 * variant of the Leader-Follower pattern
		 * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to minimize
		 * unnecessary timed waiting. When a thread becomes the leader, it waits
		 * only for the next delay to elapse, but other threads await
		 * indefinitely. The leader thread must signal some other thread before
		 * returning from take() or poll(...), unless some other thread becomes
		 * leader in the interim. Whenever the head of the queue is replaced
		 * with a task with an earlier expiration time, the leader field is
		 * invalidated by being reset to null, and some waiting thread, but not
		 * necessarily the current leader, is signalled. So waiting threads must
		 * be prepared to acquire and lose leadership while waiting.
		 */
		private Thread leader = null;

		/**
		 * Condition signalled when a newer task becomes available at the head
		 * of the queue or a new thread may need to become leader.
		 */
		private final Condition available = lock.newCondition();

		/**
		 * Set f's heapIndex if it is a ScheduledFutureTask.
		 */
		private void setIndex(RunnableScheduledFuture f, int idx) {
			if (f instanceof ScheduledFutureTask)
				((ScheduledFutureTask) f).heapIndex = idx;
		}

		/**
		 * Sift element added at bottom up to its heap-ordered spot. Call only
		 * when holding lock.
		 */
		private void siftUp(int k, StagedRunnableScheduledFuture key) {
			while (k > 0) {
				int parent = (k - 1) >>> 1;
				StagedRunnableScheduledFuture e = queue[parent];
				if (key.compareTo(e) >= 0)
					break;
				queue[k] = e;
				setIndex(e, k);
				k = parent;
			}
			queue[k] = key;
			setIndex(key, k);
		}

		/**
		 * Sift element added at top down to its heap-ordered spot. Call only
		 * when holding lock.
		 */
		private void siftDown(int k, StagedRunnableScheduledFuture key) {
			int half = size >>> 1;
			while (k < half) {
				int child = (k << 1) + 1;
				StagedRunnableScheduledFuture c = queue[child];
				int right = child + 1;
				if (right < size && c.compareTo(queue[right]) > 0)
					c = queue[child = right];
				if (key.compareTo(c) <= 0)
					break;
				queue[k] = c;
				setIndex(c, k);
				k = child;
			}
			queue[k] = key;
			setIndex(key, k);
		}

		/**
		 * Resize the heap array. Call only when holding lock.
		 */
		private void grow() {
			int oldCapacity = queue.length;
			int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
			if (newCapacity < 0) // overflow
				newCapacity = Integer.MAX_VALUE;
			queue = Arrays.copyOf(queue, newCapacity);
		}

		/**
		 * Find index of given object, or -1 if absent
		 */
		private int indexOf(Object x) {
			if (x != null) {
				if (x instanceof ScheduledFutureTask) {
					int i = ((ScheduledFutureTask) x).heapIndex;
					// Sanity check; x could conceivably be a
					// ScheduledFutureTask from some other pool.
					if (i >= 0 && i < size && queue[i] == x)
						return i;
				} else {
					for (int i = 0; i < size; i++)
						if (x.equals(queue[i]))
							return i;
				}
			}
			return -1;
		}

		public boolean contains(Object x) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return indexOf(x) != -1;
			} finally {
				lock.unlock();
			}
		}

		public boolean remove(Object x) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = indexOf(x);
				if (i < 0)
					return false;

				setIndex(queue[i], -1);
				int s = --size;
				StagedRunnableScheduledFuture replacement = queue[s];
				queue[s] = null;
				if (s != i) {
					siftDown(i, replacement);
					if (queue[i] == replacement)
						siftUp(i, replacement);
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		public int size() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return size;
			} finally {
				lock.unlock();
			}
		}

		public boolean isEmpty() {
			return size() == 0;
		}

		public int remainingCapacity() {
			return Integer.MAX_VALUE;
		}

		public RunnableScheduledFuture peek() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return queue[0];
			} finally {
				lock.unlock();
			}
		}

		public boolean offer(Runnable x) {
			if (x == null)
				throw new NullPointerException();
			StagedRunnableScheduledFuture e = (StagedRunnableScheduledFuture) x;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				int i = size;
				if (i >= queue.length)
					grow();
				size = i + 1;
				if (i == 0) {
					queue[0] = e;
					setIndex(e, 0);
				} else {
					siftUp(i, e);
				}
				if (queue[0] == e) {
					leader = null;
					available.signal();
				}
			} finally {
				lock.unlock();
			}
			return true;
		}

		public void put(Runnable e) {
			offer(e);
		}

		public boolean add(Runnable e) {
			return offer(e);
		}

		public boolean offer(Runnable e, long timeout, TimeUnit unit) {
			return offer(e);
		}

		/**
		 * Performs common bookkeeping for poll and take: Replaces first element
		 * with last and sifts it down. Call only when holding lock.
		 * 
		 * @param f
		 *            the task to remove and return
		 */
		private StagedRunnableScheduledFuture finishPoll(StagedRunnableScheduledFuture f) {
			int s = --size;
			StagedRunnableScheduledFuture x = queue[s];
			queue[s] = null;
			if (s != 0)
				siftDown(0, x);
			setIndex(f, -1);
			currentStage = f.getStage(); // @
			return f;
		}

		public StagedRunnableScheduledFuture poll() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				StagedRunnableScheduledFuture first = queue[0];
				if (first == null || finishStage(first) || first.getDelay(TimeUnit.NANOSECONDS) > 0)
					return null;
				else {
					incrementActiveThreadCount();// @
					return finishPoll(first);
				}
			} finally {
				lock.unlock();
			}
		}

		public StagedRunnableScheduledFuture take() throws InterruptedException {
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					StagedRunnableScheduledFuture first = queue[0];
					if (first == null || finishStage(first))
						available.await();
					else {
						long delay = first.getDelay(TimeUnit.NANOSECONDS);
						if (delay <= 0 || rate == 0) { // @
							incrementActiveThreadCount();// @
							return finishPoll(first);
						} else if (leader != null)
							available.await();
						else {
							Thread thisThread = Thread.currentThread();
							leader = thisThread;
							try {
								long scaledDelay = snapToGrid(timeGridNanos, delay) / rate; // @
								available.awaitNanos(scaledDelay);
							} finally {
								if (leader == thisThread)
									leader = null;
							}
						}
					}
				}
			} finally {
				if (leader == null && queue[0] != null)
					available.signal();
				lock.unlock();
			}
		}

		public StagedRunnableScheduledFuture poll(long timeout, TimeUnit unit) throws InterruptedException {
			long nanos = unit.toNanos(timeout);
			final ReentrantLock lock = this.lock;
			lock.lockInterruptibly();
			try {
				for (;;) {
					StagedRunnableScheduledFuture first = queue[0];
					if (first == null || finishStage(first)) {
						if (nanos <= 0)
							return null;
						else
							nanos = available.awaitNanos(nanos);
					} else {
						long delay = first.getDelay(TimeUnit.NANOSECONDS);
						if (delay <= 0 || rate == 0) { // @
							incrementActiveThreadCount();// @
							return finishPoll(first);
						}
						if (nanos <= 0)
							return null;
						if (nanos < delay || leader != null)
							nanos = available.awaitNanos(nanos);
						else {
							Thread thisThread = Thread.currentThread();
							leader = thisThread;
							try {
								long scaledDelay = snapToGrid(timeGridNanos, delay) / rate; // @
								long timeLeft = available.awaitNanos(scaledDelay);
								nanos -= delay - timeLeft * rate;
							} finally {
								if (leader == thisThread)
									leader = null;
							}
						}
					}
				}
			} finally {
				if (leader == null && queue[0] != null)
					available.signal();
				lock.unlock();
			}
		}

		public void clear() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				for (int i = 0; i < size; i++) {
					RunnableScheduledFuture t = queue[i];
					if (t != null) {
						queue[i] = null;
						setIndex(t, -1);
					}
				}
				size = 0;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * Return and remove first element only if it is expired. Used only by
		 * drainTo. Call only when holding lock.
		 */
		private StagedRunnableScheduledFuture pollExpired() {
			StagedRunnableScheduledFuture first = queue[0];
			if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0)
				return null;
			return finishPoll(first);
		}

		public int drainTo(Collection<? super Runnable> c) {
			if (c == null)
				throw new NullPointerException();
			if (c == this)
				throw new IllegalArgumentException();
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				StagedRunnableScheduledFuture first;
				int n = 0;
				while ((first = pollExpired()) != null) {
					c.add(first);
					++n;
				}
				return n;
			} finally {
				lock.unlock();
			}
		}

		public int drainTo(Collection<? super Runnable> c, int maxElements) {
			if (c == null)
				throw new NullPointerException();
			if (c == this)
				throw new IllegalArgumentException();
			if (maxElements <= 0)
				return 0;
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				StagedRunnableScheduledFuture first;
				int n = 0;
				while (n < maxElements && (first = pollExpired()) != null) {
					c.add(first);
					++n;
				}
				return n;
			} finally {
				lock.unlock();
			}
		}

		public Object[] toArray() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				return Arrays.copyOf(queue, size, Object[].class);
			} finally {
				lock.unlock();
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T[] toArray(T[] a) {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				if (a.length < size)
					return (T[]) Arrays.copyOf(queue, size, a.getClass());
				System.arraycopy(queue, 0, a, 0, size);
				if (a.length > size)
					a[size] = null;
				return a;
			} finally {
				lock.unlock();
			}
		}

		public Iterator<Runnable> iterator() {
			return new Itr(Arrays.copyOf(queue, size));
		}

		/**
		 * Snapshot iterator that works off copy of underlying q array.
		 */
		private class Itr implements Iterator<Runnable> {
			final RunnableScheduledFuture[] array;
			int cursor = 0; // index of next element to return
			int lastRet = -1; // index of last element, or -1 if no such

			Itr(RunnableScheduledFuture[] array) {
				this.array = array;
			}

			public boolean hasNext() {
				return cursor < array.length;
			}

			public Runnable next() {
				if (cursor >= array.length)
					throw new NoSuchElementException();
				lastRet = cursor;
				return array[cursor++];
			}

			public void remove() {
				if (lastRet < 0)
					throw new IllegalStateException();
				DelayedWorkQueue.this.remove(array[lastRet]);
				lastRet = -1;
			}
		}

		// @ =========================================

		DelayedWorkQueue(int rate) {
			this.rate = rate;
		}

		public void setTimeGridNanos(long timeGridNanos) {
			this.timeGridNanos = timeGridNanos;
		}

		public int getRate() {
			return rate;
		}

		private void incrementActiveThreadCount() {
			activeThreadCount++;
		}

		private void decrementActiveThreadCount() {
			final ReentrantLock lock = this.lock;
			lock.lock();
			try {
				activeThreadCount--;// @
			} finally {
				if (leader == null && queue[0] != null)
					available.signal();
				lock.unlock();
			}
		}

		private boolean finishStage(StagedRunnableScheduledFuture first) {
			return activeThreadCount > 0 && first.getStage() != currentStage;
		}
		// @ =========================================

	}

	// @ =========================================

	private DelayedWorkQueue delayedWorkQueue;
	private boolean started;
	private long beginTimeMillis;
	private long timeGridNanos;
	private long startUpNanoTime;
	private AtomicLong simulatedNanoTime = new AtomicLong();

	private AtomicLong cycleCount = new AtomicLong();

	private DelayedWorkQueue getDelayedWorkQueue() {
		return (DelayedWorkQueue) super.getQueue();
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		StagedRunnableScheduledFuture<?> task = (StagedRunnableScheduledFuture) r;
		long newTime = task.getTime();
		boolean done;
		do {
			long currentTime = simulatedNanoTime.get();
			if (newTime > currentTime) {
				done = simulatedNanoTime.compareAndSet(currentTime, newTime);
				if (done){
					cycleCount.incrementAndGet();
				}
			} else {
				done = true;
			}
		} while (!done);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		getDelayedWorkQueue().decrementActiveThreadCount();
	}

	@Override
	public void execute(Runnable command) {
		execute(command, 0);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(final Callable<V> callable, final long delay, final TimeUnit unit) {
		return schedule(callable, delay, unit, 0);
	}

	@Override
	public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
		return schedule(command, delay, unit, 0);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
			final TimeUnit unit) {
		return scheduleAtFixedRate(command, initialDelay, period, unit, 0);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
			final TimeUnit unit) {
		return scheduleWithFixedDelay(command, initialDelay, delay, unit, 0);
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		return submit(task, 0);
	}

	@Override
	public Future<?> submit(final Runnable task) {
		return submit(task, 0);
	}

	@Override
	public <T> Future<T> submit(final Runnable task, final T result) {
		return submit(task, result, 0);
	}

	@Override
	void ensurePrestart() {
		if (started) {
			super.ensurePrestart();
		}
	}

	public void setTimeGrid(final long period, final TimeUnit unit) {
		this.timeGridNanos = TimeUnit.NANOSECONDS.convert(period, unit);
		getDelayedWorkQueue().setTimeGridNanos(this.timeGridNanos);
	}

	public void start() {
		start(System.currentTimeMillis());
	}

	public void start(long beginTimeMillis) {
		this.startUpNanoTime = System.nanoTime();
		this.beginTimeMillis = beginTimeMillis;
		this.started = true;
		super.ensurePrestart();
	}

	public Date simulatedTime() {
		return new Date(simulatedTimeMillis());
	}

	public long simulatedTimeMillis() {
		return this.beginTimeMillis + TimeUnit.MILLISECONDS.convert(now(), TimeUnit.NANOSECONDS);
	}

	final long now() {
		int rate = getDelayedWorkQueue().getRate();
		if (rate == 0) {
			return this.simulatedNanoTime.get();
		}
		if (started) {
			long simulatedElapsed = (System.nanoTime() - this.startUpNanoTime) * rate;
			return snapToGrid(this.timeGridNanos, simulatedElapsed);
		}
		return 0;
	}

	private static long snapToGrid(long grid, long value) {
		if (grid != 0) {
			value += grid / 2;
			value -= value % grid;
		}
		return value;
	}

	public long getCycleCount() {
		return this.cycleCount.get();
	}

}
