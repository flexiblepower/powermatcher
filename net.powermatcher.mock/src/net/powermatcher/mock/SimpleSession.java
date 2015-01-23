package net.powermatcher.mock;

import java.util.UUID;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

public class SimpleSession implements Session {

	private final AgentEndpoint agent;
	private final MatcherEndpoint matcher;
	private final String sessionId;
	private MarketBasis marketBasis;

	public SimpleSession(AgentEndpoint agent, MatcherEndpoint matcher) {
		this.agent = agent;
		this.matcher = matcher;
		this.sessionId = UUID.randomUUID().toString();
	}

	public void connect() {
		this.agent.connectToMatcher(this);
		this.matcher.connectToAgent(this);
	}

	@Override
	public String getAgentId() {
		return agent.getAgentId();
	}

	@Override
	public String getMatcherId() {
		return matcher.getAgentId();
	}

	@Override
	public String getClusterId() {
		return matcher.getClusterId();
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public MarketBasis getMarketBasis() {
		return this.marketBasis;
	}

	@Override
	public void setClusterId(String clusterId) {
		// Huh?! TODO
	}

	@Override
	public void setMarketBasis(MarketBasis marketBasis) {
		this.marketBasis = marketBasis;
	}

	@Override
	public void updatePrice(PriceUpdate priceUpdate) {
		this.agent.updatePrice(priceUpdate);
	}

	@Override
	public void updateBid(Bid newBid) {
		this.matcher.updateBid(this, newBid);
	}

	@Override
	public void disconnect() {
		this.matcher.agentEndpointDisconnected(this);
		this.agent.matcherEndpointDisconnected(this);
	}

}
