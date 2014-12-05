package net.powermatcher.mock;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MockScheduler extends ScheduledThreadPoolExecutor {

    private Runnable tasker;

    public MockScheduler() {
        super(1);
    }

    @Override
    public ScheduledFuture<String> scheduleAtFixedRate(Runnable arg0, long arg1, long arg2, TimeUnit arg3) {
        this.tasker = arg0;
        return null;
    }

    public void doTaskOnce() {
        tasker.run();
    }

}
