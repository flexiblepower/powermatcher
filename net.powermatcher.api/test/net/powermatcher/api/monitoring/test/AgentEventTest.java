package net.powermatcher.api.monitoring.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import net.powermatcher.api.data.ArrayBidBuilder;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.IncomingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;

/**
 * JUnit tests for the {@link AgentEvent} class.
 *
 * @author FAN
 * @version 2.1
 */
public class AgentEventTest {
    private String clusterId;
    private String agentId;
    private String sessionId;
    private Date timestamp;
    private String fromAgent;
    private MarketBasis marketBasis;
    private Bid bid;
    private BidUpdate bidUpdate;
    private PriceUpdate priceUpdate;

    @Before
    public void setUp() {
        clusterId = "testCluster";
        agentId = "testAgent";
        sessionId = "testSession";
        timestamp = new Date();
        fromAgent = "message from agent";
        marketBasis = new MarketBasis("water", "EURO", 10, 0, 10);
        bid = new ArrayBidBuilder(marketBasis).demand(0).build();
        bidUpdate = new BidUpdate(bid, 9);
        priceUpdate = new PriceUpdate(new Price(marketBasis, 10.0), 0);
    }

    @Test
    public void testIncomingBidEvent() {
        IncomingBidUpdateEvent ibe = new IncomingBidUpdateEvent(clusterId,
                                                                agentId,
                                                                sessionId,
                                                                timestamp,
                                                                fromAgent,
                                                                bidUpdate);
        assertThat(ibe.getClusterId(), is(equalTo(clusterId)));
        assertThat(ibe.getAgentId(), is(equalTo(agentId)));
        assertThat(ibe.getSessionId(), is(equalTo(sessionId)));
        assertThat(ibe.getTimestamp(), is(equalTo(timestamp)));
        assertThat(ibe.getFromAgentId(), is(equalTo(fromAgent)));
        assertThat(ibe.getBidUpdate(), is(equalTo(bidUpdate)));
    }

    @Test
    public void testIncomingBidEventToString() {
        IncomingBidUpdateEvent ibe = new IncomingBidUpdateEvent(clusterId,
                                                                agentId,
                                                                sessionId,
                                                                timestamp,
                                                                fromAgent,
                                                                bidUpdate);
        String ibetoString = ibe.toString();
        assertThat(ibetoString, is(notNullValue()));
    }

    @Test
    public void testOutgoingBidEvent() {
        OutgoingBidUpdateEvent obe = new OutgoingBidUpdateEvent(clusterId, agentId, sessionId, timestamp, bidUpdate);
        assertThat(obe.getClusterId(), is(equalTo(clusterId)));
        assertThat(obe.getAgentId(), is(equalTo(agentId)));
        assertThat(obe.getSessionId(), is(equalTo(sessionId)));
        assertThat(obe.getTimestamp(), is(equalTo(timestamp)));
        assertThat(obe.getBidUpdate(), is(equalTo(bidUpdate)));
    }

    @Test
    public void testOutgoingBidEventToString() {
        OutgoingBidUpdateEvent obe = new OutgoingBidUpdateEvent(clusterId, agentId, sessionId, timestamp, bidUpdate);
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
