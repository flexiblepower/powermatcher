package net.powermatcher.core.auctioneer.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.SimpleSession;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit tests for the {@link Auctioneer} class.
 *
 * @author FAN
 * @version 2.0
 */
public class AuctioneerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String AUCTIONEER_NAME = "auctioneer";
    private Auctioneer auctioneer;
    private Map<String, Object> auctioneerProperties;
    private MockContext mockContext;

    private MarketBasis marketBasis;

    private String commodity;
    private String currency;
    private String clusterId;
    private int priceSteps;
    private double minimumPrice;
    private double maximumPrice;
    private int bidTimeout;
    private int priceUpdateRate;

    @Before
    public void setUp() {
        commodity = "electricity";
        currency = "EUR";
        clusterId = "testCluster";
        priceSteps = 5;
        minimumPrice = 0;
        maximumPrice = 10;
        bidTimeout = 600;
        priceUpdateRate = 1;

        marketBasis = new MarketBasis(commodity, currency, priceSteps,
                                      minimumPrice, maximumPrice);

        auctioneer = new Auctioneer();
        auctioneerProperties = new HashMap<String, Object>();
        auctioneerProperties.put("agentId", AUCTIONEER_NAME);
        auctioneerProperties.put("clusterId", clusterId);
        auctioneerProperties.put("commodity", commodity);
        auctioneerProperties.put("currency", currency);
        auctioneerProperties.put("priceSteps", priceSteps);
        auctioneerProperties.put("minimumPrice", minimumPrice);
        auctioneerProperties.put("maximumPrice", maximumPrice);
        auctioneerProperties.put("bidTimeout", bidTimeout);
        auctioneerProperties.put("priceUpdateRate", priceUpdateRate);

        mockContext = new MockContext(0);
        auctioneer.setContext(mockContext);
        auctioneer.activate(auctioneerProperties);

    }

    private class AuctioneerObserver
        implements AgentObserver {

        private IncomingBidEvent incomingBidEvent;
        private OutgoingPriceUpdateEvent outgoingPriceEvent;

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(AgentEvent event) {
            if (event instanceof IncomingBidEvent) {
                if (incomingBidEvent != null) {
                    fail("IncomingBidEvent fired more than once");
                }
                incomingBidEvent = (IncomingBidEvent) event;
            } else if (event instanceof OutgoingPriceUpdateEvent) {
                if (outgoingPriceEvent != null) {
                    fail("OutgoingPriceEvent fired more than once");
                }
                outgoingPriceEvent = (OutgoingPriceUpdateEvent) event;
            } else {
                fail("unexpected event");
            }
        }
    }

    @Test
    public void testActivate() {
        assertThat(mockContext.getMockScheduler().getMockFuture().isCancelled(), is(false));
    }

    @Test
    public void testDeactivate() {
        auctioneer.deactivate();
        assertThat(mockContext.getMockScheduler().getMockFuture().isCancelled(), is(true));
    }

    @Test
    public void testConnectToAgent() {
        MockAgent agent = new MockAgent("agent1");
        agent.setDesiredParentId(AUCTIONEER_NAME);
        new SimpleSession(agent, auctioneer).connect();
        Session session = agent.getSession();
        assertThat(session.getClusterId(),
                   is(equalTo(auctioneer.getClusterId())));
        assertThat(session.getMarketBasis().getCommodity(),
                   is(equalTo(commodity)));
        assertThat(session.getMarketBasis().getCurrency(),
                   is(equalTo(currency)));
        assertThat(session.getMarketBasis().getPriceSteps(),
                   is(equalTo(priceSteps)));
        assertThat(session.getMarketBasis().getMinimumPrice(),
                   is(equalTo(minimumPrice)));
        assertThat(session.getMarketBasis().getMaximumPrice(),
                   is(equalTo(maximumPrice)));
    }

    @Test
    public void testAgentEndpointDisconnected() {
        MockAgent agent = new MockAgent("agent1");
        agent.setDesiredParentId(AUCTIONEER_NAME);
        SimpleSession session = new SimpleSession(agent, auctioneer);
        session.connect();
        assertThat(agent.getSession(), is(notNullValue()));
        assertThat(agent.getClusterId(), is(notNullValue()));

        session.disconnect();

        assertThat(agent.getSession(), is(nullValue()));
        assertThat(agent.getClusterId(), is(nullValue()));
        assertThat(agent.getSession(), is(nullValue()));
    }

    @Test
    public void testUpdateBidNullSession() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("No session found");
        auctioneer.handleBidUpdate(null, new ArrayBid(marketBasis, 0, new double[] {
                                                                                    5.0, 4.0, 3.0, 1.0, 0.0 }));
    }

    @Test
    public void testupdateBidDifferentMarketBasis() {
        MockAgent mockAgent = new MockAgent("mockAgent");
        mockAgent.setDesiredParentId(AUCTIONEER_NAME);
        new SimpleSession(mockAgent, auctioneer).connect();

        exception.expect(IllegalArgumentException.class);
        exception
                 .expectMessage("Marketbasis new bid differs from marketbasis auctioneer");
        auctioneer.handleBidUpdate(mockAgent.getSession(), new ArrayBid(
                                                                        new MarketBasis("a", "b", 2, 0, 2), 0,
                                                                        new double[] { 5.0, 4.0 }));
    }

    @Test
    public void testUpdateBid() {
        String agentName = "mockAgent";
        MockAgent mockAgent = new MockAgent(agentName);
        mockAgent.setDesiredParentId(AUCTIONEER_NAME);

        AuctioneerObserver observer = new AuctioneerObserver();
        auctioneer.addObserver(observer);
        new SimpleSession(mockAgent, auctioneer).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        Bid bid = new ArrayBid(marketBasis, 0, demandArray);
        mockAgent.sendBid(bid);

        assertThat(observer.incomingBidEvent.getClusterId(),
                   is(equalTo(clusterId)));
        assertThat(observer.incomingBidEvent.getAgentId(),
                   is(equalTo(AUCTIONEER_NAME)));
        assertThat(observer.incomingBidEvent.getFromAgentId(),
                   is(equalTo(agentName)));
        assertThat(observer.incomingBidEvent.getBid(), is(equalTo(bid)));
    }

    @Test
    public void testPublishPriceUpdate() {
        String agentName = "mockAgent";
        MockAgent mockAgent = new MockAgent(agentName);
        mockAgent.setDesiredParentId(AUCTIONEER_NAME);

        AuctioneerObserver observer = new AuctioneerObserver();
        auctioneer.addObserver(observer);
        new SimpleSession(mockAgent, auctioneer).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        Bid bid = new ArrayBid(marketBasis, 0, demandArray);
        mockAgent.sendBid(bid);
        assertThat(mockAgent.getLastPriceUpdate(), is(nullValue()));
        mockContext.getMockScheduler().doTaskOnce();
        assertThat(mockAgent.getLastPriceUpdate(), is(notNullValue()));
        assertThat(observer.outgoingPriceEvent.getPriceUpdate(),
                   is(equalTo(mockAgent.getLastPriceUpdate())));
    }
}
