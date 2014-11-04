package net.powermatcher.core.concentrator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.IncomingBidUpdateEvent;
import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.Observer;
import net.powermatcher.api.monitoring.OutgoingBidUpdateEvent;
import net.powermatcher.api.monitoring.UpdateEvent;
import net.powermatcher.core.BidCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Concentrator.Config.class, immediate = true)
public class Concentrator implements MatcherRole, AgentRole, Observable {
	private static final Logger logger = LoggerFactory.getLogger(Concentrator.class);
	
	@Meta.OCD
	public static interface Config {
		@Meta.AD(deflt = "concentrator")
		String matcherId();

		@Meta.AD(deflt = "600", description = "Nr of seconds before a bid becomes invalidated")
		int bidTimeout();

		@Meta.AD(deflt = "60", description = "Number of seconds between bid updates")
		long bidUpdateRate();

		@Meta.AD(deflt = "concentrator")
		String agentId();
	}

	private TimeService timeService;

	@Reference
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	private ScheduledExecutorService scheduler;

	@Reference
	public void setExecutorService(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	private ScheduledFuture<?> scheduledFuture;

	private Session sessionToMatcher;

	private BidCache aggregatedBids;

	private Set<Session> sessionToAgents = new HashSet<Session>();

	private Config config;
	
	@Activate
	void activate(final Map<String, Object> properties) {
		config = Configurable.createConfigurable(Config.class, properties);
		
		// TODO remove marketref
		// TODO remove significance
		this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());

		scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				doBidUpdate();
			}
		}, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

		logger.info("Agent [{}], activated", config.agentId());
	}
	
	@Deactivate
	public void deactivate() {
		// TODO how to close all the sessions?
		for (Session session : sessionToAgents.toArray(new Session[sessionToAgents.size()])) {
			session.disconnect();
		}
		
		if(!sessionToAgents.isEmpty()) {
			logger.warn("Could not disconnect all sessions. Left: {}", sessionToAgents);
		}
		
		scheduledFuture.cancel(false);

		logger.info("Agent [{}], deactivated", config.agentId());
	}
	
	@Override
	public synchronized void connectToMatcher(Session session) {
		this.sessionToMatcher = session;
	}
	
	@Override
	public synchronized void disconnectFromMatcher(Session session) {
		for(Session agentSession : sessionToAgents) {
			agentSession.disconnect();
		}
		this.sessionToMatcher = null;
	}

	@Override
	public synchronized boolean connectToAgent(Session session) {
		if(this.sessionToMatcher == null) {
			return false;
		}
		
		this.sessionToAgents.add(session);
		session.setMarketBasis(this.sessionToMatcher.getMarketBasis());
		session.setClusterId(this.sessionToMatcher.getClusterId());
		
		this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.sessionToMatcher.getMarketBasis()));
		logger.info("Agent connected with session [{}]", session.getSessionId());
		return true;
	}

	@Override
	public synchronized void disconnectFromAgent(Session session) {
		// Find session
		if (!sessionToAgents.remove(session)) {
			return;
		}
		
		this.aggregatedBids.removeAgent(session.getSessionId());

		logger.info("Agent disconnected with session [{}]", session.getSessionId());
	}

	@Override
	public synchronized void updateBid(Session session, Bid newBid) {
		// TODO Auto-generated method stub
		if (!sessionToAgents.contains(session)) {
			// TODO throw exception
			return;
		}
		
		if (!newBid.getMarketBasis().equals(this.sessionToMatcher.getMarketBasis())) {
			// TODO throw exception
			return;
		}

		// TODO Agent ID is unknown
		this.publishEvent(new IncomingBidUpdateEvent(config.agentId(), session.getSessionId(), timeService.currentDate(), "agentId", newBid));

		// Update agent in aggregatedBids
		this.aggregatedBids.updateBid(session.getSessionId(), newBid);

		logger.info("Received bid update [{}] from session [{}]", newBid, session.getSessionId());
	}

	@Override
	public void updatePrice(Price newPrice) {
		logger.debug("Received price update [{}]", newPrice);

		// Publish new price to connected agents
		for (Session session : this.sessionToAgents) {
			session.updatePrice(newPrice);
		}
	}

	protected void doBidUpdate() {
		if (sessionToMatcher != null) {
			Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.sessionToMatcher.getMarketBasis());
			this.sessionToMatcher.updateBid(aggregatedBid);
			publishEvent(new OutgoingBidUpdateEvent(config.agentId(), sessionToMatcher.getSessionId(), timeService.currentDate(), aggregatedBid));
			
			logger.debug("Updating aggregated bid [{}]", aggregatedBid);
		}
	}

	// TODO refactor to separate (base)object
	private final Set<Observer> observers = new CopyOnWriteArraySet<Observer>();

	@Override
	public void addObserver(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	void publishEvent(UpdateEvent event) {
		for (Observer observer : observers) {
			observer.update(event);
		}
	}
}
