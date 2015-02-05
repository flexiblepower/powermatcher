package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;

/**
 * A {@link WhitelistEvent} is sent when an {@link MatcherEndpoint} rejects a connection, because the
 * {@link AgentEndpoint} was not on its whitelist.
 *
 * @author FAN
 * @version 2.0
 */
public class WhitelistEvent
    extends AgentEvent {

    /**
     * The id of the blocked {@link AgentEndpoint}
     */
    private final String blockedAgent;

    /**
     * A constructor to create an instance of a WhitelistEvent
     *
     * @param blockedAgent
     *            The id of the blocked {@link AgentEndpoint}
     * @param agentId
     *            The id of the {@link AgentEndpoint} subclass sending the UpdateEvent.
     * @param clusterId
     *            The id of the cluster the {@link AgentEndpoint} subclass sending the UpdateEvent is running in.
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     */
    public WhitelistEvent(String agentId, String blockedAgent, String clusterId, Date timestamp) {
        super(clusterId, agentId, timestamp);
        this.blockedAgent = blockedAgent;
    }

    /**
     * @return the current value of blockedAgent.
     */
    public String getBlockedAgent() {
        return blockedAgent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " blocked agent " + blockedAgent;
    }
}
