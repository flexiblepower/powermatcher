package net.powermatcher.integration.util;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.concentrator.Concentrator;

public class ConcentratorWrapper extends Concentrator {

    private Price lastPublishedPrice;
    private Price lastReceivedPrice;
    private Bid lastReceivedBid;

    @Override
    public void updatePrice(Price newPrice) {
        this.lastPublishedPrice = newPrice;
        super.updatePrice(newPrice);
    }

    @Override
    public void updateBid(Session session, Bid newBid) {
        try {
            super.updateBid(session, newBid);
            this.lastReceivedBid = newBid;
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        }
    }

    public synchronized void doBidUpdate() {
        super.doBidUpdate();
    }

    public Price getLastPublishedPrice() {
        return this.lastPublishedPrice;
    }

    public Price getLastReceivedPrice() {
        return this.lastReceivedPrice;
    }

    public Price getLastPrice() {
        return this.lastPublishedPrice;
    }
    
    public Bid getLastReceivedBid(){
        return this.lastReceivedBid;
    }
}
