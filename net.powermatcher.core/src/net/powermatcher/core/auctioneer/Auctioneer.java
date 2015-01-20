package net.powermatcher.core.auctioneer;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.concentrator.Concentrator;

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
 * It is responsible for defining and sending the {@link MarketBasis} and calculating the equilibrium based on the
 * {@link Bid} from the different agents in the topology. This equilibrium is communicated to the agents down the
 * hierarchy in the form of price update messages.
 * 
 * @author FAN
 * @version 2.0
 */
@Component(designateFactory = Auctioneer.Config.class, immediate = true, provide = { ObservableAgent.class,
        MatcherEndpoint.class })
public class Auctioneer extends BaseAgent implements MatcherEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auctioneer.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "auctioneer")
        String agentId();

        @Meta.AD(deflt = "DefaultCluster")
        String clusterId();

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
     * OSGi calls this method to activate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, Object> properties) {
        // TODO marketBasis, aggregatedBids and
        // matcherId are used in synchronized methods. Do we have do synchronize
        // activate? It's only called once, so maybe not.
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.marketBasis = new MarketBasis(config.commodity(), config.currency(), config.priceSteps(),
                config.minimumPrice(), config.maximumPrice());
        this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());
        this.setServicePid((String) properties.get("service.pid"));
        this.setClusterId(config.clusterId());
        this.setAgentId(config.agentId());

        scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                publishNewPrice();
            }
        }, 0, config.priceUpdateRate(), TimeUnit.SECONDS);
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        scheduledFuture.cancel(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean connectToAgent(Session session) {
        session.setMarketBasis(marketBasis);
        session.setClusterId(this.getClusterId());

        this.sessions.add(session);
        this.aggregatedBids.updateBid(getAgentId(), new ArrayBid.Builder(this.marketBasis).setDemand(0).build());
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void agentEndpointDisconnected(Session session) {
        // Find session
        if (!sessions.remove(session)) {
            return;
        }

        this.aggregatedBids.removeAgent(session.getSessionId());

        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void updateBid(Session session, Bid newBid) {
        if (!sessions.contains(session)) {
            throw new IllegalStateException("No session found");
        }

        if (!newBid.getMarketBasis().equals(this.marketBasis)) {
            throw new InvalidParameterException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        // Update agent in aggregatedBids
        this.aggregatedBids.updateBid(session.getAgentId(), newBid);

        LOGGER.debug("Received from session [{}] bid update [{}] ", session.getSessionId(), newBid);

        this.publishEvent(new IncomingBidEvent(session.getClusterId(), getAgentId(), session.getSessionId(),
                timeService.currentDate(), session.getAgentId(), newBid, Qualifier.AGENT));
    }

    /**
     * Generates the new {@link Price}, based on the aggregated bids. The {@link Price} is sent to the
     * {@link MatcherEndpoint} through the {@link Session}. An {@link OutgoingPriceUpdateEvent} is sent to all
     * {@link AgentObserver} listeners.
     */
    protected synchronized void publishNewPrice() {
        Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.marketBasis, false);
        Price newPrice = determinePrice(aggregatedBid);

        for (Session session : this.sessions) {
            ArrayBid lastBid = this.aggregatedBids.getLastBid(session.getAgentId());
            if (lastBid != null) {
                Integer bidNumber = lastBid.getBidNumber();
                PriceUpdate sessionPriceUpdate = new PriceUpdate(newPrice, bidNumber);
                this.publishEvent(new OutgoingPriceUpdateEvent(session.getClusterId(), getAgentId(), session
                        .getSessionId(), timeService.currentDate(), sessionPriceUpdate, Qualifier.MATCHER));
                session.updatePrice(sessionPriceUpdate);
                LOGGER.debug("New price: {}, session {}", sessionPriceUpdate, session.getSessionId());
            }
        }
    }

    /**
     * This method determines the {@link Price}, given the current aggregated {@link Bid}.
     * 
     * @param aggregatedBid
     *            the aggregated {@link Bid} used to determin the {@link Price}
     * @return the calculated {@link Price}
     */
    protected Price determinePrice(Bid aggregatedBid) {
        return aggregatedBid.calculateIntersection(0);
    }

    /**
     * @param the
     *            new {@link TimeService} implementation.
     */
    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    /**
     * @param the
     *            new {@link ScheduledExecutorService} implementation.
     */
    @Reference
    public void setExecutorService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @return the current value of aggregatedBids.
     */
    protected BidCache getAggregatedBids() {
        return this.aggregatedBids;
    }

    public boolean canEqual(Object other) {
        return other instanceof Auctioneer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        Auctioneer that = (Auctioneer) ((obj instanceof Auctioneer) ? obj : null);
        if (that == null) {
            return false;
        }

        if (this == that) {
            return true;
        }

        return canEqual(that) && this.aggregatedBids.equals(that.aggregatedBids)
                && this.marketBasis.equals(that.marketBasis) && this.sessions.equals(that.sessions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 211 * (aggregatedBids.hashCode() + marketBasis.hashCode());
    }
}
