package net.powermatcher.mock;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class MockMatcherAgent extends MockAgent implements MatcherEndpoint {

	private Map<String, Object> matcherProperties;
	private Bid lastReceivedBid;
	private MarketBasis marketBasis;
	private String clusterId;

	public MockMatcherAgent(String agentId, String clusterId) {
		super(agentId);
		this.clusterId = clusterId;
		this.matcherProperties = new HashMap<String, Object>();
		this.matcherProperties.put("matcherId", agentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean connectToAgent(Session session) {
		session.setMarketBasis(this.marketBasis);
		this.session = session;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void agentEndpointDisconnected(Session session) {
		this.session = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateBid(Session session, Bid newBid) {
		this.lastReceivedBid = newBid;
	}

	/**
	 * @return the current value of lastReceivedBid.
	 */
	public Bid getLastReceivedBid() {
		return lastReceivedBid;
	}

	/**
	 * @return the current value of matcherProperties.
	 */
	public Map<String, Object> getMatcherProperties() {
		return matcherProperties;
	}

	/**
	 * @return the current value of marketBasis.
	 */
	public MarketBasis getMarketBasis() {
		return marketBasis;
	}

	public void setMarketBasis(MarketBasis marketBasis) {
		this.marketBasis = marketBasis;
	}

	public void publishPrice(PriceUpdate priceUpdate) {
		session.updatePrice(priceUpdate);
	}

	@Override
	public String getClusterId() {
		return this.clusterId;
	}

}
