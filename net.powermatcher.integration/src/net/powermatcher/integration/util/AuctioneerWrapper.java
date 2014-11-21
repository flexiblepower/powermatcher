package net.powermatcher.integration.util;

import java.util.HashSet;
import java.util.Set;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.auctioneer.Auctioneer;

public class AuctioneerWrapper extends Auctioneer {

    private Price lastPublishedPrice;
    private Bid lastReceivedBid;
    private MarketBasis marketBasis;
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
        Bid aggregatedBid = getAggregatedBids().getAggregatedBid(this.marketBasis);
        Price estimatedNewPrice = determinePrice(aggregatedBid);
        super.publishNewPrice();
        lastPublishedPrice = estimatedNewPrice;
    }

    public void publishPrice(Price newPrice) {
        this.lastPublishedPrice = newPrice;
        for (Session session : shadowedSessions) {
            session.updatePrice(newPrice);
        }
    }
    
    @Override
    public synchronized void updateBid(Session session, Bid newBid) {
        this.lastReceivedBid = newBid;
        super.updateBid(session, newBid);
    }

    public void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }
    
    public Price getLastPublishedPrice() {
        return lastPublishedPrice;
    }

    public Bid getAggregatedBid(MarketBasis marketBasis) {
        return super.getAggregatedBids().getAggregatedBid(marketBasis);
    }
    
    public Bid getLastReceivedBid(){
        return this.lastReceivedBid;
    }
   
}
