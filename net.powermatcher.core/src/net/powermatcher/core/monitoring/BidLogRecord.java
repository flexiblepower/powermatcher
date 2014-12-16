package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.monitoring.events.BidEvent;

public class BidLogRecord extends LogRecord {

    private Bid bid;

    public BidLogRecord(BidEvent event, Date logTime, DateFormat dateFormat) {
        super(event.getClusterId(), event.getAgentId(), event.getQualifier(), logTime, event.getTimestamp(), dateFormat);

        this.bid = event.getBid();
    }

    public Bid getBid() {
        return bid;
    }
}
