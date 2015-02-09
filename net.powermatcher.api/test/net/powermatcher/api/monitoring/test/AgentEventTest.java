package net.powermatcher.api.monitoring.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the {@link AgentEvent} class.
 *
 * @author FAN
 * @version 2.0
 */
public class AgentEventTest {
    private String clusterId;
    private String agentId;
    private String sessionId;
    private Date timestamp;
    private String fromAgent;
    private MarketBasis marketBasis;
    private Bid bid;
    private PriceUpdate priceUpdate;

    @Before
    public void setUp() {
        clusterId = "testCluster";
        agentId = "testAgent";
        sessionId = "testSession";
        timestamp = new Date();
        fromAgent = "message from agent";
        marketBasis = new MarketBasis("water", "EURO", 10, 0, 10);
        bid = new ArrayBid.Builder(marketBasis).demand(0).build();
        priceUpdate = new PriceUpdate(new Price(marketBasis, 10.0), 0);
    }

    @Test
    public void testIncomingBidEvent() {
        IncomingBidEvent ibe = new IncomingBidEvent(clusterId, agentId, sessionId, timestamp, fromAgent, bid);
        assertThat(ibe.getClusterId(), is(equalTo(clusterId)));
        assertThat(ibe.getAgentId(), is(equalTo(agentId)));
        assertThat(ibe.getSessionId(), is(equalTo(sessionId)));
        assertThat(ibe.getTimestamp(), is(equalTo(timestamp)));
        assertThat(ibe.getFromAgentId(), is(equalTo(fromAgent)));
        assertThat(ibe.getBid(), is(equalTo(bid)));
    }

    @Test
    public void testIncomingBidEventToString() {
        IncomingBidEvent ibe = new IncomingBidEvent(clusterId, agentId, sessionId, timestamp, fromAgent, bid);
        String ibetoString = ibe.toString();
        assertThat(ibetoString, is(notNullValue()));
    }

    @Test
    public void testOutgoingBidEvent() {
        OutgoingBidEvent obe = new OutgoingBidEvent(clusterId, agentId, sessionId, timestamp, bid);
        assertThat(obe.getClusterId(), is(equalTo(clusterId)));
        assertThat(obe.getAgentId(), is(equalTo(agentId)));
        assertThat(obe.getSessionId(), is(equalTo(sessionId)));
        assertThat(obe.getTimestamp(), is(equalTo(timestamp)));
        assertThat(obe.getBid(), is(equalTo(bid)));
    }

    @Test
    public void testOutgoingBidEventToString() {
        OutgoingBidEvent obe = new OutgoingBidEvent(clusterId, agentId, sessionId, timestamp, bid);
        String obetoString = obe.toString();
        assertThat(obetoString, is(notNullValue()));
    }

    public void testIncomingPriceUpdateEvent() {
        IncomingPriceUpdateEvent ice = new IncomingPriceUpdateEvent(clusterId,
                                                                    agentId,
                                                                    sessionId,
                                                                    timestamp,
                                                                    priceUpdate);
        assertThat(ice.getClusterId(), is(equalTo(clusterId)));
        assertThat(ice.getAgentId(), is(equalTo(agentId)));
        assertThat(ice.getSessionId(), is(equalTo(sessionId)));
        assertThat(ice.getTimestamp(), is(equalTo(timestamp)));
        assertThat(ice.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

    @Test
    public void testIncomingPriceUpdateEventToString() {
        IncomingPriceUpdateEvent ice = new IncomingPriceUpdateEvent(clusterId,
                                                                    agentId,
                                                                    sessionId,
                                                                    timestamp,
                                                                    priceUpdate);
        String icetoString = ice.toString();
        assertThat(icetoString, is(notNullValue()));
    }

    public void testOutgoingPriceUpdateEvent() {
        OutgoingPriceUpdateEvent oce = new OutgoingPriceUpdateEvent(clusterId,
                                                                    agentId,
                                                                    sessionId,
                                                                    timestamp,
                                                                    priceUpdate);
        assertThat(oce.getClusterId(), is(equalTo(clusterId)));
        assertThat(oce.getAgentId(), is(equalTo(agentId)));
        assertThat(oce.getSessionId(), is(equalTo(sessionId)));
        assertThat(oce.getTimestamp(), is(equalTo(timestamp)));
        assertThat(oce.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

    @Test
    public void testOutgoingPriceUpdateEventToString() {
        OutgoingPriceUpdateEvent oce = new OutgoingPriceUpdateEvent(clusterId,
                                                                    agentId,
                                                                    sessionId,
                                                                    timestamp,
                                                                    priceUpdate);
        String icetoString = oce.toString();
        assertThat(icetoString, is(notNullValue()));
    }
}
