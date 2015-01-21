package net.powermatcher.core.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.AgentObserver;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.core.sessions.SessionImpl;
import net.powermatcher.mock.MockMatcherAgent;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the {@link BaseDeviceAgent} class.
 * 
 * @author FAN
 * @version 2.0
 */
public class BaseDeviceAgentTest {

    private TestBaseDeviceAgent baseDeviceAgent;
    private MarketBasis marketBasis;

    private class AgentWatcher implements AgentObserver {

        private OutgoingBidEvent outgoingBidEvent;
        private IncomingPriceUpdateEvent incomingPriceEvent;

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(AgentEvent event) {
            if (event instanceof OutgoingBidEvent) {
                if (outgoingBidEvent != null) {
                    fail("OutgoingBidEvent fired more than once");
                }
                outgoingBidEvent = (OutgoingBidEvent) event;
            } else if (event instanceof IncomingPriceUpdateEvent) {
                if (incomingPriceEvent != null) {
                    fail("IncomingPriceUpdateEvent fired more than once");
                }
                incomingPriceEvent = (IncomingPriceUpdateEvent) event;
            } else {
                fail("unexpected event");
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        marketBasis = new MarketBasis("electricity", "EURO", 5, 0, 10);
        baseDeviceAgent = new TestBaseDeviceAgent();
    }

    @Test
    public void testConnectToMatcher() {
        Session session = new SessionImpl(null, null, "testAgent", null, "testMatcherId", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        assertThat(baseDeviceAgent.getSession(), is(equalTo(session)));
    }

    @Test
    public void testMatcherEndpointDisconnected() {
        Session session = new SessionImpl(null, null, "testAgent", null, "testMatcherId", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.matcherEndpointDisconnected(session);
        assertThat(baseDeviceAgent.getSession(), is(nullValue()));
    }

    @Test
    public void testGetMarketBasis() {
        Session session = new SessionImpl(null, null, "testAgent", null, "testMatcherId", "testSession");
        session.setMarketBasis(marketBasis);
        baseDeviceAgent.connectToMatcher(session);
        assertThat(baseDeviceAgent.testGetMarketBasis(), is(marketBasis));
    }

    @Test
    public void testCreateBidSessionNull() {
        PricePoint[] pricePoints = new PricePoint[] { new PricePoint(new Price(marketBasis, 3), 10.0),
                new PricePoint(new Price(marketBasis, 7), 5.0) };
        PointBid testCreateBid = baseDeviceAgent.testCreateBid(pricePoints);
        assertThat(testCreateBid, is(nullValue()));
    }

    @Test
    public void testCreateBid() {
        PricePoint[] pricePoints = new PricePoint[] { new PricePoint(new Price(marketBasis, 3), 10.0),
                new PricePoint(new Price(marketBasis, 7), 5.0) };
        Session session = new SessionImpl(null, null, "testAgent", null, "testMatcherId", "testSession");
        session.setMarketBasis(marketBasis);
        baseDeviceAgent.connectToMatcher(session);
        PointBid testCreateBid = baseDeviceAgent.testCreateBid(pricePoints);
        assertThat(testCreateBid.getMarketBasis(), is(equalTo(marketBasis)));
        assertThat(testCreateBid.getPricePoints(), is(equalTo(pricePoints)));
    }

    @Test
    public void testUpdatePrice() {
        Price price = new Price(marketBasis, 10);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        AgentWatcher observer = new AgentWatcher();
        baseDeviceAgent.addObserver(observer);
        Session session = new SessionImpl(null, null, "testAgent", null, "testMatcherId", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.updatePrice(priceUpdate);
        assertThat(observer.incomingPriceEvent.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

    @Test
    public void testPublishBid() {
        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 5.0, 3.0, 1.0, -1.0, -3.0 });

        AgentWatcher observer = new AgentWatcher();
        baseDeviceAgent.addObserver(observer);

        MockMatcherAgent matcher = new MockMatcherAgent("mock");
        Session session = new SessionImpl(null, null, "testAgent", matcher, "mock", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.publishBid(bid);
        assertThat((ArrayBid) observer.outgoingBidEvent.getBid(), is(equalTo(bid)));

    }

    @Test
    public void testGetLastBid() {
        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 5.0, 3.0, 1.0, -1.0, -3.0 });

        AgentWatcher observer = new AgentWatcher();
        baseDeviceAgent.addObserver(observer);

        MockMatcherAgent matcher = new MockMatcherAgent("mock");
        Session session = new SessionImpl(null, null, "testAgent", matcher, "mock", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.publishBid(bid);
        assertThat((ArrayBid) observer.outgoingBidEvent.getBid(), is(equalTo(bid)));
        assertThat((ArrayBid) baseDeviceAgent.getLastBid(), is(equalTo(bid)));
    }

    @Test
    public void testRemoveObserver() {
        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 5.0, 3.0, 1.0, -1.0, -3.0 });

        AgentWatcher observer = new AgentWatcher();
        baseDeviceAgent.addObserver(observer);

        MockMatcherAgent matcher = new MockMatcherAgent("mock");
        Session session = new SessionImpl(null, null, "testAgent", matcher, "mock", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.removeObserver(observer);
        baseDeviceAgent.publishBid(bid);
        assertThat(observer.outgoingBidEvent, is(nullValue()));
        assertThat((ArrayBid) baseDeviceAgent.getLastBid(), is(equalTo(bid)));
    }

    @Test
    public void testGetCurrentBidNr() {
        int testGetCurrenBidNumber = baseDeviceAgent.testGetCurrenBidNumber();
        assertThat(testGetCurrenBidNumber, is(equalTo(0)));

        PricePoint[] pricePoints = new PricePoint[] { new PricePoint(new Price(marketBasis, 3), 10.0),
                new PricePoint(new Price(marketBasis, 7), 5.0) };
        Session session = new SessionImpl(null, null, "testAgent", null, "testMatcherId", "testSession");
        session.setMarketBasis(marketBasis);
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.testCreateBid(pricePoints);

        testGetCurrenBidNumber = baseDeviceAgent.testGetCurrenBidNumber();
        assertThat(testGetCurrenBidNumber, is(equalTo(1)));
    }

    @Test
    public void testCompareTo() {
        String agentId = "both";
        TestBaseDeviceAgent another = new TestBaseDeviceAgent();
        baseDeviceAgent.testSetAgentId(agentId);
        another.testSetAgentId(agentId);
        assertThat(another.compareTo(baseDeviceAgent), is(equalTo(0)));
    }

    @Test
    public void testSetAgentId() {
        String agentId = "teest";
        baseDeviceAgent.testSetAgentId(agentId);
        assertThat(baseDeviceAgent.getAgentId(), is(equalTo(agentId)));
    }

    @Test
    public void testCanEqual() {
        TestBaseDeviceAgent another = new TestBaseDeviceAgent();
        assertThat(baseDeviceAgent.canEqual(another), is(true));
    }

    @Test
    public void testEquals() {
        assertThat(baseDeviceAgent.equals(null), is(false));
        assertThat(baseDeviceAgent.equals(baseDeviceAgent), is(true));
        TestBaseDeviceAgent another = new TestBaseDeviceAgent();

        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 5.0, 3.0, 1.0, -1.0, -3.0 });
        MockMatcherAgent matcher = new MockMatcherAgent("mock");
        Session session = new SessionImpl(null, baseDeviceAgent, "testAgent", matcher, "mock", "testSession");
        Session sessionOther = new SessionImpl(null, another, "testAgent", matcher, "mock", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.publishBid(bid);
        another.connectToMatcher(sessionOther);
        another.publishBid(bid);
        assertThat(baseDeviceAgent.equals(another), is(true));
        assertThat(another.equals(baseDeviceAgent), is(true));
    }

    @Test
    public void testHashCode() {
        TestBaseDeviceAgent another = new TestBaseDeviceAgent();
        ArrayBid bid = new ArrayBid(marketBasis, 0, new double[] { 5.0, 3.0, 1.0, -1.0, -3.0 });
        MockMatcherAgent matcher = new MockMatcherAgent("mock");
        Session session = new SessionImpl(null, baseDeviceAgent, "testAgent", matcher, "mock", "testSession");
        Session sessionOther = new SessionImpl(null, another, "testAgent", matcher, "mock", "testSession");
        baseDeviceAgent.connectToMatcher(session);
        baseDeviceAgent.publishBid(bid);
        another.connectToMatcher(sessionOther);
        another.publishBid(bid);

        assertThat(baseDeviceAgent.hashCode(), is(equalTo(another.hashCode())));
    }
}
