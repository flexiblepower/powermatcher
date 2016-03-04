package net.powermatcher.core;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.measure.Measure;
import javax.measure.unit.SI;

import net.powermatcher.api.Agent;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.AggregatedBidEvent;
import net.powermatcher.api.monitoring.events.IncomingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;
import net.powermatcher.core.concentrator.Concentrator;

/**
 * This is an abstract class providing base functionality for a {@link MatcherEndpoint}. This class can be extended to
 * build a matcher, such as a {@link Concentrator} or an {@link Auctioneer}.
 */
public abstract class BaseMatcherEndpoint
    extends BaseAgent
    implements MatcherEndpoint {

    public static final Agent.Status NOT_CONNECTED = new Agent.Status() {
        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public MarketBasis getMarketBasis() {
            throw new IllegalStateException("Agent not connected to a cluster");
        }

        @Override
        public String getClusterId() {
            throw new IllegalStateException("Agent not connected to a cluster");
        }
    };

    /**
     * The {@link Connected} object describes the current status and configuration of an {@link BaseMatcherEndpoint}.
     * This status can be queried through the {@link BaseMatcherEndpoint#getStatus()} method and will give a snapshot of
     * the state at that time.
     */
    public static class Connected
        implements Agent.Status {
        private final String clusterId;
        private final MarketBasis marketBasis;

        /**
         * Creates a new {@link Connected} object.
         *
         * @param clusterId
         *            the current cluster identifier of the cluster this matcher is connected to
         * @param marketBasis
         *            the {@link MarketBasis} of the cluster this matcher is connected to
         */
        public Connected(String clusterId, MarketBasis marketBasis) {
            if (clusterId == null) {
                throw new NullPointerException("clusterId");
            } else if (marketBasis == null) {
                throw new NullPointerException("marketBasis");
            }
            this.clusterId = clusterId;
            this.marketBasis = marketBasis;
        }

        @Override
        public String getClusterId() {
            return clusterId;
        }

        @Override
        public MarketBasis getMarketBasis() {
            return marketBasis;
        }

        @Override
        public boolean isConnected() {
            return true;
        }
    }

    /**
     * PowerMatcher is an event driven system. The RateLimitedBidPublisher makes sure that when aggregating bids, not
     * too much (aggregated) {@link BidUpdate}s are being send. The RateLimitedBidPublisher introduces a cooling-off
     * period (typically one second). If a {@link BidUpdate} is being sent within this cooling-off period, a new
     * {@link AggregatedBid} will not be generated until the cooling-off period is finished. If multiple
     * {@link BidUpdate}s have been send in the cooling-off period, only one {@link AggregatedBid} will be generated
     * after the period.
     */
    public class RateLimitedBidPublisher
        implements Runnable {
        private final long minTimeBetweenUpdates;

        // Timestamp at which the cool down period ends (and the Concentrator is allow to send a new BidUpdate again)
        private volatile long coolingOffEnds = 0;

        // Indicates if there is already a BidUpdate scheduled at the end of the cooldown period
        private volatile Future<?> bidUpdateSchedule = null;

        /**
         * Create a new RateLimitedBidPublisher instance
         *
         * @param minTimeBetweenUpdates
         *            The minimum time (expressed in milliseconds) between two {@link BidUpdate}s (typically 1000ms).
         */
        public RateLimitedBidPublisher(long minTimeBetweenUpdates) {
            this.minTimeBetweenUpdates = minTimeBetweenUpdates;
        }

        @Override
        public void run() {
            final Agent.Status currentStatus = getStatus();
            try {
                if (currentStatus.isConnected()) {
                    AggregatedBid aggregatedBid = bidCache.aggregate();
                    publishEvent(new AggregatedBidEvent(currentStatus.getClusterId(),
                                                        getAgentId(),
                                                        now(),
                                                        aggregatedBid));
                    performUpdate(aggregatedBid);
                }
            } catch (RuntimeException e) {
                LOGGER.error("doBidUpate failed for matcher " + getAgentId(), e);
            } finally {
                synchronized (this) {
                    bidUpdateSchedule = null;
                    coolingOffEnds = context.currentTimeMillis() + minTimeBetweenUpdates;
                }
            }
        }

        synchronized void schedule() {
            if (bidUpdateSchedule == null) {
                // There is no aggregation scheduled yet
                long waitTime = coolingOffEnds - context.currentTimeMillis();
                if (waitTime > 0) {
                    // We're in the cooling-off period
                    bidUpdateSchedule = context.schedule(this,
                                                         Measure.valueOf(waitTime, SI.MILLI(SI.SECOND)));
                } else {
                    // Not in a cooling-off period, do it right away!
                    bidUpdateSchedule = context.submit(this);
                }
            }
        }

        synchronized void cancel() {
            if (bidUpdateSchedule != null) {
                bidUpdateSchedule.cancel(false);
                bidUpdateSchedule = null;
            }
        }
    }

    private volatile String agentId;
    private volatile Agent.Status status;
    private volatile BidCache bidCache;
    private volatile RateLimitedBidPublisher bidUpdater;

    public BaseMatcherEndpoint() {
        status = NOT_CONNECTED;
        agentId = null;
        bidCache = null;
        bidUpdater = null;
    }

    @Override
    public String getAgentId() {
        return agentId;
    }

    @Override
    public Agent.Status getStatus() {
        return status;
    }

    /**
     * This method should always be called during activation of the agent. It sets the agentId and desiredParentId. This
     * will also call the {@link #init(String)} method, so that call is no longer needed.
     *
     * @param agentId
     *            The agentId that should be used by this {@link Agent}. This will be returned when the
     *            {@link #getStatus()} is called.
     *
     * @throws IllegalArgumentException
     *             when either the agentId or the desiredParentId is null or is an empty string.
     */
    protected void init(String agentId) {
        if (agentId == null || agentId.isEmpty()) {
            throw new IllegalArgumentException("The agentId may not be null or empty");
        }
        this.agentId = agentId;
    }

    public void configure(MarketBasis marketBasis, String clusterId, long minTimeBetweenUpdates) {
        bidCache = new BidCache(marketBasis);
        status = new Connected(clusterId, marketBasis);
        bidUpdater = new RateLimitedBidPublisher(minTimeBetweenUpdates);
    }

    public void unconfigure() {
        for (Iterator<Session> it = sessions.values().iterator(); it.hasNext();) {
            Session session = it.next();
            session.disconnect();
            it.remove();
        }

        bidUpdater.cancel();
        bidUpdater = null;
        bidCache = null;
        status = NOT_CONNECTED;
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    @Override
    public void connectToAgent(Session session) {
        synchronized (sessions) {
            Agent.Status currentStatus = getStatus();
            if (!currentStatus.isConnected()) {
                throw new IllegalStateException("This matcher is not yet connected to the cluster");
            } else if (!sessions.containsKey(session.getAgentId())) {
                session.setMarketBasis(currentStatus.getMarketBasis());
                sessions.put(session.getAgentId(), session);
                LOGGER.info("Agent connected with session [{}]", session.getSessionId());
            } else {
                throw new IllegalStateException("An agent with id [" + session.getAgentId()
                                                + "] was already connected");
            }
        }
    }

    @Override
    public void agentEndpointDisconnected(Session session) {
        synchronized (sessions) {
            Session foundSession = sessions.get(session.getAgentId());
            if (session.equals(foundSession)) {
                sessions.remove(session.getAgentId());
                bidCache.removeBidOfAgent(session.getAgentId());
                bidUpdater.schedule();
                LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
            }
        }
    }

    public void publishPrice(Price price, AggregatedBid aggregatedBid) {
        Map<String, Integer> references = aggregatedBid.getAgentBidReferences();

        for (Session session : sessions.values()) {
            Integer bidNumber = references.get(session.getAgentId());
            if (bidNumber != null) {
                PriceUpdate priceUpdate = new PriceUpdate(price, bidNumber);
                publishEvent(new OutgoingPriceUpdateEvent(status.getClusterId(),
                                                          getAgentId(),
                                                          session.getSessionId(),
                                                          context.currentTime(),
                                                          priceUpdate));
                LOGGER.debug("New price: {}, session {}", priceUpdate, session.getSessionId());

                try {
                    session.updatePrice(priceUpdate);
                } catch (RuntimeException ex) {
                    LOGGER.warn("Unexpected exception while send priceupdate to " + session.getAgentId(), ex);
                }
            }
        }
    }

    public final AggregatedBid aggregate() {
        return bidCache.aggregate();
    }

    protected abstract void performUpdate(AggregatedBid aggregatedBid);

    @Override
    public void handleBidUpdate(Session session, BidUpdate bidUpdate) {
        Agent.Status currentStatus = getStatus();

        if (!currentStatus.isConnected()) {
            throw new IllegalStateException("Not connected to the cluster");
        }

        if (session == null || !sessions.containsKey(session.getAgentId())) {
            throw new IllegalStateException("No session found");
        }

        if (bidUpdate == null || !bidUpdate.getBid().getMarketBasis().equals(currentStatus.getMarketBasis())) {
            throw new InvalidParameterException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        // Update agent in aggregatedBids
        bidCache.updateAgentBid(session.getAgentId(), bidUpdate);

        LOGGER.debug("Received from session [{}] bid update [{}] ", session.getSessionId(), bidUpdate);

        publishEvent(new IncomingBidUpdateEvent(currentStatus.getClusterId(),
                                                getAgentId(),
                                                session.getSessionId(),
                                                context.currentTime(),
                                                session.getAgentId(),
                                                bidUpdate));

        bidUpdater.schedule();
    }
}
