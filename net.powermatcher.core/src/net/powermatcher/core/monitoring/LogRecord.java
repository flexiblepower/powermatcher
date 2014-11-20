package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import javax.xml.crypto.Data;

import net.powermatcher.api.monitoring.AgentEvent;
import net.powermatcher.api.monitoring.Qualifier;

/**
 * @author IBM
 * @version 0.9.0
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
     * The qualifier of the agent that published the {@link AgentEvent}.
     */
    private Qualifier qualifier;

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
     *            The Agent Id that published the {@link AgentEven
     * @param qualifier
     *            The qualifier of the agent that published the {@link AgentEvent}.
     */
    protected LogRecord( String clusterId,  String agentId,  Qualifier qualifier, Date logTime, Date eventTimestamp, DateFormat dateFormat) {
        this.clusterId = clusterId;
        this.agentId = agentId;
        this.qualifier = qualifier;
        this.logTime = logTime;
        this.eventTimestamp = eventTimestamp;
        this.dateFormat = dateFormat;
    }

    /**
     * Get logRecord data as a String[]
     * 
     * @return All fields of the {@link LogRecord} in a {@link String}[]
     */
    public abstract String[] getLine();

    public String getClusterId() {
        return clusterId;
    }

    public String getAgentId() {
        return agentId;
    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    public Date getLogTime() {
        return logTime;
    }

    public Date getEventTimestamp() {
        return eventTimestamp;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }
    
}
