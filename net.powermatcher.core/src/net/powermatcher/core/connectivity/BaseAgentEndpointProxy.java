package net.powermatcher.core.connectivity;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.BaseAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for remote agents. This is the "receiving end" of a
 * remote communication pair.
 * 
 * @author FAN
 * @version 2.0
 */
public abstract class BaseAgentEndpointProxy extends BaseAgent implements AgentEndpoint {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BaseAgentEndpointProxy.class);

	/**
	 * The id of the {@link MatcherEndpointProxy}
	 */
	private String matcherEndpointProxyId;

	/**
	 * The {@link Session} to the local {@link MatcherEndpoint}.
	 */
	private Session localSession;

	/**
	 * @return <code>true</code> if the local {@link Session} is not
	 *         <code>null</code>.
	 */
	public boolean isLocalConnected() {
		return this.localSession != null;
	}

	/**
	 * @return <code>true</code> if {@link Session} and the connection to the
	 *         remote {@link MatcherEndpointProxy} is active.
	 */
	public abstract boolean isRemoteConnected();

	/**
	 * Sends the {@link Bid} it receives from the remote {@link MatcherEndpoint}
	 * through the {@link MatcherEndpointProxy} to the local
	 * {@link MatcherEndpoint} through the local {@link Session}.
	 * 
	 * @param priceUpdate
	 *            the new {@link PriceUpdate}
	 */
	public void updateLocalBid(Bid newBid) {
		if (!this.isLocalConnected()) {
			LOGGER.warn("Desired parent agent not connected, skip sendingg bid update");
			return;
		}

		this.localSession.updateBid(newBid);
		return;
	}

	/**
	 * @param the
	 *            new value of the matcherEndpointProxyId
	 */
	public void setMatcherEndpointProxyId(String matcherEndpointProxyId) {
		this.matcherEndpointProxyId = matcherEndpointProxyId;
	}

	/**
	 * @return the current value of the matcherEndpointProxyId.
	 */
	public String getMatcherEndpointProxyId() {
		return this.matcherEndpointProxyId;
	}

	/**
	 * @return the {@link MarketBasis} of the local {@link Session}
	 */
	public MarketBasis getLocalMarketBasis() {
		if (this.isLocalConnected()) {
			return this.localSession.getMarketBasis();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connectToMatcher(Session session) {
		this.setClusterId(session.getClusterId());
		this.localSession = session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void matcherEndpointDisconnected(Session session) {
		this.localSession = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void updatePrice(PriceUpdate priceUpdate) {
		if (!this.isRemoteConnected()) {
			LOGGER.warn("Remote agent not connected, skip sending price update");
			return;
		}

		LOGGER.info("Sending price update to remote agent {}", priceUpdate);

		this.updateRemotePrice(priceUpdate);
	}

	/**
	 * Sends the {@link PriceUpdate} it receives through from the local
	 * {@link MatcherEndpoint} to the remote {@link MatcherEndpoint} through he
	 * {@link MatcherEndpointProxy}.
	 * 
	 * @param priceUpdate
	 *            the new {@link PriceUpdate}
	 */
	public abstract void updateRemotePrice(PriceUpdate priceUpdate);

	public boolean canEqual(Object other) {
		return other instanceof BaseAgentEndpointProxy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		BaseAgentEndpointProxy that = (BaseAgentEndpointProxy) ((obj instanceof BaseAgentEndpointProxy) ? obj
				: null);
		if (that == null) {
			return false;
		}

		if (this == that) {
			return true;
		}

		return canEqual(that)
				&& this.localSession.equals(that.localSession)
				&& this.matcherEndpointProxyId
						.equals(that.matcherEndpointProxyId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 211 * (this.localSession.hashCode() + this.matcherEndpointProxyId
				.hashCode());
	}
}
