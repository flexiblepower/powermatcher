package net.powermatcher.core.auctioneer.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockDeviceAgent;
import net.powermatcher.mock.SimpleSession;
import net.powermatcher.test.helpers.PropertiesBuilder;

public class PointBidGetDemandTest {
    private static final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 100, 0, 1);;
    private static final String AUCTIONEER_ID = "Auctioneer";

    @Test
    public void testPrice() {
        MockContext mockContext = new MockContext(new Date());
        Auctioneer auctioneer = new Auctioneer();
        auctioneer.activate(new PropertiesBuilder().agentId(AUCTIONEER_ID)
                                                   .clusterId("cluster")
                                                   .marketBasis(marketBasis)
                                                   .minTimeBetweenPriceUpdates(1000)
                                                   .build());
        mockContext = new MockContext(0);
        auctioneer.setContext(mockContext);
        MockDeviceAgent uncontrolled = new MockDeviceAgent("uncontorlled", AUCTIONEER_ID);
        new SimpleSession(uncontrolled, auctioneer).connect();
        MockDeviceAgent timeshifter = new MockDeviceAgent("timeshifter", AUCTIONEER_ID);
        new SimpleSession(timeshifter, auctioneer).connect();

        uncontrolled.sendBid(Bid.flatDemand(marketBasis, -200), 0);
        Bid bid = Bid.create(marketBasis).add(0.01, 2000).add(0.01, 0).build();
        timeshifter.sendBid(bid, 0);

        // The auctioneer should pick a price in which the timeshifter does not consume, since this is closest to the
        // target demand of 0 watts
        mockContext.doTaskOnce();
        PriceUpdate price = timeshifter.getLastPriceUpdate();
        // Note that the toString of Price rounds the value of the price!

        assertEquals(0, bid.getDemandAt(price.getPrice()), 0.01);
    }

}
