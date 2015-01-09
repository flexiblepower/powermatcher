package net.powermatcher.api.monitoring.events;

import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.Qualifier;

/**
 * An {@link BidEvent} is sent when an {@link Bid} is sent or Received by an
 * {@link AgentEndpoint} or a {@link MatcherEndpoint}.
 * 
 * @author FAN
 * @version 2.0
 */
public abstract class BidEvent extends AgentEvent {

	/**
	 * The new {@link Bid} created by the {@link AgentEndpoint} subclass.
	 */
	private final Bid bid;

	/**
	 * The qualifier of the event
	 */
	private Qualifier qualifier;

	/**
	 * The id of the {@link Session} of the {@link AgentEndpoint} subclass
	 * sending the UpdateEvent
	 */
	private final String sessionId;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @param clusterId
	 *            The id of the cluster the {@link AgentEndpoint} subclass
	 *            sending the UpdateEvent is running in.
	 * @param agentId
	 *            The id of the {@link AgentEndpoint} subclass sending the
	 *            UpdateEvent.
	 * @param sessionId
	 *            The id of the {@link Session} of the {@link AgentEndpoint}
	 *            subclass sending the UpdateEvent
	 * @param timestamp
	 *            The {@link Date} received from the {@link TimeService}
	 * @param bid
	 *            The new {@link Price} created by the {@link AgentEndpoint}
	 *            subclass.
	 */
	public BidEvent(String clusterId, String agentId, String sessionId,
			Date timestamp, Bid bid, Qualifier qualifier) {
		super(clusterId, agentId, timestamp);
		this.bid = bid;
		this.qualifier = qualifier;
		this.sessionId = sessionId;
	}

	/**
	 * @return the current value of bid.
	 */
	public Bid getBid() {
		return bid;
	}

	/**
	 * @return the current value of qualifier.
	 */
	public Qualifier getQualifier() {
		return qualifier;
	}

	/**
	 * @return the current value of session.
	 */
	public String getSessionId() {
		return sessionId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return BidEvent.class.getSimpleName() + " " + super.toString()
				+ ", qualifier = " + qualifier.getDescription()
				+ ", sessionId = " + this.sessionId + ", bid = "
				+ bid.toString();
	}
}
