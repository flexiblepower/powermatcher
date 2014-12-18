package net.powermatcher.mock;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockScheduler extends ScheduledThreadPoolExecutor {

    private Runnable task;
    private long updateRate;
    private MockFuture mockFuture;
    
    public class MockFuture implements ScheduledFuture<String>{

        private boolean cancelled = false;
        
        @Override
        public long getDelay(TimeUnit arg0) {
            return 0;
        }

        @Override
        public int compareTo(Delayed arg0) {
            return 0;
        }
        
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
        
        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean cancel(boolean arg0) {
            cancelled = true;
            return cancelled;
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            return "mock get()";
        }

        @Override
        public String get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {
            return "mock get(long, TimeUnit)";
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return true;
        }
        
    }

    public MockScheduler() {
        super(1);
    }

    @Override
    public ScheduledFuture<String> scheduleAtFixedRate(Runnable task, long delay, long updateRate, TimeUnit timeUnit) {
        this.task = task;
        this.updateRate = updateRate;
        mockFuture = new MockFuture();
        return mockFuture;
    }
    
    public long getUpdateRate(){
        return updateRate;
    }
    
    public MockFuture getMockFuture(){
        return mockFuture;
    }

    public void doTaskOnce() {
        task.run();
    }

}
