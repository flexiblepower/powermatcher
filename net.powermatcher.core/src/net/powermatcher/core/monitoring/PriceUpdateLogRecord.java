package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;

/**
 * {@link PriceUpdateLogRecord} is an implementation of {@link LogRecord} that stores a {@link PriceUpdateEvent}.
 *
 * @author FAN
 * @version 2.0
 */
public class PriceUpdateLogRecord
    extends LogRecord {

    /**
     * The {@link PriceUpdate} the {@link PriceUpdateEvent} was sent for.
     */
    private final PriceUpdate priceUpdate;

    /**
     * A constructor that creates an instance of this class.
     *
     * @param event
     *            the {@link PriceUpdateEvent} that needs to be logged
     * @param logTime
     *            the time the event was logged
     * @param dateFormat
     *            the {@link DateFormat} that will be used to log the {@link LogRecord}
     */
    public PriceUpdateLogRecord(PriceUpdateEvent event, Date logTime, DateFormat dateFormat) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);
        priceUpdate = event.getPriceUpdate();
    }

    /**
     * @return the current value of priceUpdate.
     */
    public PriceUpdate getPriceUpdate() {
        return priceUpdate;
    }
}
