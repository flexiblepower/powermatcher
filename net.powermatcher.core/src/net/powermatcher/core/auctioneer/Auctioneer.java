package net.powermatcher.core.auctioneer;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;
import net.powermatcher.core.concentrator.Concentrator;

import org.flexiblepower.context.FlexiblePowerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
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
@Component(designateFactory = Auctioneer.Config.class,
           immediate = true,
           provide = { ObservableAgent.class, MatcherEndpoint.class })
public class Auctioneer
    extends BaseAgent
    implements MatcherEndpoint {

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
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * The bid cache maintains an aggregated {@link Bid}, where bids can be added and removed explicitly.
     */
    protected BidCache aggregatedBids;

    /**
     * The {@link MarketBasis} for {@link Bid} and {@link Price}.
     */
    private MarketBasis marketBasis;

    /**
     * Holds the sessions from the agents.
     */
    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    private Config config;

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
        config = Configurable.createConfigurable(Config.class, properties);
        marketBasis = new MarketBasis(config.commodity(),
                                      config.currency(), config.priceSteps(), config.minimumPrice(),
                                      config.maximumPrice());
        aggregatedBids = new BidCache(marketBasis);
        setClusterId(config.clusterId());
        setAgentId(config.agentId());
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
    public boolean connectToAgent(Session session) {
        if (!sessions.containsKey(session.getAgentId())) {
            session.setMarketBasis(marketBasis);
            sessions.put(session.getAgentId(), session);
            LOGGER.info("Agent connected with session [{}]", session.getSessionId());
            return true;
        } else {
            LOGGER.warn("An agent with id [{}] was already connected", session.getAgentId());
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentEndpointDisconnected(Session session) {
        // Find session
        Session foundSession = sessions.get(session.getAgentId());
        if (!foundSession.equals(session)) {
            return;
        } else {
            sessions.remove(session.getAgentId());

            aggregatedBids.removeBidOfAgent(session.getAgentId());

            LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBidUpdate(Session session, Bid newBid) {
        if (!sessions.containsKey(session.getAgentId())) {
            throw new IllegalStateException("No session found");
        }

        if (!newBid.getMarketBasis().equals(marketBasis)) {
            throw new InvalidParameterException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        // Update agent in aggregatedBids
        aggregatedBids.updateAgentBid(session.getAgentId(), newBid);

        LOGGER.debug("Received from session [{}] bid update [{}] ", session.getSessionId(), newBid);

        publishEvent(new IncomingBidEvent(session.getClusterId(),
                                          getAgentId(),
                                          session.getSessionId(),
                                          context.currentTime(),
                                          session.getAgentId(),
                                          newBid));
    }

    /**
     * Generates the new {@link Price}, based on the aggregated bids. The {@link Price} is sent to the
     * {@link MatcherEndpoint} through the {@link Session}. An {@link OutgoingPriceUpdateEvent} is sent to all
     * {@link AgentObserver} listeners.
     */
    protected void publishNewPrice() {
        AggregatedBid aggregatedBid = aggregatedBids.aggregate();
        Map<String, Integer> bidReferences = aggregatedBid.getAgentBidReferences();

        Price newPrice = determinePrice(aggregatedBid.getAggregatedBid());
        for (Session session : sessions.values()) {
            Integer bidReference = bidReferences.get(session.getAgentId());

            if (bidReference != null) {
                PriceUpdate sessionPriceUpdate = new PriceUpdate(newPrice, bidReference);
                publishEvent(new OutgoingPriceUpdateEvent(session.getClusterId(),
                                                          getAgentId(),
                                                          session.getSessionId(),
                                                          context.currentTime(),
                                                          sessionPriceUpdate));
                LOGGER.debug("New price: {}, session {}", sessionPriceUpdate, session.getSessionId());
                session.updatePrice(sessionPriceUpdate);
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
     *            new {@link ScheduledExecutorService} implementation.
     */
    @Override
    public void setContext(FlexiblePowerContext context) {
        this.context = context;
        Runnable command = new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                publishNewPrice();
            }
        };
        scheduledFuture = context.getScheduler().scheduleAtFixedRate(command,
                                                                     0,
                                                                     config.priceUpdateRate(),
                                                                     TimeUnit.SECONDS);
    }
}
