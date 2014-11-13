package net.powermatcher.core.mock;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;

public class MockMatcherAgent extends MockAgent implements MatcherRole {

    private Map<String, Object> matcherProperties;
    private Bid lastReceivedBid;
    private MarketBasis marketBasis;

    public MockMatcherAgent(String agentId) {
        super(agentId);
        this.matcherProperties = new HashMap<String, Object>();
        this.matcherProperties.put("matcherId", agentId);
    }

    @Override
    public boolean connectToAgent(Session session) {
        session.setMarketBasis(this.marketBasis);
        session.setClusterId(this.matcherProperties.get("matcherId").toString());
        return true;
    }

    @Override
    public void agentRoleDisconnected(Session session) {
    }

    @Override
    public void updateBid(Session session, Bid newBid) {
        this.lastReceivedBid = newBid;
    }

    public Bid getLastReceivedBid() {
        return lastReceivedBid;
    }

    public Map<String, Object> getMatcherProperties() {
        return matcherProperties;
    }

    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }

}
