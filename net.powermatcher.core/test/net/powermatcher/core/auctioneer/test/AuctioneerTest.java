package net.powermatcher.core.auctioneer.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.SimpleSession;
import net.powermatcher.test.helpers.PropertieBuilder;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the {@link Auctioneer} class.
 *
 * @author FAN
 * @version 2.0
 */
public class AuctioneerTest {
    private static final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);;
    private static final String AUCTIONEER_ID = "Auctioneer";
    private static final String CLUSTER_ID = "testCluster";
    private static final int BID_UPDATE_RATE = 30;
    private static final int PRICE_UPDATE_RATE = 5;

    private Auctioneer auctioneer;
    private MockContext mockContext;

    @Before
    public void setUp() {
        auctioneer = new Auctioneer();
        auctioneer.activate(new PropertieBuilder().agentId(AUCTIONEER_ID)
                                                  .clusterId(CLUSTER_ID)
                                                  .marketBasis(marketBasis)
                                                  .bidUpdateRate(BID_UPDATE_RATE)
                                                  .priceUpdateRate(PRICE_UPDATE_RATE)
                                                  .build());

        mockContext = new MockContext(0);
        auctioneer.setContext(mockContext);
    }

    private class AuctioneerObserver
        implements AgentObserver {

        private IncomingBidEvent incomingBidEvent;
        private OutgoingPriceUpdateEvent outgoingPriceEvent;

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleAgentEvent(AgentEvent event) {
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
        assertThat(mockContext.getMockFuture().isCancelled(), is(false));
    }

    @Test
    public void testDeactivate() {
        auctioneer.deactivate();
        assertThat(mockContext.getMockFuture().isCancelled(), is(true));
    }

    @Test
    public void testConnectToAgent() {
        MockAgent agent = new MockAgent("agent1");
        agent.setDesiredParentId(AUCTIONEER_ID);
        new SimpleSession(agent, auctioneer).connect();
        Session session = agent.getSession();
        assertThat(session.getClusterId(), is(equalTo(auctioneer.getClusterId())));
        assertThat(session.getMarketBasis(), is(equalTo(marketBasis)));
    }

    @Test
    public void testAgentEndpointDisconnected() {
        MockAgent agent = new MockAgent("agent1");
        agent.setDesiredParentId(AUCTIONEER_ID);
        SimpleSession session = new SimpleSession(agent, auctioneer);
        session.connect();
        assertThat(agent.getSession(), is(notNullValue()));
        assertThat(agent.getClusterId(), is(notNullValue()));

        session.disconnect();

        assertThat(agent.getSession(), is(nullValue()));
        assertThat(agent.getClusterId(), is(nullValue()));
        assertThat(agent.getSession(), is(nullValue()));
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateBidNullSession() {
        auctioneer.handleBidUpdate(null, new BidUpdate(new ArrayBid(marketBasis,
                                                                    new double[] { 5.0, 4.0, 3.0, 1.0, 0.0 }), 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testupdateBidDifferentMarketBasis() {
        MockAgent mockAgent = new MockAgent("mockAgent");
        mockAgent.setDesiredParentId(AUCTIONEER_ID);
        new SimpleSession(mockAgent, auctioneer).connect();

        auctioneer.handleBidUpdate(mockAgent.getSession(),
                                   new BidUpdate(Bid.flatDemand(new MarketBasis("a", "b", 2, 0, 2), 0), 0));
    }

    @Test
    public void testUpdateBid() {
        String agentName = "mockAgent";
        MockAgent mockAgent = new MockAgent(agentName);
        mockAgent.setDesiredParentId(AUCTIONEER_ID);

        AuctioneerObserver observer = new AuctioneerObserver();
        auctioneer.addObserver(observer);
        new SimpleSession(mockAgent, auctioneer).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        Bid bid = new ArrayBid(marketBasis, demandArray);
        mockAgent.sendBid(bid, 0);

        assertThat(observer.incomingBidEvent.getClusterId(), is(equalTo(CLUSTER_ID)));
        assertThat(observer.incomingBidEvent.getAgentId(), is(equalTo(AUCTIONEER_ID)));
        assertThat(observer.incomingBidEvent.getFromAgentId(), is(equalTo(agentName)));
        assertThat(observer.incomingBidEvent.getBidUpdate().getBid(), is(equalTo(bid)));
    }

    @Test
    public void testPublishPriceUpdate() {
        String agentName = "mockAgent";
        MockAgent mockAgent = new MockAgent(agentName);
        mockAgent.setDesiredParentId(AUCTIONEER_ID);

        AuctioneerObserver observer = new AuctioneerObserver();
        auctioneer.addObserver(observer);
        new SimpleSession(mockAgent, auctioneer).connect();

        double[] demandArray = new double[] { 2, 1, 0, -1, -2 };
        Bid bid = new ArrayBid(marketBasis, demandArray);
        mockAgent.sendBid(bid, 0);
        assertThat(mockAgent.getLastPriceUpdate(), is(nullValue()));
        mockContext.doTaskOnce();
        assertThat(mockAgent.getLastPriceUpdate(), is(notNullValue()));
        assertThat(observer.outgoingPriceEvent.getPriceUpdate(), is(equalTo(mockAgent.getLastPriceUpdate())));
    }
}
