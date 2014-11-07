package net.powermatcher.core.auctioneer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.BidCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		@Meta.AD(deflt = "30", description = "Number of seconds between price updates")
		long priceUpdateRate();
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

	private BidCache aggregatedBids;

	private MarketBasis marketBasis;

	private Set<Session> sessions = new HashSet<Session>();

	private String matcherId;
	
	@Activate
	public void activate(final Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class, properties);
		
		// TODO remove marketref
		// TODO remove significance
		this.marketBasis = new MarketBasis(config.commodity(), config.currency(), config.priceSteps(), config.minimumPrice(), config.maximumPrice(), 2, 0);
		this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());
		this.matcherId = config.matcherId();
		
		scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				publishNewPrice();
			}
		}, 0, config.priceUpdateRate(), TimeUnit.SECONDS);
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
	public synchronized boolean connectToAgent(Session session) {
		session.setMarketBasis(marketBasis);
		session.setClusterId(matcherId);
		this.sessions.add(session);
		this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.marketBasis));
		logger.info("Agent connected with session [{}]", session.getSessionId());
		return true;
	}

	@Override
	public synchronized void disconnectFromAgent(Session session) {
		// Find session
		if (!sessions.remove(session)) {
			return;
		}
		
		this.aggregatedBids.removeAgent(session.getSessionId());

		logger.info("Agent disconnected with session [{}]", session.getSessionId());
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
		
		logger.debug("Received bid update [{}] from session [{}]", newBid, session.getSessionId());
	}

	/**
	 * Generates the new price out of the aggregated bids and sends this to all
	 * listeners
	 * TODO This is temporarily made public instead of default to test some things.
	 * This should be fixed as soon as possible.
	 */
	public synchronized void publishNewPrice() {
		Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.marketBasis);
		Price newPrice = determinePrice(aggregatedBid);
		for (Session session : this.sessions) {
			session.updatePrice(newPrice);
			logger.debug("New price: {}, session {}", newPrice, session.getSessionId());
		}
	}

	protected Price determinePrice(Bid aggregatedBid) {
		return aggregatedBid.calculateIntersection(0);
	}
}
