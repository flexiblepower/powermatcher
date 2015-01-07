package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.monitoring.events.WhitelistEvent;
import net.powermatcher.core.concentrator.Concentrator;

/**
 * {@link BidLogRecord} is an implementation of {@link LogRecord} that stores a {@link WhitelistLogRecord}.
 * 
 * @author FAN
 * @version 2.0
 */
public class WhitelistLogRecord extends LogRecord {

    /**
     * The id of the {@link AgentEndpoint} that was blocked by the {@link Concentrator}.
     */
    private String blockedAgent;

    /**
     * A constructor that creates an instance of this class.
     * 
     * @param event
     *            the {@link WhitelistEvent} that needs to be logged
     * @param logTime
     *            the time the event was logged
     * @param dateFormat
     *            the {@link DateFormat} that will be used to log the {@link LogRecord}
     * @param blockedAgent
     *            the id of the {@link AgentEndpoint} that was blocked by the {@link Concentrator}
     */
    public WhitelistLogRecord(WhitelistEvent event, Date logTime, DateFormat dateFormat, String blockedAgent) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);
        this.blockedAgent = blockedAgent;

    }

    /**
     * @return the current value of blockedAgent.
     */
    public String getBlockedAgent() {
        return blockedAgent;
    }
}
