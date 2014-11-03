package net.powermatcher.api;

import net.powermatcher.api.data.MarketBasis;

public class Session {
	private MarketBasis marketBasis;
	private String sessionId;
	
	private AgentRole agentRole;
	private MatcherRole matcherRole;
	
	public Session(MarketBasis marketBasis, String sessionId,
			AgentRole agentRole, MatcherRole matcherRole) {
		super();
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
}
