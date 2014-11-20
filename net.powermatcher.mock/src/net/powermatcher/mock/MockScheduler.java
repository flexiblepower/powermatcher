package net.powermatcher.mock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockScheduler implements ScheduledExecutorService {

    @Override
    public boolean awaitTermination(long arg0, TimeUnit arg1) throws InterruptedException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
            throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> arg0) throws InterruptedException, ExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> arg0, long arg1, TimeUnit arg2)
            throws InterruptedException, ExecutionException, TimeoutException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isShutdown() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isTerminated() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Runnable> shutdownNow() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Future<?> submit(Runnable arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable arg0, T arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void execute(Runnable arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public ScheduledFuture<?> schedule(Runnable arg0, long arg1, TimeUnit arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> arg0, long arg1, TimeUnit arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable arg0, long arg1, long arg2, TimeUnit arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable arg0, long arg1, long arg2, TimeUnit arg3) {
        // TODO Auto-generated method stub
        return null;
    }

}
