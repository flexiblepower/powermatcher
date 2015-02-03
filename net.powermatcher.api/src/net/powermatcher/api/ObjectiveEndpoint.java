package net.powermatcher.api;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

/**
 * {@link ObjectiveEndpoint} defines the interface for Objective Agents. Objective Agents are objects that can
 * manipulate the {@link Price} in a PowerMatcher cluster by sending a modified {@link Bid}.
 *
 * @author FAN
 * @version 2.0
 */
public interface ObjectiveEndpoint {

    /**
     * This method is called by the Auctioneer when it will send the price
     *
     * @param price
     *            the {@link Price} sent by the Auctioneer.
     */
    void notifyPrice(Price price);

    /**
     * This method will calculate a new Aggregated {@link Bid} to manipulate the cluster.
     *
     * @param aggregatedBid
     *            the Auctioneer's aggregated {@link Bid} that will be used to calculate the new {@link Bid}.
     * @return the updated {@link Bid}
     */
    Bid handleAggregateBid(Bid aggregatedBid);
}
