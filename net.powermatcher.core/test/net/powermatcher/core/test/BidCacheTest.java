package net.powermatcher.core.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.BidCacheSnapshot;
import net.powermatcher.core.time.SystemTimeService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit tests for the {@link BidCache } class.
 * 
 * @author FAN
 * @version 2.0
 */
public class BidCacheTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BidCache bidCache;
    private MarketBasis marketBasis;

    @Before
    public void setUp() {
        TimeService timeService = new SystemTimeService();
        this.bidCache = new BidCache(timeService, 0);
        this.marketBasis = new MarketBasis("electricity", "EURO", 5, 0, 10);
    }

    @Test
    public void testUpdateBidNull() {
        expectedException.expect(IllegalArgumentException.class);
        bidCache.updateBid("agent1", null);
    }

    @Test
    public void testUpdateBid() {
        ArrayBid bid1 = new ArrayBid.Builder(marketBasis).setDemand(0).build();
        ArrayBid bid2 = new ArrayBid(bid1, 2);
        Bid emptyBid = bidCache.updateBid("agent1", bid1);
        assertThat(emptyBid, is(nullValue()));
        bidCache.getAggregatedBid(marketBasis, true);
        ArrayBid updateBid = bidCache.updateBid("agent1", bid2);
        assertThat(updateBid, is(equalTo(bid1)));
    }

    @Test
    public void testGetAggregatedBid() {
        ArrayBid aggregatedBid = bidCache.getAggregatedBid(null, false);
        assertThat(aggregatedBid, is(nullValue()));
    }

    @Test
    public void testRemoveAgent() {
        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 6, 5, 4, 3, 2 });
        bidCache.updateBid("agent1", bid);
        ArrayBid removeAgent = bidCache.removeAgent("agent1");
        assertThat(removeAgent, is(nullValue()));

        bidCache.updateBid("agent1", bid);
        ArrayBid aggregatedBid = bidCache.getAggregatedBid(marketBasis, true);
        assertThat(aggregatedBid.getDemand(), is(equalTo(bid.getDemand())));
        removeAgent = bidCache.removeAgent("agent1");
        assertThat(removeAgent, is(equalTo(bid)));
    }

    @Test
    public void testGetMatchingSnapshot() {
        String agentId = "agent1";
        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 6, 5, 4, 3, 2 });
        bidCache.updateBid(agentId, bid);

        bidCache.getAggregatedBid(marketBasis, true);
        BidCacheSnapshot matchingSnapshot = bidCache.getMatchingSnapshot(1);
        Map<String, Integer> bidNumbers = matchingSnapshot.getBidNumbers();
        assertThat(bidNumbers.containsKey("agent1"), is(true));
        assertThat(bidNumbers.size(), is(equalTo(1)));
        assertThat(bidNumbers.get(agentId), is(equalTo(0)));
    }

    @Test
    public void testGetLast() {
        String agentId = "agent1";
        ArrayBid lastBid = bidCache.getLastBid(agentId);
        assertThat(lastBid, is(nullValue()));

        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 6, 5, 4, 3, 2 });
        bidCache.updateBid(agentId, bid);
        bidCache.getAggregatedBid(marketBasis, true);

        lastBid = bidCache.getLastBid(agentId);
        assertThat(lastBid, is(equalTo(bid)));
    }

    @Test
    public void testCleanup() {
        Set<String> expected = new HashSet<String>();
        expected.add("agent1");
        expected.add("agent2");
        double[] expectedDemand = new double[] { 10, 8, 7, 6, 3 };

        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 6, 5, 4, 3, 2 });
        bidCache.updateBid("agent1", bid);

        ArrayBid bid2 = new ArrayBid(marketBasis, 0, new double[] { 4, 3, 3, 3, 1 });
        bidCache.updateBid("agent1", bid);
        bidCache.updateBid("agent2", bid2);

        ArrayBid aggregatedBid = bidCache.getAggregatedBid(marketBasis, true);
        assertThat(aggregatedBid.getBidNumber(), is(equalTo(1)));
        assertThat(aggregatedBid.getDemand(), is(equalTo(expectedDemand)));

        Set<String> cleanup = bidCache.cleanup();
        assertThat(cleanup, is(equalTo(expected)));
    }
}
