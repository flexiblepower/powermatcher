package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import javax.xml.crypto.Data;

import net.powermatcher.api.monitoring.events.AgentEvent;

/**
 * LogRecord is an abstract class that contains the basic information needed to log an {@link AgentEvent}.
 * 
 * @author FAN
 * @version 2.0
 */
public abstract class LogRecord {
    /**
     * The cluster id of the agent that published the {@link AgentEvent}.
     */
    private String clusterId;

    /**
     * The Agent Id that published the {@link AgentEvent}.
     */
    private String agentId;

    /**
     * The time the event was logged in a {@link Data} instance
     */
    private Date logTime;

    /**
     * The time the event was created in a {@link Data} instance
     */
    private Date eventTimestamp;

    /**
     * The {@link DateFormat} instance used to format the logTime and eventTimeStamp
     */
    private DateFormat dateFormat;

    /**
     * Constructs an instance of this class from the specified equipment ID parameter.
     * 
     * @param clusterId
     *            The cluster id of the agent that published the {@link AgentEvent}.
     * @param agentId
     *            The Agent Id that published the {@link AgentEvent}.
     * @param qualifier
     *            The qualifier of the agent that published the {@link AgentEvent}.
     */
    protected LogRecord(String clusterId, String agentId, Date logTime, Date eventTimestamp, DateFormat dateFormat) {
        this.clusterId = clusterId;
        this.agentId = agentId;
        this.logTime = logTime;
        this.eventTimestamp = eventTimestamp;
        this.dateFormat = dateFormat;
    }

    /**
     * @return the current value of clusterId.
     */
    public String getClusterId() {
        return clusterId;
    }

    /**
     * @return the current value of agentId.
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @return the current value of logTime.
     */
    public Date getLogTime() {
        return logTime;
    }

    /**
     * @return a copy of eventTimestamp.
     */
    public Date getEventTimestamp() {
        return new Date(eventTimestamp.getTime());
    }

    /**
     * @return the current value of className.
     */
    public DateFormat getDateFormat() {
        return dateFormat;
    }

}
