package net.powermatcher.core.bidcache.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;

import org.junit.Test;

public class BidCacheTest {
    private static final MarketBasis MB = new MarketBasis("Electricity", "EUR", 100, 0, 1);

    @Test
    public void testFlatBids() {
        BidCache bidCache = new BidCache(MB);

        AggregatedBid aggregatedBid = bidCache.aggregate();
        assertTrue(aggregatedBid.getAgentBidReferences().isEmpty());
        assertEquals(Bid.flatDemand(MB, 0, 0).toArrayBid(), aggregatedBid.getAggregatedBid());

        assertSame(aggregatedBid, bidCache.aggregate());
        assertSame(aggregatedBid, bidCache.retreiveAggregatedBid(0));

        // Add the bids of 2 agents
        bidCache.updateAgentBid("agent1", Bid.flatDemand(MB, 1, 100));
        bidCache.updateAgentBid("agent2", Bid.flatDemand(MB, 2, -50));

        aggregatedBid = bidCache.aggregate();
        assertEquals(2, aggregatedBid.getAgentBidReferences().size());
        assertTrue(aggregatedBid.getAgentBidReferences().containsKey("agent1"));
        assertTrue(aggregatedBid.getAgentBidReferences().containsKey("agent2"));
        assertEquals(Bid.flatDemand(MB, 1, 50).toArrayBid(), aggregatedBid.getAggregatedBid());

        assertSame(aggregatedBid, bidCache.aggregate());
        assertNotNull(bidCache.retreiveAggregatedBid(1));
        assertSame(aggregatedBid, bidCache.retreiveAggregatedBid(1));

        try {
            bidCache.retreiveAggregatedBid(0);
            fail("Expected the old 0-bid to be gone");
        } catch (IllegalArgumentException ex) {
            // Expected
        }

        // Now remove the bid of the first agent
        bidCache.removeBidOfAgent("agent1");

        aggregatedBid = bidCache.aggregate();
        assertEquals(1, aggregatedBid.getAgentBidReferences().size());
        assertFalse(aggregatedBid.getAgentBidReferences().containsKey("agent1"));
        assertTrue(aggregatedBid.getAgentBidReferences().containsKey("agent2"));
        assertEquals(Bid.flatDemand(MB, 2, -50).toArrayBid(), aggregatedBid.getAggregatedBid());

        assertSame(aggregatedBid, bidCache.aggregate());
        assertNotNull(bidCache.retreiveAggregatedBid(1));
        assertSame(aggregatedBid, bidCache.retreiveAggregatedBid(2));

        try {
            bidCache.retreiveAggregatedBid(1);
            fail("Expected the old 1-bid to be gone");
        } catch (IllegalArgumentException ex) {
            // Expected
        }
    }
}
