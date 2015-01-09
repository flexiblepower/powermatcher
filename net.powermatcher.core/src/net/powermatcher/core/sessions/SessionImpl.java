package net.powermatcher.core.sessions;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;

/**
 * <p>
 * This class represents an implementation of the {@link Session} object.
 * </p>
 * 
 * <p>
 * It is responsible to hold serveral properties like sessionId, agentId,
 * matcherId. There are two important methods. UpdateBid() and updatePrice().
 * UpdateBid() is called by {@link Concentrator} and agents. UpdatePrice() is
 * called by the {@link Auctioneer} and {@link Concentrator}.
 * 
 * @author FAN
 * @version 2.0
 */
public class SessionImpl implements Session {

	/**
	 * The sessionmanager collects the connections in the cluster.
	 */
	private final SessionManager sessionManager;

	/**
	 * Id of the agentEndpoint instance of the session.
	 */
	private final String agentId;

	/**
	 * Id of the matcherEndpoint instance of the session.
	 */
	private final String matcherId;

	/**
	 * Id of the session.
	 */
	private final String sessionId;

	/**
	 * The {@link AgentEndpoint} instance links to the {@link MatcherEndpoint}
	 * in this {@link Session}.
	 */
	private final AgentEndpoint agentEndpoint;

	/**
	 * The {@link MatcherEndpoint} instance links to the {@link AgentEndpoint}
	 * in this {@link Session}.
	 */
	private final MatcherEndpoint matcherEndpoint;

	/**
	 * Holds the clusterId
	 */
	private String clusterId;

	/**
	 * The {@link MarketBasis} set from the {@link Auctioneer}
	 */
	private MarketBasis marketBasis;

	public SessionImpl(SessionManager sessionManager,
			AgentEndpoint agentEndpoint, String agentId,
			MatcherEndpoint matcherEndpoint, String matcherId, String sessionId) {
		this.sessionManager = sessionManager;
		this.agentId = agentId;
		this.matcherId = matcherId;
		this.agentEndpoint = agentEndpoint;
		this.matcherEndpoint = matcherEndpoint;
		this.sessionId = sessionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAgentId() {
		return agentId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMatcherId() {
		return matcherId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getClusterId() {
		return clusterId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MarketBasis getMarketBasis() {
		return marketBasis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClusterId(String clusterId) {
		if (this.clusterId != null) {
			throw new IllegalStateException("clusterId can only be set once");
		}
		this.clusterId = clusterId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setMarketBasis(MarketBasis marketBasis) {
		if (this.marketBasis != null) {
			throw new IllegalStateException("marketBasis can only be set once");
		}
		this.marketBasis = marketBasis;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updatePrice(PriceUpdate priceUpdate) {
		agentEndpoint.updatePrice(priceUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateBid(Bid newBid) {
		matcherEndpoint.updateBid(this, newBid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		agentEndpoint.matcherEndpointDisconnected(this);
		matcherEndpoint.agentEndpointDisconnected(this);
		sessionManager.disconnected(this);
	}

	/**
	 * @return the current value of agentEndpoint.
	 */
	public AgentEndpoint getAgentEndpoint() {
		return agentEndpoint;
	}

	/**
	 * @return the current value of matcherEndpoint.
	 */
	public MatcherEndpoint getMatcherEndpoint() {
		return matcherEndpoint;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		SessionImpl that = (SessionImpl) ((obj instanceof SessionImpl) ? obj
				: null);
		if (that == null) {
			return false;
		}

		if (this == that) {
			return true;
		}

		return this.agentId.equals(that.agentId)
				&& this.clusterId.equals(that.clusterId)
				&& this.agentEndpoint.equals(that.agentEndpoint)
				&& this.matcherId.equals(that.matcherId)
				&& this.marketBasis.equals(that.marketBasis)
				&& this.matcherEndpoint.equals(that.matcherEndpoint)
				&& this.sessionId.equals(that.sessionId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 211 * (this.agentId.hashCode() + this.matcherId.hashCode() + this.sessionId
				.hashCode());
	}
}
