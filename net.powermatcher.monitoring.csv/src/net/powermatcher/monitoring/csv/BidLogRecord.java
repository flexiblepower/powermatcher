package net.powermatcher.monitoring.csv;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.monitoring.events.BidEvent;

/**
 * {@link BidLogRecord} is an implementation of {@link LogRecord} that stores a {@link BidEvent}.
 *
 * @author FAN
 * @version 2.0
 */
public class BidLogRecord
    extends LogRecord {

    /**
     * The {@link Bid} the {@link BidEvent} was sent for.
     */
    private final Bid bid;

    /**
     * A constructor that creates an instance of this class.
     *
     * @param event
     *            the {@link BidEvent} that needs to be logged
     * @param logTime
     *            the time the event was logged
     * @param dateFormat
     *            the {@link DateFormat} that will be used to log the {@link LogRecord}
     */
    public BidLogRecord(BidEvent event, Date logTime, DateFormat dateFormat) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);

        bid = event.getBid();
    }

    /**
     * @return the current value of bid.
     */
    public Bid getBid() {
        return bid;
    }
}
