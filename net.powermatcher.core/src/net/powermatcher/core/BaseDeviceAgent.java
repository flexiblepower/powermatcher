package net.powermatcher.core;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;

/**
 * {@link BaseDeviceAgent} defines the basic functionality of any Device Agent.
 * 
 * @author FAN
 * @version 2.0
 */
public abstract class BaseDeviceAgent extends BaseAgent implements
		AgentEndpoint, Comparable<BaseDeviceAgent> {
	private final AtomicInteger bidNumberGenerator = new AtomicInteger();

	/**
	 * The last {@link Bid} received by this BaseDeviceAgent
	 */
	private Bid lastBid;

	/**
	 * The current {@link Session} this BaseDeviceAgent is linked in.
	 */
	private Session session;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized void connectToMatcher(Session session) {
		this.session = session;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized void matcherEndpointDisconnected(Session session) {
		this.session = null;
	}

	/**
	 * @return the current value of session.
	 */
	public final Session getSession() {
		return session;
	}

	/**
	 * @return the current value of the marketBasis of the session.
	 */
	protected final synchronized MarketBasis getMarketBasis() {
		if (session == null) {
			return null;
		} else {
			return session.getMarketBasis();
		}
	}

	/**
	 * Creates a new {@link PointBid}, based on the parameter,
	 * {@link MarketBasis} and a new nidnumber.
	 * 
	 * @param pricePoints
	 *            The {@link PricePoint} array, used to create the
	 *            {@link PointBid}
	 * @return A new {@link PointBid}
	 */
	protected final synchronized PointBid createBid(PricePoint... pricePoints) {
		if (session == null) {
			return null;
		} else {
			return new PointBid(session.getMarketBasis(),
					bidNumberGenerator.incrementAndGet(), pricePoints);
		}
	}

	/**
	 * @return the current value of lastBid.
	 */
	public final Bid getLastBid() {
		return lastBid;
	}

	/**
	 * Handles a new {@link Bid} being created by the subclasses.
	 * 
	 * @param newBid
	 */
	public final synchronized void publishBid(Bid newBid) {
		if (session != null) {
			lastBid = newBid;
			session.updateBid(newBid);
			publishEvent(new OutgoingBidEvent(getClusterId(), getAgentId(),
					session.getSessionId(), now(), newBid, Qualifier.AGENT));
		}
	}

	/**
	 * @return creates a new threadsafe bidNumber.
	 */
	protected int getCurrentBidNr() {
		return bidNumberGenerator.get();
	}

	/**
	 * Returns the current time in a {@link Date} object.
	 * 
	 * @return A {@link Date} object, representing the current date and time
	 */
	protected abstract Date now();

	/**
	 * Contains the logic to create a new {@link Bid} and then calls
	 * {@link BaseDeviceAgent}{@link #publishBid(Bid)}
	 */
	protected abstract void doBidUpdate();

	/**
	 * Compares this object with the specified object for order. Returns a
	 * negative integer, zero, or a positive integer as this object is less
	 * than, equal to, or greater than the specified object.
	 * 
	 * This method compares the agentId values of both instances. They are
	 * compared alphabetically.
	 * 
	 * @param that
	 *            The {@link PricePoint} instance you want to compare with this
	 *            one.
	 * 
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object.
	 */
	@Override
	public int compareTo(BaseDeviceAgent that) {
		return getAgentId().compareTo(that.getAgentId());
	}

	public boolean canEqual(Object other) {
		return other instanceof BaseDeviceAgent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		BaseDeviceAgent other = (BaseDeviceAgent) ((obj instanceof BaseDeviceAgent) ? obj
				: null);
		if (other == null) {
			return false;
		}

		if (this == other) {
			return true;
		}

		return this.canEqual(other) && super.equals(other)
				&& this.lastBid.equals(other.lastBid);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 211 * (super.hashCode() + lastBid.hashCode() + session
				.hashCode());
	}

}
