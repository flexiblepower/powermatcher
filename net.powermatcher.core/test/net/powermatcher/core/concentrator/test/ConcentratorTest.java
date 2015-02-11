package net.powermatcher.core.concentrator.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.SimpleSession;
import net.powermatcher.test.helpers.PropertieBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for the {@link Concentrator} class.
 *
 * @author FAN
 * @version 2.0
 **/
public class ConcentratorTest {
    private static final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);;
    private static final String CONCENTRATOR_ID = "Concentrator";
    private static final String AUCTIONEER_ID = "Auctioneer";
    private static final String CLUSTER_ID = "testCluster";
    private static final int BID_UPDATE_RATE = 30;

    private final Concentrator concentrator = new Concentrator();
    private final MockContext context = new MockContext(0);

    @Before
    public void setUp() {
        concentrator.activate(new PropertieBuilder().agentId(CONCENTRATOR_ID)
                                                    .desiredParentId(AUCTIONEER_ID)
                                                    .bidUpdateRate(BID_UPDATE_RATE)
                                                    .build());
        concentrator.setContext(context);
    }

    @Test
    public void testActivate() {
        assertThat(concentrator.getAgentId(), is(equalTo(CONCENTRATOR_ID)));
        assertThat(concentrator.getDesiredParentId(), is(equalTo(AUCTIONEER_ID)));
        assertThat(context.getMockScheduler().getUpdateRate(), is(equalTo(BID_UPDATE_RATE)));
    }

    @Test
    public void testDeactivate() {
        context.getMockScheduler().doTaskOnce();
        assertThat(context.getMockScheduler().getMockFuture().isCancelled(), is(false));
        concentrator.deactivate();
        assertThat(context.getMockScheduler().getMockFuture().isCancelled(), is(true));
    }

    @Test
    public void testConnectToAgentBeforeMatcher() {
        Session session = new SimpleSession(new MockAgent("testAgent"), concentrator);
        boolean connectToAgent = concentrator.connectToAgent(session);
        assertThat(connectToAgent, is(false));
    }

    @Test
    public void testMatcherEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
        mockMatcherAgent.setMarketBasis(marketBasis);

        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(CONCENTRATOR_ID);

        SimpleSession topSession = new SimpleSession(concentrator, mockMatcherAgent);
        topSession.connect();
        new SimpleSession(mockAgent, concentrator).connect();

        assertNotNull(mockAgent.getClusterId());

        topSession.disconnect();
        assertNull(mockMatcherAgent.getSession());
        assertNull(concentrator.getClusterId());
        assertNull(mockAgent.getSession());
        assertNull(mockAgent.getClusterId());
        assertNull(mockAgent.getSession());
    }

    @Test
    public void testAgentEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
        mockMatcherAgent.setDesiredParentId("test");
        mockMatcherAgent.setMarketBasis(marketBasis);
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        SimpleSession agentSession = new SimpleSession(mockAgent, concentrator);
        agentSession.connect();

        assertNotNull(mockMatcherAgent.getSession());
        assertNotNull(mockAgent.getClusterId());

        agentSession.disconnect();

        assertNotNull(mockMatcherAgent.getSession());
        assertNotNull(concentrator.getClusterId(), is(CLUSTER_ID));
        assertNull(mockAgent.getSession());
        assertNull(mockAgent.getClusterId());
        assertNull(mockAgent.getSession());

    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateBidNullSession() {
        Concentrator concentrator = new Concentrator();
        concentrator.handleBidUpdate(null, Bid.flatDemand(marketBasis, 0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testupdateBidDifferentMarketBasis() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
        mockMatcherAgent.setDesiredParentId("test");
        mockMatcherAgent.setMarketBasis(marketBasis);
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        concentrator.handleBidUpdate(mockAgent.getSession(),
                                     new ArrayBid.Builder(new MarketBasis("a", "b", 2, 0, 2)).demand(0).build());
    }

    @Test
    public void testUpdateBid() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        mockAgent.sendBid(arrayBid);
        context.getMockScheduler().doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 0);
        assertThat(mockMatcherAgent.getLastReceivedBid(),
                   is(equalTo(expectedBid)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdatePriceNull() {
        concentrator.handlePriceUpdate(null);
    }

    @Test
    public void testUpdatePrice() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(CONCENTRATOR_ID);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        int sentBidNumber = 5;
        Bid bid = new ArrayBid(marketBasis, sentBidNumber, new double[] { 2, 1, 0, -1, -1 });
        mockAgent.sendBid(bid);
        context.getMockScheduler().doTaskOnce();

        int validBidNumber = mockMatcherAgent.getLastReceivedBid().getBidNumber();
        PriceUpdate expected = new PriceUpdate(new Price(marketBasis, 5.0), validBidNumber);
        PriceUpdate error = new PriceUpdate(new Price(marketBasis, 6.0), 655449);
        concentrator.handlePriceUpdate(expected);
        concentrator.handlePriceUpdate(error);
        assertThat(mockAgent.getLastPriceUpdate().getBidNumber(), is(equalTo(sentBidNumber)));
        assertThat(mockAgent.getLastPriceUpdate().getPrice(), is(equalTo(expected.getPrice())));
    }
}
