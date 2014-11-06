package net.powermatcher.core.auctioneer;

import java.security.InvalidParameterException;
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

/**
 * <p>
 * This class represents an auctioneer component which will receive all bids of other agents
 * as a single bid or as an aggregate bid via one or more concentrators.
 * </p>
 * 
 * <p>
 * 
 * It is responsible for defining and sending the market basis and calculating the equilibrium based on the bids 
 * from the different agents in the topology.
 * This equilibrium is communicated to the agents down the hierarchy in the form of price update messages.
 * 
 * The price that is communicated contains a price and a market basis which enables the conversion to a normalized price 
 * or to any other market basis for other financial calculation purposes.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
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

	/**
     * TimeService that is used for obtaining real or simulated time.
     */
	private TimeService timeService;

	/**
	 * Scheduler that can schedule commands to run after a given delay, or to execute periodically.
	 */
	private ScheduledExecutorService scheduler;
	
	/**
	 * A delayed result-bearing action that can be cancelled.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/**
	 * The bid cache maintains an aggregated bid, where bids can be added and
     * removed explicitly.
	 */
	private BidCache aggregatedBids;
	
	/**
	 * The marketBasis specifies The PowerMatcher market basis for bids and prices.
	 */
	private MarketBasis marketBasis;
	
	/**
	 * Holds the sessions from the agents.
	 */
	private Set<Session> sessions = new HashSet<Session>();
	
	/**
	 * Name/Id of the Auctioneer.
	 */
	private String matcherId;
	
	@Reference
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	@Reference
	public void setExecutorService(ScheduledExecutorService scheduler) {
		this.scheduler = scheduler;
	}
	
	@Activate
	void activate(final Map<String, Object> properties) {
		Config config = Configurable.createConfigurable(Config.class, properties);
		
		this.marketBasis = new MarketBasis(config.commodity(), config.currency(), config.priceSteps(), config.minimumPrice(), config.maximumPrice());
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
			logger.info("Session {} closed", session);
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
		if (!sessions.contains(session)) {
			throw new IllegalStateException("No session found");
		}
		
		if (!newBid.getMarketBasis().equals(this.marketBasis)) {
			throw new InvalidParameterException("Marketbasis new bid differs from marketbasis auctioneer");
		}
		
		// Update agent in aggregatedBids
		this.aggregatedBids.updateBid(session.getSessionId(), newBid);
		
		logger.debug("Received bid update [{}] from session [{}]", newBid, session.getSessionId());
	}

	synchronized void publishNewPrice() {
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
