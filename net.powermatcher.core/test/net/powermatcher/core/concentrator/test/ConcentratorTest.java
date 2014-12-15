package net.powermatcher.core.concentrator.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.core.sessions.SessionImpl;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.MockScheduler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for the Concentrator
 * 
 * */
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
    private List<String> whiteList;

    @Before
    public void setUp() {
        marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);
        concentrator = new Concentrator();
        mockScheduler = new MockScheduler();

        props = new HashMap<>();
        concentratorId = "concentrator";
        auctioneerId = "auctioneer";
        clusterId = "testCluster";
        bidUpdateRate = 30;
        whiteList = new ArrayList<>();
        whiteList.add("testAgent");
        
        props.put("agentId", concentratorId);
        props.put("desiredParentId", auctioneerId);
        props.put("bidUpdateRate", bidUpdateRate);
        props.put("whiteListAgents", whiteList);

        concentrator.setExecutorService(mockScheduler);
        concentrator.setTimeService(new SystemTimeService());
        concentrator.activate(props);
    }

    @Test
    public void testActivate() {
        assertThat(concentrator.getAgentId(), is(equalTo(concentratorId)));
        assertThat(concentrator.getDesiredParentId(), is(equalTo(auctioneerId)));
        assertThat(concentrator.getWhiteList(), is(equalTo(whiteList)));        
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
        Session session = new SessionImpl(null, null, "testAgent", null, concentratorId, "mockSession");
        session.setClusterId(clusterId);
        boolean connectToAgent = concentrator.connectToAgent(session);
        assertThat(connectToAgent, is(false));
    }

    @Test
    public void testConnectToAgent() {
        Session session = new SessionImpl(null, null, concentratorId, null, auctioneerId, "mockSession");
        session.setClusterId(clusterId);
        session.setMarketBasis(marketBasis);
        concentrator.connectToMatcher(session);
        Session session2 = new SessionImpl(null, null, "testAgent", null, concentratorId, "mockSession");
        boolean connectToAgent = concentrator.connectToAgent(session2);
        assertThat(connectToAgent, is(true));
        assertThat(session2.getMarketBasis(), is(equalTo(marketBasis)));
        assertThat(session2.getClusterId(), is(equalTo(clusterId)));
    }
    
    @Test
    public void testMatcherEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setDesiredParentId("what");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.addMatcherEndpoint(concentrator);

        sessionManager.addAgentEndpoint(mockAgent);
        assertThat(mockAgent.getClusterId(), is(notNullValue()));

        sessionManager.removeMatcherEndpoint(mockMatcherAgent);
        assertThat(mockMatcherAgent.getSession(), is(nullValue()));
        assertThat(concentrator.getClusterId(), is(nullValue()));
        assertThat(mockAgent.getSession(), is(nullValue()));
        assertThat(mockAgent.getClusterId(), is(nullValue()));
        assertThat(mockAgent.getSession(), is(nullValue()));
    }

    @Test
    public void testAgentEndpointDisconnected() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setDesiredParentId("test");
        mockMatcherAgent.setMarketBasis(marketBasis);
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.addMatcherEndpoint(concentrator);

        sessionManager.addAgentEndpoint(mockAgent);

        assertThat(mockMatcherAgent.getSession(), is(notNullValue()));
        assertThat(mockAgent.getClusterId(), is(notNullValue()));

        sessionManager.removeAgentEndpoint(mockAgent);
        assertThat(mockMatcherAgent.getSession(), is(notNullValue()));
        assertThat(concentrator.getClusterId(), is(auctioneerId));
        assertThat(mockAgent.getSession(), is(nullValue()));
        assertThat(mockAgent.getClusterId(), is(nullValue()));
        assertThat(mockAgent.getSession(), is(nullValue()));

    }

    @Test
    public void testUpdateBidNullSession() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("No session found");
        Concentrator concentrator = new Concentrator();
        concentrator.updateBid(null, new ArrayBid.Builder(marketBasis).setDemand(0).build());
    }

    @Test
    public void testupdateBidDifferentMarketBasis() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setDesiredParentId("test");
        mockMatcherAgent.setMarketBasis(marketBasis);
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.addMatcherEndpoint(concentrator);

        sessionManager.addAgentEndpoint(mockAgent);
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Marketbasis new bid differs from marketbasis auctioneer");
        concentrator.updateBid(mockAgent.getSession(),new ArrayBid.Builder(new MarketBasis("a", "b", 2, 0, 2)).setDemand(0).build());
    }

    @Test
    public void testUpdateBid() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.addMatcherEndpoint(concentrator);

        sessionManager.addAgentEndpoint(mockAgent);
        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        concentrator.updateBid(mockAgent.getSession(), arrayBid);
        mockScheduler.doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 1);
        assertThat(mockMatcherAgent.getLastReceivedBid(), is(equalTo(expectedBid)));
    }

    @Test
    public void testUpdatePriceNull() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Price cannot be null");
        concentrator.updatePrice(null);
    }

    @Test
    public void testUpdatePrice() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.addMatcherEndpoint(concentrator);

        sessionManager.addAgentEndpoint(mockAgent);

        Bid bid = new ArrayBid(marketBasis, 1, new double[] { 2, 1, 0, -1, -1 });
        mockAgent.sendBid(bid);
        mockScheduler.doTaskOnce();
        PriceUpdate expected = new PriceUpdate(new Price(marketBasis, 5.0), 1);
        PriceUpdate error = new PriceUpdate(new Price(marketBasis, 6.0), 1);
        concentrator.updatePrice(expected);
        concentrator.updatePrice(error);
        assertThat(mockAgent.getLastPriceUpdate(), is(equalTo(expected)));
    }
}
