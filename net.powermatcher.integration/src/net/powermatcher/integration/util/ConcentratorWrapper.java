package net.powermatcher.integration.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.concentrator.Concentrator;

public class ConcentratorWrapper extends Concentrator {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcentratorWrapper.class);

    private Price lastPublishedPrice;
    private Price lastReceivedPrice;
    private Bid lastReceivedBid;
    private Bid lastPublishedBid;

    @Override
    public void updatePrice(Price newPrice) {
        this.lastReceivedPrice = newPrice;
        super.updatePrice(newPrice);
        
        //This should reflect the check in Concentrator.updatePrice
        if(newPrice != null){
            this.lastPublishedPrice = newPrice;
        }
    }

    @Override
    public void updateBid(Session session, Bid newBid){
        try {
            //Exceptions can be thrown in updateBid, if so, lastPublishedBid is not set.
            this.lastReceivedBid = newBid;
            super.updateBid(session, newBid);
            this.lastPublishedBid = newBid;
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.error("Illegal argument or state in updateBid.", e);
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
    
    public Bid getLastPublishedBid(){
        return this.lastPublishedBid;
    }
}
