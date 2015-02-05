package net.powermatcher.mock;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import org.flexiblepower.context.FlexiblePowerContext;

public class MockContext
    implements FlexiblePowerContext {

    private long now;
    private final MockScheduler scheduler;

    public MockContext(Date initialDate) {
        now = initialDate.getTime();
        scheduler = new MockScheduler();
    }

    public MockContext(long initialDate) {
        now = initialDate;
        scheduler = new MockScheduler();
    }

    @Override
    public long currentTimeMillis() {
        return now;
    }

    public void jump(long ms) {
        now += ms;
    }

    @Override
    public Date currentTime() {
        return new Date(now);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public MockScheduler getMockScheduler() {
        return scheduler;
    }

}
