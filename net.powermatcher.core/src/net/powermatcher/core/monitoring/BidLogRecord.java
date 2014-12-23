package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidEvent;

public class BidLogRecord extends LogRecord {

    private Bid bid;

    /**
     * The qualifier of the agent that published the {@link AgentEvent}.
     */
    private Qualifier qualifier;

    public BidLogRecord(BidEvent event, Date logTime, DateFormat dateFormat) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);

        this.bid = event.getBid();
        this.qualifier = event.getQualifier();
    }

    public Bid getBid() {
        return bid;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }
}
