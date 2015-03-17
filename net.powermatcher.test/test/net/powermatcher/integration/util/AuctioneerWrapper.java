package net.powermatcher.integration.util;

import java.util.HashSet;
import java.util.Set;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.mock.MockContext;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class AuctioneerWrapper
    extends Auctioneer {

    private BidUpdate lastReceivedBid;
    private final Set<Session> shadowedSessions = new HashSet<Session>();

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void connectToAgent(Session session) {
        shadowedSessions.add(session);
        super.connectToAgent(session);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void agentEndpointDisconnected(Session session) {
        shadowedSessions.remove(session);
        super.agentEndpointDisconnected(session);
    }

    public void publishPrice() {
        ((MockContext) context).doTaskOnce();
    }

    public void publishPrice(PriceUpdate priceUpdate) {
        for (Session session : shadowedSessions) {
            session.updatePrice(priceUpdate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBidUpdate(Session session, BidUpdate bidUpdate) {
        lastReceivedBid = bidUpdate;
        super.handleBidUpdate(session, bidUpdate);
    }

    public Bid getAggregatedBid() {
        return aggregate();
    }

    public BidUpdate getLastReceivedBid() {
        return lastReceivedBid;
    }

}
