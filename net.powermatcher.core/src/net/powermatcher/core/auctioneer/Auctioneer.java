package net.powermatcher.core.auctioneer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.BidCache;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = Auctioneer.Config.class, immediate = true)
public class Auctioneer implements MatcherRole {
	private static final Logger logger = LoggerFactory.getLogger(Auctioneer.class);
	
	@Meta.OCD
	public static interface Config {
		@Meta.AD(deflt = "auctioneer")
		String matcherId();
		
		@Meta.AD(deflt="electricity", description = "Commodity of the market basis")
		String commodity();

		@Meta.AD(deflt="EUR", description = "Currency of the market basis")
		String currency();

		@Meta.AD(deflt="100", description = "Number of price steps in the market basis")
		int priceSteps();

		@Meta.AD(deflt="0", description = "Minimum price of the market basis")
		double minimumPrice();

		@Meta.AD(deflt="1", description = "Maximum price of the market basis")
		double maximumPrice();

		@Meta.AD(deflt = "600", description = "Nr of seconds before a bid becomes invalidated")
		int bidTimeout();

		@Meta.AD(deflt = "60", description = "Number of seconds between price updates")
		long priceUpdateRate();
	}

	private TimeService timeService;

	@Reference
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	private ScheduledExecutorService executorService;

	@Reference
	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}
	
	private ScheduledFuture<?> scheduledFuture;

	private BidCache aggregatedBids;

	private MarketBasis marketBasis;

	private Set<Session> sessions = new HashSet<Session>();

	@Activate
	void activate(final Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class, properties);
		
		// TODO remove marketref
		// TODO remove significance
		this.marketBasis = new MarketBasis(config.commodity(), config.currency(), config.priceSteps(), config.minimumPrice(), config.maximumPrice(), 2, 0);
		this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());
		
		scheduledFuture = this.executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				publishNewPrice();
			}
		}, config.priceUpdateRate(), config.priceUpdateRate(), TimeUnit.SECONDS);
	}
	
	@Deactivate
	public void deactivate() {
		// TODO how to close all the sessions?
		for (Session session : sessions.toArray(new Session[sessions.size()])) {
			session.disconnect();
		}
		
		if(!sessions.isEmpty()) {
			logger.warn("Could not disconnect all sessions. Left: {}", sessions);
		}
		
		scheduledFuture.cancel(false);
	}
	
	@Override
	public synchronized Session connect(AgentRole agentRole) {
		// Create Session
		Session session = new Session(this.marketBasis, UUID.randomUUID().toString(), agentRole, this);
		this.sessions.add(session);
	
		this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.marketBasis));
		
		return session;
	}

	@Override
	public synchronized void disconnect(Session session) {
		// Find session
		if (!sessions.remove(session)) {
			return;
		}
		
		this.aggregatedBids.removeAgent(session.getSessionId());
	}

	@Override
	public synchronized void updateBid(Session session, Bid newBid) {
		// TODO Auto-generated method stub
		if (!sessions.contains(session)) {
			// TODO throw exception
			return;
		}
		
		if (!newBid.getMarketBasis().equals(this.marketBasis)) {
			// TODO throw exception
			return;
		}
		
		// Update agent in aggregatedBids
		this.aggregatedBids.updateBid(session.getSessionId(), newBid);
	}

	synchronized void publishNewPrice() {
		Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.marketBasis);
		Price newPrice = determinePrice(aggregatedBid);
		logger.debug("New price: {}", newPrice);
		for (Session session : this.sessions) {
			session.getAgentRole().updatePrice(newPrice);
		}
	}

	protected Price determinePrice(Bid aggregatedBid) {
		return aggregatedBid.calculateIntersection(0);
	}
}
