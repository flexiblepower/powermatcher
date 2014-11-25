package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * A {@link ObjectiveEndpoint} defines the interface for classes like objective agents. 
 * HandleAggregateBid(Bid) will process the logical code typically for a objective agent.
 * 
 * @author FAN
 * @version 1.0
 */
public interface ObjectiveEndpoint {

    void notifyPriceUpdate(Price newPrice);

    Bid handleAggregateBid(Bid aggregatedBid);
}
