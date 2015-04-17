package net.powermatcher.mock;

import net.powermatcher.api.Agent;

import org.flexiblepower.context.FlexiblePowerContext;

public abstract class MockAgent
    implements Agent {
    private final String agentId;
    protected FlexiblePowerContext context;

    public MockAgent(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public void setContext(FlexiblePowerContext context) {
        this.context = context;
    }
}
