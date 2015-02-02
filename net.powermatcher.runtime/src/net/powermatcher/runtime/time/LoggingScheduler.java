package net.powermatcher.runtime.time;

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

import aQute.bnd.annotation.component.Deactivate;

/**
 * {@link LoggingScheduler} is an implementation of {@link ScheduledExecutorService}. It is used by all Powermatcher
 * instances that have to perform periodic tasks.
 *
 * @author FAN
 * @version 2.0
 */
public class LoggingScheduler
    implements ScheduledExecutorService {
    private static final Logger LOGGER = LoggerFactory
                                                      .getLogger(LoggingScheduler.class);

    /**
     * the {@link ScheduledExecutorService} that will be used to perform the periodic tasks.
     */
    private final ScheduledExecutorService executor;

    /**
     * A private wrapper class for {@link Callable} tasks.
     * 
     * @author FAN
     * @version 2.0
     */
    private static class CallableWrapper<T>
        implements Callable<T> {

        /**
         * The wrapped {@link Runnable} that will run.
         */
        private final Callable<T> wrapped;

        /**
         * A constructor to create an instance of this class.
         * 
         * @param wrapped
         *            the {@link Callable} that will be used.
         */
        public CallableWrapper(Callable<T> wrapped) {
            this.wrapped = wrapped;
        }

        /**
         * {@inheritDoc}
         */
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

    /**
     * A private wrapper class for {@link Runnable} tasks.
     * 
     * @author FAN
     * @version 2.0
     */
    private static class RunnableWrapper
        implements Runnable {

        /**
         * The wrapped {@link Runnable} that will run.
         */
        private final Runnable wrapped;

        /**
         * A constructor to create an instance of this class.
         * 
         * @param wrapped
         *            the {@link Callable} that will be used.
         */
        public RunnableWrapper(Runnable wrapped) {
            this.wrapped = wrapped;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                wrapped.run();
            } catch (Exception ex) {
                LOGGER.error("Error while executing " + wrapped, ex);
            }
        }
    }

    /**
     * A constructor to create an instance of this class.
     */
    public LoggingScheduler() {
        executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime()
                                                          .availableProcessors());
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        executor.shutdownNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(new CallableWrapper<T>(task));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(new RunnableWrapper(task), result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(new RunnableWrapper(task));
    }

    /**
     * Takes the tasks parameter and wraps all the {@link Callable}s in a {@link CallableWrapper}
     * 
     * @param tasks
     *            the collection of tasks that have to be performed.
     * @return A list of {@link CallableWrapper}s
     */
    private <T> List<Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> result = new ArrayList<Callable<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            result.add(new CallableWrapper<T>(task));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                                                                                 throws InterruptedException {
        return executor.invokeAll(wrap(tasks));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T>
            List<Future<T>>
            invokeAll(
                      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                                                                                           throws InterruptedException {
        return executor.invokeAll(wrap(tasks), timeout, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
                                                                   throws InterruptedException, ExecutionException {
        return executor.invokeAny(wrap(tasks));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                           long timeout, TimeUnit unit) throws InterruptedException,
                                                       ExecutionException, TimeoutException {
        return executor.invokeAny(wrap(tasks), timeout, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Runnable command) {
        executor.execute(new RunnableWrapper(command));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay,
                                       TimeUnit unit) {
        return executor.schedule(new RunnableWrapper(command), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
                                           TimeUnit unit) {
        return executor.schedule(new CallableWrapper<V>(callable), delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(new RunnableWrapper(command),
                                            initialDelay, period, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay, long delay, TimeUnit unit) {
        return executor.scheduleWithFixedDelay(new RunnableWrapper(command),
                                               initialDelay, delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
                                                                throws InterruptedException {
        throw new SecurityException(
                                    "The awaitTermination method may not be called by non-runtime components");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        throw new SecurityException(
                                    "The shutdown method may not be called by non-runtime components");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Runnable> shutdownNow() {
        throw new SecurityException(
                                    "The shutdownNow method may not be called by non-runtime components");
    }
}
