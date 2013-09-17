package net.powermatcher.fpai.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.flexiblepower.time.TimeService;

public class MockScheduledExecutor implements ScheduledExecutorService {
    private final BlockingQueue<FutureTask<?>> runnables = new LinkedBlockingQueue<FutureTask<?>>();

    private final TimeService timeService;
    private boolean shutdown;

    public MockScheduledExecutor(TimeService timeService) {
        this.timeService = timeService;
    }

    /** executes all scheduled tasks (if they are due to run given the time provided by the time service) */
    public synchronized void executePending() {
        Iterator<FutureTask<?>> tasksIterator = runnables.iterator();
        while (tasksIterator.hasNext()) {
            FutureTask<?> task = tasksIterator.next();

            if (task instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> scheduledTask = (ScheduledFutureTask<?>) task;

                if (scheduledTask.getDelay(TimeUnit.NANOSECONDS) <= 0) {
                    scheduledTask.run();

                    if (scheduledTask.isPeriodic() == false || scheduledTask.isDone() || scheduledTask.isCancelled()) {
                        tasksIterator.remove();
                    }
                }
            } else {
                task.run();
                tasksIterator.remove();
            }
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return submit(new FutureTask<T>(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return submit(new FutureTask<T>(task, result));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return submit(new FutureTask<Void>(task, null));
    }

    @Override
    public void execute(Runnable command) {
        submit(command);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return submit(new ScheduledFutureTask<Void>(command, null, triggerTime(delay, unit)));
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return submit(new ScheduledFutureTask<V>(callable, triggerTime(delay, unit)));
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return submit(new ScheduledFutureTask<Void>(command,
                                                    null,
                                                    triggerTime(initialDelay, unit),
                                                    unit.toMillis(period)));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return submit(new ScheduledFutureTask<Void>(command,
                                                    null,
                                                    triggerTime(initialDelay, unit),
                                                    unit.toMillis(-delay)));
    }

    private <T> FutureTask<T> submit(FutureTask<T> future) {
        checkShutdown();
        runnables.add(future);
        return future;
    }

    private <T> ScheduledFutureTask<T> submit(ScheduledFutureTask<T> future) {
        checkShutdown();
        runnables.add(future);
        return future;
    }

    private long triggerTime(long delay, TimeUnit unit) {
        return timeService.getCurrentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
        checkShutdown();
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        checkShutdown();
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
        checkShutdown();
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
        checkShutdown();
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();

        return getScheduled();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown && getScheduled().size() == 0;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        long deadline = System.currentTimeMillis() + unit.toMillis(timeout);

        while (System.currentTimeMillis() < deadline && getScheduled().size() > 0) {
            executePending();
        }

        return getScheduled().size() == 0;
    }

    private void checkShutdown() {
        if (shutdown) {
            throw new IllegalStateException("scheduler has shut down");
        }
    }

    private List<Runnable> getScheduled() {
        List<Runnable> scheduled = new ArrayList<Runnable>();
        scheduled.addAll(runnables);
        return scheduled;
    }

    public class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {
        /** period if positive, delay if negative */
        private final long period;

        /** the next time this task should run */
        private long time;

        public ScheduledFutureTask(Callable<V> callable, long delay) {
            super(callable);
            this.time = delay;
            this.period = 0;
        }

        public ScheduledFutureTask(Runnable runnable, V result, long delay) {
            super(runnable, result);
            this.time = delay;
            this.period = 0;
        }

        public ScheduledFutureTask(Runnable runnable, V result, long delay, long period) {
            super(runnable, result);
            this.time = delay;
            this.period = period;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(time - timeService.getCurrentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            if (this == o) {
                return 0;
            }

            if (o instanceof ScheduledFutureTask) {
                ScheduledFutureTask<?> otherTask = (ScheduledFutureTask<?>) o;
                long diff = time - otherTask.time;

                if (diff < 0) {
                    return -1;
                } else if (diff > 0) {
                    return 1;
                } else {
                    return 1;
                }
            }

            long d = (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
            return (d == 0) ? 0 : ((d < 0) ? -1 : 1);
        }

        @Override
        public boolean isPeriodic() {
            return period != 0;
        }

        @Override
        public void run() {
            if (isPeriodic()) {
                super.runAndReset();

                if (period > 0) {
                    // set the next time to run, with the period as period
                    this.time += period;
                } else {
                    // set the next time to run, with the period as delay
                    this.time = triggerTime(-period, TimeUnit.MILLISECONDS);
                }
            } else {
                super.run();
            }
        }
    }

}
