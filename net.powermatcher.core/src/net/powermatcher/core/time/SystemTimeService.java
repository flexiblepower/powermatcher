package net.powermatcher.core.time;

import java.util.Date;

import net.powermatcher.api.TimeService;
import aQute.bnd.annotation.component.Component;

/**
 * The {@link SystemTimeService} is an implementation of the {@link TimeService} interface. The "current time" in this
 * class is the current time, returned by the {@link System} class.
 * 
 * @author FAN
 * @version 2.0
 */
@Component
public class SystemTimeService implements TimeService {

    /**
     * {@inheritDoc}
     */
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date currentDate() {
        return new Date();
    }
}
