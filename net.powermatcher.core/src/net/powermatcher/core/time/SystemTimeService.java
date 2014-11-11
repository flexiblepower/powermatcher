package net.powermatcher.core.time;

import java.util.Date;

import aQute.bnd.annotation.component.Component;
import net.powermatcher.api.TimeService;

@Component
public class SystemTimeService implements TimeService {

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Date currentDate() {
        return new Date();
    }

}
