package net.powermatcher.core.auctioneer;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.Agent;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.OutgoingPriceEvent;
import net.powermatcher.api.monitoring.Qualifier;
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
 * This class represents an {@link ObjectiveAuctioneer} component which will receive all {@link Bid} of other agents as
 * a single {@link Bid} or as an aggregate {@link Bid} via one or more {@link Concentrator}. If {@link ObjectiveAgent}
 * are active, the {@link ObjectiveAuctioneer} will also receive a {@link Bid} from the {@link ObjectiveAgent} as a
 * single {@link Bid}.
 * </p>
 * 
 * <p>
 * It is responsible for defining and sending the {@link MarketBasis} and calculating the equilibrium based on the
 * {@link Bid} from the different agents in the topology and the objective agent. This equilibrium is communicated to
 * the agents down the hierarchy in the form of price update messages and to the objective agent.
 * 
 * In order of aggregation the {@link Bid} from the device agents and objective agents, the {@link ObjectiveAuctioneer}
 * will first aggregate the device agents bid and secondly aggregate the {@link Bid} from the objective agent. After the
 * aggregation the {@link ObjectiveAuctioneer} will determine the price and sends it to the {@link Concentrator} /
 * device agents and the objective agent.
 * </p>
 * 
 * @author FAN
 * @version 1.0
 * 
 */
@Component(designateFactory = ObjectiveAuctioneer.Config.class, immediate = true, provide = { ObservableAgent.class,
        MatcherEndpoint.class })
public class ObjectiveAuctioneer extends Auctioneer implements MatcherEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveAuctioneer.class);

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "objectiveauctioneer")
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
     * Holds the objective agent
     */
    private ObjectiveEndpoint objectiveEndpoint;

    @Activate
    @Override
    public void activate(final Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.marketBasis = new MarketBasis(config.commodity(), config.currency(), config.priceSteps(),
                config.minimumPrice(), config.maximumPrice());
        this.aggregatedBids = new BidCache(this.timeService, config.bidTimeout());

        this.setClusterId(config.clusterId());
        this.setAgentId(config.agentId());

        scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                publishNewPrice();
            }
        }, 0, config.priceUpdateRate(), TimeUnit.SECONDS);
    }

    @Deactivate
    public void deactivate() {
        scheduledFuture.cancel(false);
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addObjectiveEndpoint(ObjectiveEndpoint objectiveEndpoint) {
        Agent agent = (Agent) objectiveEndpoint;

        if (agent.getAgentId() != null) {
            this.objectiveEndpoint = objectiveEndpoint;
            LOGGER.debug("Added new objective agent [{}]: ", agent.getAgentId());
        } else {
            throw new IllegalStateException("Only one objective agent can be added to the cluster");
        }
    }

    public void removeObjectiveEndpoint(ObjectiveEndpoint objectiveEndpoint) {
        if (this.objectiveEndpoint == objectiveEndpoint) {
            this.objectiveEndpoint = null;
            LOGGER.debug("Removed objective agent");
        } else {
            throw new IllegalStateException("This objective agent is not active and can't be removed");
        }
    }

    @Override
    public synchronized boolean connectToAgent(Session session) {
        session.setMarketBasis(marketBasis);
        session.setClusterId(this.getClusterId());

        this.sessions.add(session);
        this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.marketBasis));
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
        return true;
    }

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
     * Generates the new price out of the aggregated bids and sends this to all listeners. The listeners can be device
     * agents and objective agents. TODO This is temporarily made public instead of default to test some things. This
     * should be fixed as soon as possible.
     */
    @Override
    public synchronized void publishNewPrice() {
        // aggregate bid device agents
        Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.marketBasis);

        Price newPrice;
        // check if objective agent is active
        if (this.objectiveEndpoint != null) {
            // receive the aggregate bid from the objective agent
            Bid aggregatedObjectiveBid = this.objectiveEndpoint.handleAggregateBid(aggregatedBid);
            // aggregate again with device agent bid.
            Bid finalAggregatedBid = aggregatedBid.aggregate(aggregatedObjectiveBid);

            newPrice = determinePrice(finalAggregatedBid);
            // send price update to objective agent
            objectiveEndpoint.notifyPriceUpdate(newPrice);
        } else {
            newPrice = determinePrice(aggregatedBid);
        }

        // send price updates to device agents
        for (Session session : this.sessions) {
            this.publishEvent(new OutgoingPriceEvent(session.getClusterId(), getAgentId(), session.getSessionId(),
                    timeService.currentDate(), newPrice, Qualifier.MATCHER));

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

    protected BidCache getAggregatedBids() {
        return this.aggregatedBids;
    }
}
