package net.powermatcher.core.concentrator.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.ArrayBidBuilder;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockDeviceAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.SimpleSession;
import net.powermatcher.test.helpers.PropertiesBuilder;

/**
 * JUnit test for the {@link Concentrator} class.
 *
 * @author FAN
 * @version 2.0
 **/
public class ConcentratorTest {
    private static final int MIN_TIME_BETWEEN_BIDS = 1000;
    private static final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);;
    private static final String CONCENTRATOR_ID = "Concentrator";
    private static final String AUCTIONEER_ID = "Auctioneer";
    private static final String CLUSTER_ID = "testCluster";

    private final Concentrator concentrator = new Concentrator();
    private final MockContext context = new MockContext(0);

    @Before
    public void setUp() {
        concentrator.activate(new PropertiesBuilder().agentId(CONCENTRATOR_ID)
                                                     .desiredParentId(AUCTIONEER_ID)
                                                     .minTimeBetweenBidUpdates(MIN_TIME_BETWEEN_BIDS)
                                                     .build());
        concentrator.setContext(context);
    }

    @Test
    public void testActivate() {
        assertThat(concentrator.getAgentId(), is(equalTo(CONCENTRATOR_ID)));
        assertThat(concentrator.getDesiredParentId(), is(equalTo(AUCTIONEER_ID)));
    }

    @Test(expected = IllegalStateException.class)
    public void testConnectToAgentBeforeMatcher() {
        Session session = new SimpleSession(new MockDeviceAgent("testAgent", CONCENTRATOR_ID), concentrator);
        concentrator.connectToAgent(session);
    }

    @Test
    public void testMatcherEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID, marketBasis);
        MockDeviceAgent mockAgent = new MockDeviceAgent("testAgent", CONCENTRATOR_ID);

        SimpleSession topSession = new SimpleSession(concentrator, mockMatcherAgent);
        topSession.connect();
        new SimpleSession(mockAgent, concentrator).connect();

        assertNotNull(mockAgent.getStatus().getClusterId());

        topSession.disconnect();
        assertNull(mockMatcherAgent.getSession());
        assertFalse(concentrator.getStatus().isConnected());
        assertNull(mockAgent.getSession());
    }

    @Test
    public void testAgentEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID, marketBasis);
        MockDeviceAgent mockAgent = new MockDeviceAgent("testAgent", CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        SimpleSession agentSession = new SimpleSession(mockAgent, concentrator);
        agentSession.connect();

        assertNotNull(mockMatcherAgent.getSession());
        assertNotNull(mockAgent.getStatus().getClusterId());

        agentSession.disconnect();

        assertNotNull(mockMatcherAgent.getSession());
        assertNotNull(concentrator.getStatus().getClusterId(), is(CLUSTER_ID));
        assertNull(mockAgent.getSession());
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateBidNullSession() {
        Concentrator concentrator = new Concentrator();
        concentrator.handleBidUpdate(null, new BidUpdate(Bid.flatDemand(marketBasis, 0), 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testupdateBidDifferentMarketBasis() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID, marketBasis);
        MockDeviceAgent mockAgent = new MockDeviceAgent("testAgent", CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        concentrator
                    .handleBidUpdate(mockAgent.getSession(),
                                     new BidUpdate(new ArrayBidBuilder(new MarketBasis("a", "b", 2, 0, 2)).demand(0)
                                                                                                          .build(),
                                                   0));
    }

    @Test
    public void testUpdateBid() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID, marketBasis);
        MockDeviceAgent mockAgent = new MockDeviceAgent("testAgent", CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        Bid arrayBid = new Bid(marketBasis, demandArray);
        mockAgent.sendBid(new BidUpdate(arrayBid, 1));
        context.doTaskOnce();
        Bid expectedBid = new Bid(arrayBid.getMarketBasis(), arrayBid.getDemand());
        assertThat(mockMatcherAgent.getLastReceivedBid().getBid().getDemand(),
                   is(equalTo(expectedBid.getDemand())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePriceNull() {
        concentrator.handlePriceUpdate(null);
    }

    @Test
    public void testUpdatePrice() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID, marketBasis);
        MockDeviceAgent mockAgent = new MockDeviceAgent("testAgent", CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        int sentBidNumber = 5;
        Bid bid = new Bid(marketBasis, new double[] { 2, 1, 0, -1, -1 });
        mockAgent.sendBid(new BidUpdate(bid, sentBidNumber));
        context.doTaskOnce();

        int validBidNumber = mockMatcherAgent.getLastReceivedBid().getBidNumber();
        PriceUpdate expected = new PriceUpdate(new Price(marketBasis, 5.0), validBidNumber);
        PriceUpdate error = new PriceUpdate(new Price(marketBasis, 6.0), 655449);
        concentrator.handlePriceUpdate(expected);
        concentrator.handlePriceUpdate(error);
        assertThat(mockAgent.getLastPriceUpdate().getBidNumber(), is(equalTo(sentBidNumber)));
        assertThat(mockAgent.getLastPriceUpdate().getPrice(), is(equalTo(expected.getPrice())));
    }

    @Test
    public void testTiming() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID, marketBasis);
        MockDeviceAgent mockAgent = new MockDeviceAgent("testAgent", CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        // cleanup
        context.jump(1001);
        mockMatcherAgent.resetLastReceivedBid();
        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        Bid arrayBid = new Bid(marketBasis, demandArray);

        // Currently no cool down, should sent BidUpdate right away
        mockAgent.sendBid(new BidUpdate(arrayBid, 1));
        context.doTaskIfTimeIsRight();
        assertNotNull(mockMatcherAgent.getLastReceivedBid());
        mockMatcherAgent.resetLastReceivedBid();

        // Move in the future, but not past the cool down, should not send bidUpdate
        context.jump(500);
        mockAgent.sendBid(new BidUpdate(arrayBid, 2));
        context.doTaskIfTimeIsRight();
        assertNull(mockMatcherAgent.getLastReceivedBid());
        assertEquals(context.currentTimeMillis() + 500, context.getScheduleTime());

        // Move to the end of the cool down, now it should send a price update
        context.jump(500);
        context.doTaskIfTimeIsRight();
        assertNotNull(mockMatcherAgent.getLastReceivedBid());
    }
}
