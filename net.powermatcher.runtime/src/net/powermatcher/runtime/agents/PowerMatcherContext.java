package net.powermatcher.runtime.agents;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.measure.Measurable;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PowerMatcherContext
    implements FlexiblePowerContext {
    static final Logger logger = LoggerFactory.getLogger(PowerMatcherContext.class);
    static final Unit<Duration> MS = SI.MILLI(SI.SECOND);

    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    static class WrappedCallable<T>
        implements Callable<T> {
        private final Callable<T> wrapped;

        public WrappedCallable(Callable<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public T call() throws Exception {
            try {
                return wrapped.call();
            } catch (Exception ex) {
                logger.error("An scheduled execution has thrown an exception: " + ex.getMessage(), ex);
                throw ex;
            }
        }
    }

    static class WrappedRunnable
        implements Runnable {
        private final Runnable wrapped;

        public WrappedRunnable(Runnable wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void run() {
            try {
                wrapped.run();
            } catch (RuntimeException ex) {
                logger.error("An scheduled execution has thrown an exception: " + ex.getMessage(), ex);
                throw ex;
            }
        }
    }

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Date currentTime() {
        return new Date(currentTimeMillis());
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(new WrappedCallable<T>(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(new WrappedRunnable(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(new WrappedRunnable(task));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, Measurable<Duration> delay) {
        return executor.schedule(new WrappedRunnable(command), delay.longValue(MS), TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, Measurable<Duration> delay) {
        return executor.schedule(new WrappedCallable<V>(callable), delay.longValue(MS), TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  Measurable<Duration> initialDelay,
                                                  Measurable<Duration> period) {
        return executor.scheduleAtFixedRate(new WrappedRunnable(command),
                                            initialDelay.longValue(MS),
                                            period.longValue(MS),
                                            TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     Measurable<Duration> initialDelay,
                                                     Measurable<Duration> delay) {
        return executor.scheduleWithFixedDelay(new WrappedRunnable(command),
                                               initialDelay.longValue(MS),
                                               delay.longValue(MS),
                                               TimeUnit.MILLISECONDS);
    }
}
