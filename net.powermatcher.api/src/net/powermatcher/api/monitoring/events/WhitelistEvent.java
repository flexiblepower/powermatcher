package net.powermatcher.api.monitoring.events;

import java.util.Date;

public class WhitelistEvent extends AgentEvent {

    private String blockedAgent;

    public WhitelistEvent(String agentId, String blockedAgent, String clusterId, Date timestamp) {
        super(clusterId, agentId, timestamp);
        this.blockedAgent = blockedAgent;
    }

    public String getBlockedAgent() {
        return blockedAgent;
    }

    @Override
    public String toString() {
        return super.toString() + " blocked agent " + blockedAgent;
    }
}
