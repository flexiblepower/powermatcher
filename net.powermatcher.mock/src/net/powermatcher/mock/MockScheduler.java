package net.powermatcher.mock;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class MockScheduler extends ScheduledThreadPoolExecutor {

    private Runnable task;
    private long updateRate;
    private MockFuture mockFuture;

    public class MockFuture implements ScheduledFuture<String> {

        private boolean cancelled = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public long getDelay(TimeUnit arg0) {
            return 0;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
         * as this object is less than, equal to, or greater than the specified object.
         * 
         * This mock method will always return 0. This method had to be overridden, but it is never used.
         * 
         * @param that
         *            The {@link PricePoint} instance you want to compare with this one.
         * 
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
         *         the specified object.
         */
        @Override
        public int compareTo(Delayed that) {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean cancel(boolean arg0) {
            cancelled = true;
            return cancelled;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String get() throws InterruptedException, ExecutionException {
            return "mock get()";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
            return "mock get(long, TimeUnit)";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDone() {
            return true;
        }

    }

    public MockScheduler() {
        super(1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledFuture<String> scheduleAtFixedRate(Runnable task, long delay, long updateRate, TimeUnit timeUnit) {
        this.task = task;
        this.updateRate = updateRate;
        mockFuture = new MockFuture();
        return mockFuture;
    }

    /**
     * @return the current value of updateRate.
     */
    public long getUpdateRate() {
        return updateRate;
    }

    /**
     * @return the current value of mockFuture.
     */
    public MockFuture getMockFuture() {
        return mockFuture;
    }

    public void doTaskOnce() {
        task.run();
    }

}
