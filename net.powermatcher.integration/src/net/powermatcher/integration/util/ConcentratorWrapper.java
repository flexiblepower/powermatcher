package net.powermatcher.integration.util;

import net.powermatcher.api.data.Price;
import net.powermatcher.core.concentrator.Concentrator;

public class ConcentratorWrapper extends Concentrator {
    
    private Price lastPublishedPrice;
    private Price lastReceivedPrice;
    
    @Override
    public void updatePrice(Price newPrice){
        this.lastPublishedPrice = newPrice;
        super.updatePrice(newPrice);
    }
    
    public synchronized void doBidUpdate(){
        super.doBidUpdate();
    }
    
    public Price getLastPublishedPrice(){
        return this.lastPublishedPrice;
    }
    
    public Price getLastReceivedPrice(){
        return this.lastReceivedPrice;
    }

    public Price getLastPrice(){
        return this.lastPublishedPrice;
    }
}
