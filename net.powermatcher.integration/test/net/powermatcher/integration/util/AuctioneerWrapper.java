package net.powermatcher.integration.util;

import java.util.HashSet;
import java.util.Set;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.auctioneer.Auctioneer;

public class AuctioneerWrapper extends Auctioneer {

    private Bid lastReceivedBid;
    private Set<Session> shadowedSessions = new HashSet<>();

    @Override
    public synchronized boolean connectToAgent(Session session) {
        this.shadowedSessions.add(session);
        return super.connectToAgent(session);
    }

    @Override
    public synchronized void agentEndpointDisconnected(Session session) {
        shadowedSessions.remove(session);
        super.agentEndpointDisconnected(session);
    }

    public void publishPrice() {
        super.publishNewPrice();
    }

    public void publishPrice(PriceUpdate priceUpdate) {
        for (Session session : shadowedSessions) {
            session.updatePrice(priceUpdate);
        }
    }

    @Override
    public synchronized void updateBid(Session session, Bid newBid) {
        this.lastReceivedBid = newBid;
        super.updateBid(session, newBid);
    }

    public Bid getAggregatedBid(MarketBasis marketBasis) {
        return super.getAggregatedBids().getAggregatedBid(marketBasis, false);
    }

    public Bid getLastReceivedBid() {
        return this.lastReceivedBid;
    }

}
