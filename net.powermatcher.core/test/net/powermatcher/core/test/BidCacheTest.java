package net.powermatcher.core.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.BidCache;
import net.powermatcher.core.time.SystemTimeService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BidCacheTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private BidCache bidCache;
    private MarketBasis marketBasis;

    @Before
    public void setUp() {
        TimeService timeService = new SystemTimeService();
        this.bidCache = new BidCache(timeService, 600);
        this.marketBasis = new MarketBasis("electricity", "EURO", 5, 0, 10);
    }

    @Test
    public void testUpdateBidNull() {
        // sent null bid
        expectedException.expect(IllegalArgumentException.class);
        bidCache.updateBid("agent1", null);
    }

    @Test
    public void testUpdateBid() {
        ArrayBid bid1 = new ArrayBid.Builder(marketBasis).setDemand(0).build();
        ArrayBid bid2 = new ArrayBid(bid1, 2);
        Bid emptyBid = bidCache.updateBid("agent1", bid1);
        assertThat(emptyBid, is(nullValue()));
    }
}
