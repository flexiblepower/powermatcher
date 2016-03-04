package net.powermatcher.runtime;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.measure.Measurable;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The runtime implementation of {@link FlexiblePowerContext}. This implementation uses a
 * {@link ScheduledThreadPoolExecutor} in order to execute tasks. You would typically use a different implementation in
 * a simulated environment.
 */
public class PowerMatcherContext
    extends ScheduledThreadPoolExecutor
    implements FlexiblePowerContext {

    static final Unit<Duration> MS = SI.MILLI(SI.SECOND);

    private static final Logger logger = LoggerFactory.getLogger(PowerMatcherContext.class);

    /**
     * This class wraps a task and catches and logs exceptions that might occur. Normally, a scheduled tasks gets
     * cancelled when it throws an exception. Since PowerMatcher needs to be robust, we need to continue even if an
     * exception gets thrown.
     */
    static class WrappedTask<T>
        implements RunnableScheduledFuture<T> {
        private final RunnableScheduledFuture<T> task;

        public WrappedTask(RunnableScheduledFuture<T> task) {
            this.task = task;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return task.cancel(mayInterruptIfRunning);
        }

        @Override
        public int compareTo(Delayed o) {
            return task.compareTo(o);
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return task.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return task.get(timeout, unit);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return task.getDelay(unit);
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public boolean isDone() {
            return task.isDone();
        }

        @Override
        public boolean isPeriodic() {
            return task.isPeriodic();
        }

        @Override
        public void run() {
            try {
                task.run();
            } catch (Exception ex) {
                // The Exception is not thrown again to avoid this task being cancelled
                logger.error("An scheduled execution has thrown an exception: " + ex.getMessage(), ex);
            }
        }
    }

    public PowerMatcherContext() {
        // We provide a ThreadFactor so we can name the Threads, which makes debugging easier
        super(Runtime.getRuntime().availableProcessors() + 1, new ThreadFactory() {

            private final AtomicInteger cnt = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread newThread = Executors.defaultThreadFactory().newThread(r);
                newThread.setName("PowerMatcherRuntime-" + cnt.getAndIncrement());
                return newThread;
            }
        });
        setKeepAliveTime(5, TimeUnit.MINUTES);
    }

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(final Callable<V> callable,
                                                          final RunnableScheduledFuture<V> task) {
        return new WrappedTask<V>(task);
    };

    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
                                                          java.util.concurrent.RunnableScheduledFuture<V> task) {
        return new WrappedTask<V>(task);
    };

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Date currentTime() {
        return new Date(currentTimeMillis());
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, Measurable<Duration> delay) {
        return schedule(command, delay.longValue(MS), TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, Measurable<Duration> delay) {
        return schedule(callable, delay.longValue(MS), TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  Measurable<Duration> initialDelay,
                                                  Measurable<Duration> period) {
        return scheduleAtFixedRate(command,
                                   initialDelay.longValue(MS),
                                   period.longValue(MS),
                                   TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     Measurable<Duration> initialDelay,
                                                     Measurable<Duration> delay) {
        return scheduleWithFixedDelay(command,
                                      initialDelay.longValue(MS),
                                      delay.longValue(MS),
                                      TimeUnit.MILLISECONDS);
    }
}
