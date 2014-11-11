package net.powermatcher.core.time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

@Component
public class LoggingScheduler implements ScheduledExecutorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingScheduler.class);

    private final ScheduledExecutorService executor;

    private static class CallableWrapper<T> implements Callable<T> {
        private final Callable<T> wrapped;

        public CallableWrapper(Callable<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public T call() {
            try {
                return wrapped.call();
            } catch (Exception ex) {
                LOGGER.error("Error while executing " + wrapped, ex);
                return null;
            }
        }
    }

    private static class RunnableWrapper implements Runnable {
        private final Runnable wrapped;

        public RunnableWrapper(Runnable wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void run() {
            try {
                new Thread(wrapped).start();
            } catch (Exception ex) {
                LOGGER.error("Error while executing " + wrapped, ex);
            }
        }
    }

    public LoggingScheduler() {
        executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);
    }

    @Deactivate
    public void deactivate() {
        executor.shutdownNow();
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(new CallableWrapper<T>(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(new RunnableWrapper(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(new RunnableWrapper(task));
    }

    private <T> List<Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> result = new ArrayList<Callable<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            result.add(new CallableWrapper<T>(task));
        }
        return result;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(wrap(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return executor.invokeAll(wrap(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executor.invokeAny(wrap(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(wrap(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(new RunnableWrapper(command));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(new RunnableWrapper(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executor.schedule(new CallableWrapper<V>(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(new RunnableWrapper(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executor.scheduleWithFixedDelay(new RunnableWrapper(command), initialDelay, delay, unit);
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        throw new SecurityException("The awaitTermination method may not be called by non-runtime components");
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public void shutdown() {
        throw new SecurityException("The shutdown method may not be called by non-runtime components");
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new SecurityException("The shutdownNow method may not be called by non-runtime components");
    }
}
