package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.monitoring.events.AgentEvent;

public class WhitelistLogRecord extends LogRecord {

    private String blockedAgent;

    public WhitelistLogRecord(AgentEvent event, Date logTime, DateFormat dateFormat, String blockedAgent) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);
        this.blockedAgent = blockedAgent;

    }

    public String getBlockedAgent() {
        return blockedAgent;
    }
}
