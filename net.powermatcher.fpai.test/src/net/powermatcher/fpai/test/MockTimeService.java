package net.powermatcher.fpai.test;

import java.util.Date;

import net.powermatcher.core.scheduler.service.TimeService;

import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;

/**
 * Implementation of a TimeService which can be controlled programmatically
 */
public class MockTimeService implements TimeService {
    private long currentTime;

    public MockTimeService() {
        currentTime = System.currentTimeMillis();
    }

    public MockTimeService(long initialTime) {
        currentTime = initialTime;
    }

    public MockTimeService(Date initialTime) {
        currentTime = initialTime.getTime();
    }

    public void setAbsoluteTime(long time) {
        currentTime = time;
    }

    public void stepInTime(long stepMs) {
        currentTime += stepMs;
    }

    public void stepInTime(double value, TimeUnit unit) {
        currentTime += unit.convertTo(value, TimeUnit.MILLISECONDS);
    }

    public void stepInTime(long value, java.util.concurrent.TimeUnit unit) {
        currentTime += java.util.concurrent.TimeUnit.MILLISECONDS.convert(value, unit);
    }

    public void stepInTime(Duration duration) {
        currentTime += duration.getMilliseconds();
    }

    @Override
    public long currentTimeMillis() {
        return currentTime;
    }

    @Override
    public int getRate() {
        return 0;
    }

    public Date getDate() {
        return new Date(currentTimeMillis());
    }

    public org.flexiblepower.time.TimeService getFlexiblePowerTimeService() {
        return new PowerMatcherTimeServiceAdapter(this);
    }

    @Override
    public String toString() {
        return "MockTimeService [currentTime=" + new Date(currentTime) + "]";
    }

    public static class PowerMatcherTimeServiceAdapter implements org.flexiblepower.time.TimeService {
        private final TimeService timeService;

        public PowerMatcherTimeServiceAdapter(TimeService timeService) {
            this.timeService = timeService;
        }

        @Override
        public Date getTime() {
            return new Date(getCurrentTimeMillis());
        }

        @Override
        public long getCurrentTimeMillis() {
            return timeService.currentTimeMillis();
        }
    }

}
