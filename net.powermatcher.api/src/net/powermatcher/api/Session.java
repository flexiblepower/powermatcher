package net.powermatcher.api;

import net.powermatcher.api.data.MarketBasis;

public class Session {
	private final MarketBasis marketBasis;
	private final String sessionId;
	
	private final AgentRole agentRole;
	private final MatcherRole matcherRole;
	
	public Session(MarketBasis marketBasis, String sessionId,
			AgentRole agentRole, MatcherRole matcherRole) {
		this.marketBasis = marketBasis;
		this.sessionId = sessionId;
		this.agentRole = agentRole;
		this.matcherRole = matcherRole;
	}

	public MarketBasis getMarketBasis() {
		return marketBasis;
	}

	public String getSessionId() {
		return sessionId;
	}

	public AgentRole getAgentRole() {
		return agentRole;
	}

	public MatcherRole getMatcherRole() {
		return matcherRole;
	}
	
	public void disconnect() {
		matcherRole.disconnect(this);
		agentRole.disconnect(this);
	}
}
