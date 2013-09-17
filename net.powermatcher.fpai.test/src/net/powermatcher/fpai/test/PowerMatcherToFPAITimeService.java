package net.powermatcher.fpai.test;

import java.util.Date;

import org.flexiblepower.time.TimeService;

public class PowerMatcherToFPAITimeService implements TimeService {

    private final net.powermatcher.core.scheduler.service.TimeService timeService;

    public PowerMatcherToFPAITimeService(net.powermatcher.core.scheduler.service.TimeService timeService) {
        this.timeService = timeService;
    }

    @Override
    public Date getTime() {
        return new Date(timeService.currentTimeMillis());
    }

    @Override
    public long getCurrentTimeMillis() {
        return timeService.currentTimeMillis();
    }
}
