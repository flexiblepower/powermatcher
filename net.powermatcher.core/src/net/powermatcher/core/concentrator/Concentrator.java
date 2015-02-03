package net.powermatcher.core.concentrator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.WhitelistableMatcherEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
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
 * @version 2.0
 */
@Component(designateFactory = Concentrator.Config.class,
           immediate = true,
           provide = { ObservableAgent.class,
                      MatcherEndpoint.class,
                      AgentEndpoint.class,
                      WhitelistableMatcherEndpoint.class })
public class Concentrator
    extends BaseAgent
    implements MatcherEndpoint, AgentEndpoint {

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
     * The schedule that is running the bid updates. This is created in the {@link #activate(Map)} method and cancelled
     * in the {@link #deactivate()} method.
     */
    private ScheduledFuture<?> bidUpdateSchedule;

    /**
     * {@link Session} object for connecting to matcher
     */
    private volatile Session sessionToMatcher;

    /**
     * The {@link Bid} cache maintains an aggregated {@link Bid}, where bids can be added and removed explicitly.
     */
    private volatile BidCache aggregatedBids;

    /**
     * Holds the sessions from the agents.
     */
    private final Map<String, Session> sessionToAgents = new ConcurrentHashMap<String, Session>();

    /**
     * OSGI configuration meta type with info about the concentrator.
     */
    protected Config config;

    /**
     * OSGi calls this method to activate a managed service.
     *
     * @param properties
     *            the configuration properties
     */
    @Activate
    public void activate(final Map<String, Object> properties) {
        activate(Configurable.createConfigurable(Config.class, properties));
    }

    /**
     * Convenient activate method that takes a {@link Config} object. This also makes subclassing easier.
     *
     * @param config
     *            The {@link Config} object that configures this concentrator
     */
    public void activate(Config config) {
        this.config = config;
        setAgentId(config.agentId());
        setDesiredParentId(config.desiredParentId());
        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    /**
     * @param the
     *            new {@link ScheduledExecutorService} implementation.
     */
    @Override
    public void setExecutorService(ScheduledExecutorService scheduler) {
        super.setExecutorService(scheduler);

        Runnable command = new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                try {
                    doBidUpdate();
                } catch (IllegalStateException e) {
                    LOGGER.error("doBidUpate failed for Concentrator " + config.agentId(), e);
                } catch (IllegalArgumentException e) {
                    LOGGER.error("doBidUpate failed for Concentrator " + config.agentId(), e);
                }
            }
        };

        bidUpdateSchedule = scheduler.scheduleAtFixedRate(command, 0, config.bidUpdateRate(), TimeUnit.SECONDS);
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     */
    @Deactivate
    public void deactivate() {
        bidUpdateSchedule.cancel(false);

        LOGGER.info("Agent [{}], deactivated", config.agentId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToMatcher(Session session) {
        sessionToMatcher = session;
        aggregatedBids = new BidCache(session.getMarketBasis());
        setClusterId(session.getClusterId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void matcherEndpointDisconnected(Session session) {
        for (Session agentSession : sessionToAgents.values().toArray(new Session[sessionToAgents.size()])) {
            agentSession.disconnect();
        }
        setClusterId(null);
        aggregatedBids = null;
        sessionToMatcher = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean connectToAgent(Session session) {
        if (sessionToMatcher == null) {
            return false;
        } else if (!sessionToAgents.containsKey(session.getAgentId())) {
            session.setMarketBasis(sessionToMatcher.getMarketBasis());
            sessionToAgents.put(session.getAgentId(), session);
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
        Session foundSession = sessionToAgents.get(session.getAgentId());
        if (!foundSession.equals(session)) {
            return;
        } else {
            sessionToAgents.remove(session.getAgentId());

            aggregatedBids.removeBidOfAgent(session.getAgentId());

            LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBidUpdate(Session session, Bid newBid) {
        if (!sessionToAgents.containsKey(session.getAgentId())) {
            throw new IllegalStateException("No session found");
        }

        if (!newBid.getMarketBasis().equals(sessionToMatcher.getMarketBasis())) {
            throw new IllegalArgumentException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        publishEvent(new IncomingBidEvent(session.getClusterId(),
                                          config.agentId(),
                                          session.getSessionId(),
                                          timeService.currentDate(),
                                          "agentId",
                                          newBid));

        // Update agent in aggregatedBids
        aggregatedBids.updateAgentBid(session.getAgentId(), newBid);

        LOGGER.info("Received from session [{}] bid update [{}] ", session.getSessionId(), newBid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        if (priceUpdate == null) {
            String message = "Price cannot be null";
            LOGGER.error(message);
            throw new IllegalArgumentException(message);
        }

        LOGGER.debug("Received price update [{}]", priceUpdate);
        publishEvent(new IncomingPriceUpdateEvent(sessionToMatcher.getClusterId(),
                                                  config.agentId(),
                                                  sessionToMatcher.getSessionId(),
                                                  timeService.currentDate(),
                                                  priceUpdate));

        try {
            AggregatedBid aggregatedBid = aggregatedBids.retreiveAggregatedBid(priceUpdate.getBidNumber());
            Price price = transformPrice(priceUpdate.getPrice(), aggregatedBid.getAggregatedBid());

            // Publish new price to connected agents
            for (Session session : sessionToAgents.values()) {
                Integer originalAgentBid = aggregatedBid.getAgentBidReferences().get(session.getAgentId());
                if (originalAgentBid != null) {
                    PriceUpdate agentPriceUpdate = new PriceUpdate(price, originalAgentBid);
                    session.updatePrice(agentPriceUpdate);

                    publishEvent(new OutgoingPriceUpdateEvent(session.getClusterId(),
                                                              config.agentId(),
                                                              session.getSessionId(),
                                                              timeService.currentDate(),
                                                              priceUpdate));
                }
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Received a price update for a bid that I never sent, id: {}", priceUpdate.getBidNumber());
        }
    }

    /**
     * Aggregates the bids and sends them to the matching agent.
     */
    final void doBidUpdate() {
        if (sessionToMatcher != null && aggregatedBids != null && isInitialized()) {
            AggregatedBid aggregatedBid = aggregatedBids.aggregate();

            Bid bid = transformBid(aggregatedBid.getAggregatedBid());

            sessionToMatcher.updateBid(bid);
            publishEvent(new OutgoingBidEvent(sessionToMatcher.getClusterId(),
                                              config.agentId(),
                                              sessionToMatcher.getSessionId(),
                                              now(),
                                              bid));

            LOGGER.debug("Updating aggregated bid [{}]", bid);
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

    /**
     * This method should be overridden when the price that will be sent down has to be changed. This is called just
     * before the price will be sent down to the connected agents.
     *
     * @param price
     *            The input price update as received from the connected matcher.
     * @param aggregatedBid
     *            The {@link AggregatedBid} that has lead to this price update.
     * @return The {@link Price} as it has to be sent to the connected agents.
     */
    protected Price transformPrice(Price price, Bid bid) {
        return price;
    }
}
