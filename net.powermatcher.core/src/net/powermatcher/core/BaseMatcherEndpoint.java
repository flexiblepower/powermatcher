package net.powermatcher.core;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.measure.Measure;
import javax.measure.unit.SI;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.AggregatedBidEvent;
import net.powermatcher.api.monitoring.events.IncomingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;

public abstract class BaseMatcherEndpoint
    extends BaseAgent
    implements MatcherEndpoint {

    private BidCache bidCache;

    private long minTimeBetweenUpdates;

    @Override
    protected void configure(MarketBasis marketBasis, String clusterId) {
        throw new AssertionError("The configure method of the BaseMatcherEndpoint should not be called directly, use the configure(marketBasis, clusterId, minTimeBetweenBids)");
    };

    public void configure(MarketBasis marketBasis, String clusterId, long minTimeBetweenUpdates) {
        synchronized (lock) {
            super.configure(marketBasis, clusterId);
            bidCache = new BidCache(marketBasis);
            this.minTimeBetweenUpdates = minTimeBetweenUpdates;
        }
    }

    @Override
    public void unconfigure() {
        synchronized (lock) {
            for (Iterator<Session> it = sessions.values().iterator(); it.hasNext();) {
                Session session = it.next();
                session.disconnect();
                it.remove();
            }
            super.unconfigure();
            bidCache = null;
            minTimeBetweenUpdates = 0;
        }
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    @Override
    public void connectToAgent(Session session) {
        synchronized (lock) {
            if (!isConnected()) {
                throw new IllegalStateException("This matcher is not yet connected to the cluster");
            } else if (!sessions.containsKey(session.getAgentId())) {
                session.setMarketBasis(getMarketBasis());
                sessions.put(session.getAgentId(), session);
                LOGGER.info("Agent connected with session [{}]", session.getSessionId());
            } else {
                throw new IllegalStateException("An agent with id [" + session.getAgentId() + "] was already connected");
            }
        }
    }

    @Override
    public void agentEndpointDisconnected(Session session) {
        synchronized (lock) {
            Session foundSession = sessions.get(session.getAgentId());
            if (session.equals(foundSession)) {
                sessions.remove(session.getAgentId());
                bidCache.removeBidOfAgent(session.getAgentId());
                doUpdate();
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
                publishEvent(new OutgoingPriceUpdateEvent(session.getClusterId(),
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

    /**
     * Timestamp at which the cool down period ends (and the Concentrator is allow to send a new BidUpdate again)
     */
    private volatile long coolDownEnds = 0;

    /**
     * Indicates if there is already a BidUpdate scheduled at the end of the cooldown period
     */
    private volatile boolean bidUpdateScheduled = false;

    /**
     * Runnable which generates a BidUpdate and sets the bidUpdateScheduled and coolDownEnds fields
     */
    private final Runnable bidUpdateCommand = new Runnable() {
        @Override
        public void run() {
            try {
                AggregatedBid aggregatedBid = null;
                synchronized (lock) {
                    if (isConnected()) {
                        aggregatedBid = bidCache.aggregate();
                        publishEvent(new AggregatedBidEvent(getClusterId(), getAgentId(), now(), aggregatedBid));
                    }
                }
                if (aggregatedBid != null) {
                    performUpdate(aggregatedBid);
                }
            } catch (RuntimeException e) {
                LOGGER.error("doBidUpate failed for matcher " + getAgentId(), e);
            } finally {
                synchronized (this) {
                    bidUpdateScheduled = false;
                    coolDownEnds = context.currentTimeMillis() + minTimeBetweenUpdates;
                }
            }
        }
    };

    @Override
    public void handleBidUpdate(Session session, BidUpdate bidUpdate) {
        if (session == null || !sessions.containsKey(session.getAgentId())) {
            throw new IllegalStateException("No session found");
        }

        if (bidUpdate == null || !bidUpdate.getBid().getMarketBasis().equals(getMarketBasis())) {
            throw new InvalidParameterException("Marketbasis new bid differs from marketbasis auctioneer");
        }

        // Update agent in aggregatedBids
        bidCache.updateAgentBid(session.getAgentId(), bidUpdate);

        LOGGER.debug("Received from session [{}] bid update [{}] ", session.getSessionId(), bidUpdate);

        publishEvent(new IncomingBidUpdateEvent(session.getClusterId(),
                                                getAgentId(),
                                                session.getSessionId(),
                                                context.currentTime(),
                                                session.getAgentId(),
                                                bidUpdate));

        doUpdate();
    }

    private void doUpdate() {
        synchronized (bidUpdateCommand) {
            if (!bidUpdateScheduled) {
                long waitTime = coolDownEnds - context.currentTimeMillis();
                if (waitTime > 0) {
                    // We're in the cooldown period
                    context.schedule(bidUpdateCommand, Measure.valueOf(waitTime, SI.MILLI(SI.SECOND)));
                } else {
                    // Not in a cooldown period, do it right away!
                    context.submit(bidUpdateCommand);
                }
                bidUpdateScheduled = true;
            }
        }
    }
}
