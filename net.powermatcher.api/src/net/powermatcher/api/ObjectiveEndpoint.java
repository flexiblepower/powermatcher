package net.powermatcher.api;

import aQute.bnd.annotation.component.Component;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;


/**
 * @author FAN
 * @version 1.0
 */
public interface ObjectiveEndpoint {

    void notifyPriceUpdate(Price newPrice);

    Bid handleAggregateBid(Bid aggregatedBid);
}
