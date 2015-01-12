package net.powermatcher.core.monitoring.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.IncomingBidEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.core.monitoring.BidLogRecord;
import net.powermatcher.core.monitoring.LogRecord;
import net.powermatcher.core.monitoring.PriceUpdateLogRecord;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the {@link LogRecord} class.
 * 
 * @author FAN
 * @version 2.0
 */
public class LogRecordTest {

    private MarketBasis marketBasis;
    private String clusterId;
    private String agentId;
    private String sessionId;
    private String fromAgentId;
    private Date timeStamp;
    private SimpleDateFormat dateFormat;
    private int bidNumber;
    private double[] demand;
    private Qualifier qualifier;
    private IncomingBidEvent incomingBidEvent;
    private OutgoingBidEvent outgoingBidEvent;
    private IncomingPriceUpdateEvent incomingPriceUpdateEvent;
    private OutgoingPriceUpdateEvent outgoingPriceUpdateEvent;
    private ArrayBid bid;
    private Price price;
    private PriceUpdate priceUpdate;

    @Before
    public void setUp() throws Exception {
        marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);
        clusterId = "testCluster";
        agentId = "testagentId";
        sessionId = "testsessionId";
        timeStamp = new Date();
        bidNumber = 1;
        qualifier = Qualifier.AGENT;
        demand = new double[] { 4.0, 3.0, 2.0, 1.0, 0.0 };
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bid = new ArrayBid(marketBasis, bidNumber, demand);
        price = new Price(marketBasis, 10);
        priceUpdate = new PriceUpdate(price, bidNumber);
        incomingBidEvent = new IncomingBidEvent(clusterId, agentId, sessionId, timeStamp, fromAgentId, bid, qualifier);
        outgoingBidEvent = new OutgoingBidEvent(clusterId, agentId, sessionId, timeStamp, bid, qualifier);
        incomingPriceUpdateEvent = new IncomingPriceUpdateEvent(clusterId, agentId, sessionId, timeStamp, priceUpdate,
                qualifier);
        outgoingPriceUpdateEvent = new OutgoingPriceUpdateEvent(clusterId, agentId, sessionId, timeStamp, priceUpdate,
                qualifier);
    }

    @Test
    public void testBidLogRecordIncomingBid() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        BidLogRecord bidLogRecord = new BidLogRecord(incomingBidEvent, logTime, dateFormat);
        assertThat(bidLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(bidLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(bidLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) bidLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(bidLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(bidLogRecord.getQualifier(), is(equalTo(qualifier)));
        assertThat(bidLogRecord.getBid().toArrayBid(), is(equalTo(bid)));
    }

    @Test
    public void testBidLogRecordOutgoingBid() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        BidLogRecord bidLogRecord = new BidLogRecord(outgoingBidEvent, logTime, dateFormat);
        assertThat(bidLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(bidLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(bidLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) bidLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(bidLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(bidLogRecord.getQualifier(), is(equalTo(qualifier)));
        assertThat(bidLogRecord.getBid().toArrayBid(), is(equalTo(bid)));
    }

    @Test
    public void testPriceLogRecordIncomingPrice() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        PriceUpdateLogRecord priceUpdateLogRecord = new PriceUpdateLogRecord(incomingPriceUpdateEvent, logTime,
                dateFormat);
        assertThat(priceUpdateLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(priceUpdateLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(priceUpdateLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) priceUpdateLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(priceUpdateLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(priceUpdateLogRecord.getQualifier(), is(equalTo(qualifier)));
        assertThat(priceUpdateLogRecord.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

    @Test
    public void testPriceLogRecordOutgoingPrice() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        PriceUpdateLogRecord priceUpdateLogRecord = new PriceUpdateLogRecord(outgoingPriceUpdateEvent, logTime,
                dateFormat);
        assertThat(priceUpdateLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(priceUpdateLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(priceUpdateLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) priceUpdateLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(priceUpdateLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(priceUpdateLogRecord.getQualifier(), is(equalTo(qualifier)));
        assertThat(priceUpdateLogRecord.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

}
