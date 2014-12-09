package net.powermatcher.core.concentrator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.IncomingBidEvent;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.OutgoingBidEvent;
import net.powermatcher.api.monitoring.OutgoingPriceEvent;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.BidCacheSnapshot;
import net.powermatcher.core.auctioneer.Auctioneer;

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
        MatcherEndpoint.class, AgentEndpoint.class })
public class Concentrator extends BaseAgent implements MatcherEndpoint, AgentEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(Concentrator.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator")
        String agentId();

        @Meta.AD(deflt = "auctioneer")
        String desiredParentId();

        @Meta.AD(deflt = "60", description = "Number of seconds between bid updates")
        long bidUpdateRate();
    }

    /**
     * TimeService that is used for obtaining real or simulated time.
     */
    private TimeService timeService;

    /**
     * The scheduler that is used to schedule the bid updates during activation and thus must be set before activation.
     */
    private ScheduledExecutorService scheduler;

    /**
     * The schedule that is running the bid updates. This is created in the {@link #activate(Map)} method and cancelled
     * in the {@link #deactivate()} method.
     */
    private ScheduledFuture<?> bidUpdateSchedule;

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
    protected Config config;

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

        this.setAgentId(config.agentId());
        this.setDesiredParentId(config.desiredParentId());
        // Since the cleanup is never called, the expiration time is useless
        // TODO: how should we deal with this cleanup?
        this.aggregatedBids = new BidCache(this.timeService, 0);

        bidUpdateSchedule = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    doBidUpdate();
                } catch (IllegalStateException | IllegalArgumentException e) {
                    LOGGER.error("doBidUpate failed for Concentrator " + config.agentId(), e);
                }
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    @Deactivate
    public void deactivate() {
        bidUpdateSchedule.cancel(false);

        LOGGER.info("Agent [{}], deactivated", config.agentId());
    }

    @Override
    public synchronized void connectToMatcher(Session session) {
        this.sessionToMatcher = session;
        setClusterId(session.getClusterId());
    }

    @Override
    public synchronized void matcherEndpointDisconnected(Session session) {
        for (Session agentSession : sessionToAgents.toArray(new Session[sessionToAgents.size()])) {
            agentSession.disconnect();
        }
        setClusterId(null);
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

        this.aggregatedBids.updateBid(session.getAgentId(), new ArrayBid.Builder(this.sessionToMatcher.getMarketBasis()).setDemand(0).build());
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

    @Override
    public synchronized void agentEndpointDisconnected(Session session) {
        // Find session
        if (!sessionToAgents.remove(session)) {
            return;
        }

        this.aggregatedBids.removeAgent(session.getSessionId());

        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
    }

    @Override
    public synchronized void updateBid(Session session, Bid newBid) throws IllegalStateException,
            IllegalArgumentException {

        if (!sessionToAgents.contains(session)) {
            throw new IllegalStateException("No session found");
        }
        if (!newBid.getMarketBasis().equals(this.sessionToMatcher.getMarketBasis())) {
            throw new IllegalArgumentException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        this.publishEvent(new IncomingBidEvent(session.getClusterId(), config.agentId(), session.getSessionId(),
                timeService.currentDate(), "agentId", newBid, Qualifier.AGENT));

        // Update agent in aggregatedBids
        this.aggregatedBids.updateBid(session.getAgentId(), newBid);

        LOGGER.info("Received from session [{}] bid update [{}] ", session.getSessionId(), newBid);
    }

    @Override
    public synchronized void updatePrice(PriceUpdate priceUpdate) {
        if (priceUpdate == null) {
            String message = "Price cannot be null";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }
        
        LOGGER.debug("Received price update [{}]", priceUpdate);
        this.publishEvent(new IncomingPriceEvent(sessionToMatcher.getClusterId(), this.config.agentId(),
                this.sessionToMatcher.getSessionId(), timeService.currentDate(), priceUpdate.getPrice(), Qualifier.AGENT));

        // Find bidCacheSnapshot belonging to the newly received price update
        BidCacheSnapshot bidCacheSnapshot = this.aggregatedBids.getMatchingSnapshot(priceUpdate.getBidNumber());
        if (bidCacheSnapshot == null) {
        	// ignore price and log warning
        	LOGGER.warn("Received a price update for a bid that I never sent, id: {}", priceUpdate.getBidNumber());
        	return;
        }
        
        
        // Publish new price to connected agents
        for (Session session : this.sessionToAgents) {
        	Integer originalAgentBid = bidCacheSnapshot.getBidNumbers().get(session.getAgentId());    
        	if (originalAgentBid == null) {
        		// ignore price for this agent and log warning
        		continue;
        	} 

        	PriceUpdate agentPriceUpdate = new PriceUpdate(priceUpdate.getPrice(), originalAgentBid);
        	
            session.updatePrice(agentPriceUpdate);

            this.publishEvent(new OutgoingPriceEvent(session.getClusterId(), this.config.agentId(), session.getSessionId(), timeService
                    .currentDate(), priceUpdate.getPrice(), Qualifier.MATCHER));

        }
    }

    protected synchronized void doBidUpdate() {
        if (sessionToMatcher != null) {
            Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.sessionToMatcher.getMarketBasis());

            aggregatedBid = transformBid(aggregatedBid);

            this.sessionToMatcher.updateBid(aggregatedBid);
            publishEvent(new OutgoingBidEvent(sessionToMatcher.getClusterId(), config.agentId(),
                    sessionToMatcher.getSessionId(), timeService.currentDate(), aggregatedBid, Qualifier.MATCHER));

            LOGGER.debug("Updating aggregated bid [{}]", aggregatedBid);
        }
    }

    /**
     * This method should be overridden when the bid that will be sent has to be changed.
     * 
     * @param aggregatedBid
     *            The (input) aggregated bid as calculated normally (the sum of all the bids of the agents).
     * @return The bid that will be sent to the matcher that is connected to this {@link Concentrator}.
     */
    protected Bid transformBid(Bid aggregatedBid) {
        return aggregatedBid;
    }
}
