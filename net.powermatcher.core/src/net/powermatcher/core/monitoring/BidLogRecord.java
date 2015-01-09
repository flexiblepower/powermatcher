package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidEvent;

/**
 * {@link BidLogRecord} is an implementation of {@link LogRecord} that stores a
 * {@link BidEvent}.
 * 
 * @author FAN
 * @version 2.0
 */
public class BidLogRecord extends LogRecord {

	/**
	 * The {@link Bid} the {@link BidEvent} was sent for.
	 */
	private Bid bid;

	/**
	 * The qualifier of the agent that published the {@link AgentEvent}.
	 */
	private Qualifier qualifier;

	/**
	 * A constructor that creates an instance of this class.
	 * 
	 * @param event
	 *            the {@link BidEvent} that needs to be logged
	 * @param logTime
	 *            the time the event was logged
	 * @param dateFormat
	 *            the {@link DateFormat} that will be used to log the
	 *            {@link LogRecord}
	 */
	public BidLogRecord(BidEvent event, Date logTime, DateFormat dateFormat) {
		super(event.getClusterId(), event.getAgentId(), logTime, event
				.getTimestamp(), dateFormat);

		this.bid = event.getBid();
		this.qualifier = event.getQualifier();
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
}
