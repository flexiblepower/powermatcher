package net.powermatcher.core.mock;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

public class MockAgent implements AgentRole {

    private Map<String, Object> agentProperties;
    private Price lastPriceUpdate;
    private Session session;

    public MockAgent(String agentId) {
        this.agentProperties = new HashMap<String, Object>();
        this.agentProperties.put("agentId", agentId);
    }

    @Override
    public void connectToMatcher(Session session) {
        this.session = session;
    }

    @Override
    public void matcherRoleDisconnected(Session session) {
        this.session = null;
    }

    @Override
    public void updatePrice(Price newPrice) {
        this.lastPriceUpdate = newPrice;
    }

    public void sendBid(Bid newBid) {
        this.session.updateBid(newBid);
    }

    public Price getLastPriceUpdate() {
        return lastPriceUpdate;
    }

    public Map<String, Object> getAgentProperties() {
        return agentProperties;
    }
}
