package net.powermatcher.core.sessions;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

class SessionImpl implements Session {
	private final SessionManager sessionManager;
	private final String agentId, matcherId, sessionId;
	private final AgentRole agentRole;
	private final MatcherRole matcherRole;
	
	private String clusterId = null;
	private MarketBasis marketBasis = null;
	
	public SessionImpl(SessionManager sessionManager, AgentRole agentRole, String agentId, MatcherRole matcherRole, String matcherId, String sessionId) {
		this.sessionManager = sessionManager;
		this.agentId = agentId;
		this.matcherId = matcherId;
		this.agentRole =agentRole;
		this.matcherRole = matcherRole;
		this.sessionId = sessionId;
	}

	@Override
	public String getAgentId() {
		return agentId;
	}

	@Override
	public String getMatcherId() {
		return matcherId;
	}
	
	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public String getClusterId() {
		if(clusterId == null) {
			throw new IllegalStateException("No clusterId has been defined");
		}
		return clusterId;
	}

	@Override
	public MarketBasis getMarketBasis() {
		if(marketBasis == null) {
			throw new IllegalStateException("No marketBasis has been defined");
		}
		return marketBasis;
	}

	@Override
	public void setClusterId(String clusterId) {
		if(this.clusterId != null) {
			throw new IllegalStateException("clusterId can only be set once");
		}
		this.clusterId = clusterId;
	}

	@Override
	public void setMarketBasis(MarketBasis marketBasis) {
		if(this.marketBasis != null) {
			throw new IllegalStateException("marketBasis can only be set once");
		}
		this.marketBasis = marketBasis;
	}

	@Override
	public void updatePrice(Price newPrice) {
		agentRole.updatePrice(newPrice);
	}

	@Override
	public void updateBid(Bid newBid) {
		matcherRole.updateBid(this, newBid);
	}

	@Override
	public void disconnect() {
		agentRole.disconnectFromMatcher(this);
		matcherRole.disconnectFromAgent(this);
		sessionManager.disconnected(this);
	}
}
