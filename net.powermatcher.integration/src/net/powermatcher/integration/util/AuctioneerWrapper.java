package net.powermatcher.integration.util;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.auctioneer.Auctioneer;

public class AuctioneerWrapper extends Auctioneer {

    public Price getLastPublishedPrice() {
        return this.getLastPublishedPrice();
    }

    public Bid getAggregatedBid(MarketBasis marketBasis) {
        return super.getAggregatedBids().getAggregatedBid(marketBasis);
    }

    public void publishPriceInfo() {
        super.publishNewPrice();
    }

}
