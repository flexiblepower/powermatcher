package net.powermatcher.core.concentrator.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.MockScheduler;
import net.powermatcher.mock.MockTimeService;
import net.powermatcher.mock.SimpleSession;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for the {@link Concentrator} class.
 *
 * @author FAN
 * @version 2.0
 **/
public class ConcentratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MarketBasis marketBasis;
    private Concentrator concentrator;
    private MockScheduler mockScheduler;

    private Map<String, Object> props;
    private String concentratorId;
    private String auctioneerId;
    private String clusterId;
    private long bidUpdateRate;
    private TimeService systemTimeService;

    @Before
    public void setUp() {
        marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);
        concentrator = new Concentrator();
        mockScheduler = new MockScheduler();

        props = new HashMap<String, Object>();
        concentratorId = "concentrator";
        auctioneerId = "auctioneer";
        clusterId = "testCluster";
        bidUpdateRate = 30;

        props.put("agentId", concentratorId);
        props.put("desiredParentId", auctioneerId);
        props.put("bidUpdateRate", bidUpdateRate);

        systemTimeService = new MockTimeService(0);
        concentrator.activate(props);
        concentrator.setTimeService(systemTimeService);
        concentrator.setExecutorService(mockScheduler);
    }

    @Test
    public void testActivate() {
        assertThat(concentrator.getAgentId(), is(equalTo(concentratorId)));
        assertThat(concentrator.getDesiredParentId(), is(equalTo(auctioneerId)));
        assertThat(mockScheduler.getUpdateRate(), is(equalTo(bidUpdateRate)));
    }

    @Test
    public void testDeactivate() {
        mockScheduler.doTaskOnce();
        assertThat(mockScheduler.getMockFuture().isCancelled(), is(false));
        concentrator.deactivate();
        assertThat(mockScheduler.getMockFuture().isCancelled(), is(true));
    }

    @Test
    public void testConnectToAgentBeforeMatcher() {
        Session session = new SimpleSession(new MockAgent("testAgent"), concentrator);
        boolean connectToAgent = concentrator.connectToAgent(session);
        assertThat(connectToAgent, is(false));
    }

    @Test
    public void testMatcherEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId, "testCluster");
        mockMatcherAgent.setDesiredParentId("what");
        mockMatcherAgent.setMarketBasis(marketBasis);

        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

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
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId, "testCluster");
        mockMatcherAgent.setDesiredParentId("test");
        mockMatcherAgent.setMarketBasis(marketBasis);
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        SimpleSession agentSession = new SimpleSession(mockAgent, concentrator);
        agentSession.connect();

        assertNotNull(mockMatcherAgent.getSession());
        assertNotNull(mockAgent.getClusterId());

        agentSession.disconnect();

        assertNotNull(mockMatcherAgent.getSession());
        assertNotNull(concentrator.getClusterId(), is("testCluster"));
        assertNull(mockAgent.getSession());
        assertNull(mockAgent.getClusterId());
        assertNull(mockAgent.getSession());

    }

    @Test
    public void testUpdateBidNullSession() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("No session found");
        Concentrator concentrator = new Concentrator();
        concentrator.handleBidUpdate(null, new ArrayBid.Builder(marketBasis)
                                                                            .setDemand(0).build());
    }

    @Test
    public void testupdateBidDifferentMarketBasis() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId,
                                                                 clusterId);
        mockMatcherAgent.setDesiredParentId("test");
        mockMatcherAgent.setMarketBasis(marketBasis);
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        exception.expect(IllegalArgumentException.class);
        exception
                 .expectMessage("Marketbasis new bid differs from marketbasis auctioneer");
        concentrator.handleBidUpdate(mockAgent.getSession(),
                                     new ArrayBid.Builder(
                                                          new MarketBasis("a", "b", 2, 0, 2)).setDemand(0).build());
    }

    @Test
    public void testUpdateBid() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId,
                                                                 clusterId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        mockAgent.sendBid(arrayBid);
        mockScheduler.doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 0);
        assertThat(mockMatcherAgent.getLastReceivedBid(),
                   is(equalTo(expectedBid)));
    }

    @Test
    public void testUpdatePriceNull() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Price cannot be null");
        concentrator.handlePriceUpdate(null);
    }

    @Test
    public void testUpdatePrice() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId,
                                                                 clusterId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        new SimpleSession(concentrator, mockMatcherAgent).connect();
        new SimpleSession(mockAgent, concentrator).connect();

        int sentBidNumber = 5;
        Bid bid = new ArrayBid(marketBasis, sentBidNumber, new double[] { 2, 1, 0, -1, -1 });
        mockAgent.sendBid(bid);
        mockScheduler.doTaskOnce();

        int validBidNumber = mockMatcherAgent.getLastReceivedBid().getBidNumber();
        PriceUpdate expected = new PriceUpdate(new Price(marketBasis, 5.0), validBidNumber);
        PriceUpdate error = new PriceUpdate(new Price(marketBasis, 6.0), 655449);
        concentrator.handlePriceUpdate(expected);
        concentrator.handlePriceUpdate(error);
        assertThat(mockAgent.getLastPriceUpdate().getBidNumber(), is(equalTo(sentBidNumber)));
        assertThat(mockAgent.getLastPriceUpdate().getPrice(), is(equalTo(expected.getPrice())));
    }
}
