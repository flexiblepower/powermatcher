package net.powermatcher.core.concentrator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.IncomingBidEvent;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.OutgoingBidEvent;
import net.powermatcher.api.monitoring.OutgoingPriceEvent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.auctioneer.Auctioneer;
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
 * This class represents a {@link Concentrator} component where several instances can be created.
 * </p>
 * 
 * <p>
 * The {@link Concentrator} receives {@link Bid} from the agents and forwards this in an aggregate {@link Bid} up in the
 * hierarchy to a {@link Concentrator} or to the {@link Auctioneer}. It will receive price updates from the
 * {@link Auctioneer} and forward them to its connected agents.
 * 
 * @author FAN
 * @version 1.0
 * 
 */
@Component(designateFactory = Concentrator.Config.class, immediate = true, provide = { ObservableAgent.class,
        MatcherRole.class, AgentRole.class })
public class Concentrator extends BaseObservable implements MatcherRole, AgentRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(Concentrator.class);

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
     * {@link Session} object for connecting to matcher
     */
    private Session sessionToMatcher;

    /**
     * The {@link Bid} cache maintains an aggregated {@link Bid}, where bids can be added and removed explicitly.
     */
    private BidCache aggregatedBids;

    /**
     * Holds the sessions from the agents.
     */
    private Set<Session> sessionToAgents = new HashSet<Session>();

    /**
     * OSGI configuration meta type with info about the concentrator.
     */
    private Config config;

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    @Reference
    public void setExecutorService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Activate
    public void activate(final Map<String, Object> properties) {
        config = Configurable.createConfigurable(Config.class, properties);

        this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());

        scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doBidUpdate();
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    // TODO sessionToMatcher is used in synchronized methods. Do we have do synchronize
    // deactivate? SessiontoMatcher is normally only set once, so maybe not.
    @Deactivate
    public void deactivate() {
        for (Session session : sessionToAgents.toArray(new Session[sessionToAgents.size()])) {
            session.disconnect();
        }

        if (sessionToMatcher != null) {
            sessionToMatcher.disconnect();
        }

        if (!sessionToAgents.isEmpty()) {
            LOGGER.warn("Could not disconnect all sessions. Left: {}", sessionToAgents);
        }

        scheduledFuture.cancel(false);

        LOGGER.info("Agent [{}], deactivated", config.agentId());
    }

    @Override
    public synchronized void connectToMatcher(Session session) {
        this.sessionToMatcher = session;
    }

    @Override
    public synchronized void disconnectFromMatcher(Session session) {
        for (Session agentSession : sessionToAgents.toArray(new Session[sessionToAgents.size()])) {
            agentSession.disconnect();
        }
        this.sessionToMatcher = null;
    }

    @Override
    public synchronized boolean connectToAgent(Session session) {
        if (this.sessionToMatcher == null) {
            return false;
        }

        this.sessionToAgents.add(session);
        session.setMarketBasis(this.sessionToMatcher.getMarketBasis());
        session.setClusterId(this.sessionToMatcher.getClusterId());

        this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.sessionToMatcher.getMarketBasis()));
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

    @Override
    public synchronized void disconnectFromAgent(Session session) {
        // Find session
        if (!sessionToAgents.remove(session)) {
            return;
        }

        this.aggregatedBids.removeAgent(session.getSessionId());

        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
    }

    @Override
    public synchronized void updateBid(Session session, Bid newBid) {

        if (!sessionToAgents.contains(session)) {
            throw new IllegalStateException("No session found");
        }
        if (!newBid.getMarketBasis().equals(this.sessionToMatcher.getMarketBasis())) {
            throw new IllegalArgumentException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        this.publishEvent(new IncomingBidEvent(config.agentId(), session.getSessionId(), timeService
                .currentDate(), "agentId", newBid));

        // Update agent in aggregatedBids
        this.aggregatedBids.updateBid(session.getSessionId(), newBid);

        LOGGER.info("Received bid update [{}] from session [{}]", newBid, session.getSessionId());
    }

    @Override
    public void updatePrice(Price newPrice) {
        LOGGER.debug("Received price update [{}]", newPrice);

        this.publishEvent(new IncomingPriceEvent(this.config.agentId(), this.sessionToMatcher.getSessionId(),
                timeService.currentDate(), newPrice));

        // Publish new price to connected agents
        for (Session session : this.sessionToAgents) {
            session.updatePrice(newPrice);

            this.publishEvent(new OutgoingPriceEvent(this.config.agentId(), session.getSessionId(), timeService
                    .currentDate(), newPrice));
        }
    }

    @Override
    public String getObserverId() {
        return this.config.agentId();
    }

    /**
     * sends the aggregatedbids to the matcher this method has temporarily been made public due to issues with the
     * scheduler. TODO fix this asap
     */
    public synchronized void doBidUpdate() {
        if (sessionToMatcher != null) {
            Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.sessionToMatcher.getMarketBasis());
            this.sessionToMatcher.updateBid(aggregatedBid);
            publishEvent(new OutgoingBidEvent(config.agentId(), sessionToMatcher.getSessionId(),
                    timeService.currentDate(), aggregatedBid));

            LOGGER.debug("Updating aggregated bid [{}]", aggregatedBid);
        }
    }
}
