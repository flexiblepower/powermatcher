package net.powermatcher.mock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockScheduler extends ScheduledThreadPoolExecutor {

    private Runnable tasker;

    public MockScheduler() {
        super(1);
    }

    @Override
    public boolean awaitTermination(long arg0, TimeUnit arg1) throws InterruptedException {
        return false;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0) throws InterruptedException {
        return super.invokeAll(arg0);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
            throws InterruptedException {
        return super.invokeAll(arg0, arg1, arg2);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> arg0) throws InterruptedException, ExecutionException {
        return super.invokeAny(arg0);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
            throws InterruptedException, ExecutionException, TimeoutException {
        return super.invokeAny(arg0, arg1, arg2);
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return shutdownNow();
    }

    @Override
    public <T> Future<T> submit(Callable<T> arg0) {
        return submit(arg0);
    }

    @Override
    public Future<?> submit(Runnable arg0) {
        return submit(arg0);
    }

    @Override
    public <T> Future<T> submit(Runnable arg0, T arg1) {
        return submit(arg0, arg1);
    }

    @Override
    public void execute(Runnable arg0) {
        super.execute(arg0);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable arg0, long arg1, TimeUnit arg2) {
        return super.schedule(arg0, arg1, arg2);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> arg0, long arg1, TimeUnit arg2) {
        return super.schedule(arg0, arg1, arg2);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable arg0, long arg1, long arg2, TimeUnit arg3) {
        this.tasker = arg0;
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable arg0, long arg1, long arg2, TimeUnit arg3) {
        return super.scheduleWithFixedDelay(arg0, arg1, arg2, arg3);
    }

    public void doTaskOnce() {
        tasker.run();
    }

}
