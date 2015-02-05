package net.powermatcher.runtime.context;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import org.flexiblepower.context.FlexiblePowerContext;

/**
 * The {@link RuntimeContext} is an implementation of the {@link FlexiblePowerContext} interface. The "current time" in
 * this class is the current time, returned by the {@link System} class.
 *
 * @author FAN
 * @version 2.0
 */
public class RuntimeContext
    implements FlexiblePowerContext {

    private final LoggingScheduler scheduler;

    public RuntimeContext() {
        scheduler = new LoggingScheduler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Date currentTime() {
        return new Date();
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
