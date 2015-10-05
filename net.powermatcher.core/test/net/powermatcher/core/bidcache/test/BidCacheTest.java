package net.powermatcher.core.bidcache.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;

public class BidCacheTest {
    private static final MarketBasis MB = new MarketBasis("Electricity", "EUR", 100, 0, 1);

    @Test
    public void testFlatBids() {
        BidCache bidCache = new BidCache(MB);

        AggregatedBid aggregatedBid = bidCache.aggregate();
        assertTrue(aggregatedBid.getAgentBidReferences().isEmpty());
        assertArrayEquals(Bid.flatDemand(MB, 0).getDemand(), aggregatedBid.getDemand(), 0);

        assertSame(aggregatedBid, bidCache.aggregate());

        // Add the bids of 2 agents
        bidCache.updateAgentBid("agent1", new BidUpdate(Bid.flatDemand(MB, 100), 1));
        bidCache.updateAgentBid("agent2", new BidUpdate(Bid.flatDemand(MB, -50), 2));

        aggregatedBid = bidCache.aggregate();
        assertEquals(2, aggregatedBid.getAgentBidReferences().size());
        assertTrue(aggregatedBid.getAgentBidReferences().containsKey("agent1"));
        assertTrue(aggregatedBid.getAgentBidReferences().containsKey("agent2"));
        assertArrayEquals(Bid.flatDemand(MB, 50).getDemand(), aggregatedBid.getDemand(), 0);

        assertSame(aggregatedBid, bidCache.aggregate());

        // Now remove the bid of the first agent
        bidCache.removeBidOfAgent("agent1");

        aggregatedBid = bidCache.aggregate();
        assertEquals(1, aggregatedBid.getAgentBidReferences().size());
        assertFalse(aggregatedBid.getAgentBidReferences().containsKey("agent1"));
        assertTrue(aggregatedBid.getAgentBidReferences().containsKey("agent2"));
        assertArrayEquals(Bid.flatDemand(MB, -50).getDemand(), aggregatedBid.getDemand(), 0);

        assertSame(aggregatedBid, bidCache.aggregate());
    }
}
