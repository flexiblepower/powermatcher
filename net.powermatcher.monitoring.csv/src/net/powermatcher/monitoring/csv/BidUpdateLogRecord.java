package net.powermatcher.monitoring.csv;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.monitoring.events.BidUpdateEvent;

/**
 * {@link BidUpdateLogRecord} is an implementation of {@link LogRecord} that stores a {@link BidUpdateEvent}.
 *
 * @author FAN
 * @version 2.1
 */
public class BidUpdateLogRecord
    extends LogRecord {

    /**
     * The {@link Bid} the {@link BidUpdateEvent} was sent for.
     */
    private final BidUpdate bidUpdate;

    /**
     * A constructor that creates an instance of this class.
     *
     * @param event
     *            the {@link BidUpdateEvent} that needs to be logged
     * @param logTime
     *            the time the event was logged
     * @param dateFormat
     *            the {@link DateFormat} that will be used to log the {@link LogRecord}
     */
    public BidUpdateLogRecord(BidUpdateEvent event, Date logTime, DateFormat dateFormat) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);

        bidUpdate = event.getBidUpdate();
    }

    /**
     * @return the current value of bid.
     */
    public BidUpdate getBidUpdate() {
        return bidUpdate;
    }
}
