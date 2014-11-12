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
import net.powermatcher.api.monitoring.IncomingBidUpdateEvent;
import net.powermatcher.api.monitoring.Observable;
import net.powermatcher.api.monitoring.OutgoingPriceUpdateEvent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.core.monitoring.BaseObservable;

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
 * This class represents an {@link Auctioneer} component which will receive all {@link Bid} of other agents as a single
 * {@link Bid} or as an aggregate {@link Bid} via one or more {@link Concentrator}.
 * </p>
 * 
 * <p>
 * It is responsible for defining and sending the {@link MarketBasis} and calculating the equilibrium based on the
 * {@link Bid} from the different agents in the topology. This equilibrium is communicated to the agents down the
 * hierarchy in the form of price update messages.
 * 
 * The {@link Price} that is communicated contains a {@link Price} and a {@link MarketBasis} which enables the
 * conversion to a normalized {@link Price} or to any other {@link MarketBasis} for other financial calculation
 * purposes.
 * </p>
 * 
 * @author FAN
 * @version 1.0
 * 
 */
@Component(designateFactory = Auctioneer.Config.class, immediate = true, provide = { Observable.class,
        MatcherRole.class })
public class Auctioneer extends BaseObservable implements MatcherRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auctioneer.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "auctioneer")
        String matcherId();

        @Meta.AD(deflt = "electricity", description = "Commodity of the market basis")
        String commodity();

        @Meta.AD(deflt = "EUR", description = "Currency of the market basis")
        String currency();

        @Meta.AD(deflt = "100", description = "Number of price steps in the market basis")
        int priceSteps();

        @Meta.AD(deflt = "0", description = "Minimum price of the market basis")
        double minimumPrice();

        @Meta.AD(deflt = "1", description = "Maximum price of the market basis")
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
     * The bid cache maintains an aggregated {@link Bid}, where bids can be added and removed explicitly.
     */
    private BidCache aggregatedBids;

    /**
     * The {@link MarketBasis} for {@link Bid} and {@link Price}.
     */
    private MarketBasis marketBasis;

    /**
     * Holds the sessions from the agents.
     */
    private Set<Session> sessions = new HashSet<Session>();

    /**
     * Id of the {@link Auctioneer}.
     */
    private String matcherId;

    // TODO marketBasis, aggregatedBids and
    // matcherId are used in synchronized methods. Do we have do synchronize
    // activate? It's only called once, so maybe not.
    @Activate
    public void activate(final Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.marketBasis = new MarketBasis(config.commodity(), config.currency(), config.priceSteps(),
                config.minimumPrice(), config.maximumPrice());
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
        for (Session session : sessions.toArray(new Session[sessions.size()])) {
            session.disconnect();
            LOGGER.info("Session {} closed", session);
        }

        if (!sessions.isEmpty()) {
            LOGGER.warn("Could not disconnect all sessions. Left: {}", sessions);
        }

        scheduledFuture.cancel(false);
    }

    @Override
    public synchronized boolean connectToAgent(Session session) {
        session.setMarketBasis(marketBasis);
        session.setClusterId(matcherId);
        this.sessions.add(session);
        this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.marketBasis));
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

    @Override
    public synchronized void disconnectFromAgent(Session session) {
        // Find session
        if (!sessions.remove(session)) {
            return;
        }

        this.aggregatedBids.removeAgent(session.getSessionId());

        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
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

        LOGGER.debug("Received bid update [{}] from session [{}]", newBid, session.getSessionId());

        this.publishEvent(new IncomingBidUpdateEvent(matcherId, session.getSessionId(), timeService.currentDate(),
                session.getAgentId(), newBid));
    }

    @Override
    public String getObserverId() {
        return matcherId;
    }

    /**
     * Generates the new price out of the aggregated bids and sends this to all listeners TODO This is temporarily made
     * public instead of default to test some things. This should be fixed as soon as possible.
     */
    public synchronized void publishNewPrice() {
        Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.marketBasis);
        Price newPrice = determinePrice(aggregatedBid);

        for (Session session : this.sessions) {
            this.publishEvent(new OutgoingPriceUpdateEvent(matcherId, session.getSessionId(),
                    timeService.currentDate(), newPrice));

            session.updatePrice(newPrice);
            LOGGER.debug("New price: {}, session {}", newPrice, session.getSessionId());
        }
    }

    protected Price determinePrice(Bid aggregatedBid) {
        return aggregatedBid.calculateIntersection(0);
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setExecutorService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }
}
