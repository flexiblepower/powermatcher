package net.powermatcher.core;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;

public abstract class BaseMatcherEndpoint
    extends BaseAgent
    implements MatcherEndpoint {

    private BidCache bidCache;

    @Override
    public void configure(MarketBasis marketBasis, String clusterId) {
        super.configure(marketBasis, clusterId);
        bidCache = new BidCache(marketBasis);
    }

    @Override
    public void unconfigure() {
        for (Iterator<Session> it = sessions.values().iterator(); it.hasNext();) {
            Session session = it.next();
            session.disconnect();
            it.remove();
        }
        super.unconfigure();
        bidCache = null;
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    @Override
    public boolean connectToAgent(Session session) {
        if (!isInitialized()) {
            LOGGER.info("Could not connect an agent yet, not yet initialized");
            return false;
        } else if (!sessions.containsKey(session.getAgentId())) {
            session.setMarketBasis(getMarketBasis());
            sessions.put(session.getAgentId(), session);
            LOGGER.info("Agent connected with session [{}]", session.getSessionId());
            return true;
        } else {
            LOGGER.warn("An agent with id [{}] was already connected", session.getAgentId());
            return false;
        }
    }

    @Override
    public void agentEndpointDisconnected(Session session) {
        Session foundSession = sessions.get(session.getAgentId());
        if (session.equals(foundSession)) {
            sessions.remove(session.getAgentId());
            bidCache.removeBidOfAgent(session.getAgentId());
            LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
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

                session.updatePrice(priceUpdate);
            }
        }
    }

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

        publishEvent(new IncomingBidEvent(session.getClusterId(),
                                          getAgentId(),
                                          session.getSessionId(),
                                          context.currentTime(),
                                          session.getAgentId(),
                                          bidUpdate));
    }

    public final AggregatedBid aggregate() {
        return bidCache.aggregate();
    }
}
