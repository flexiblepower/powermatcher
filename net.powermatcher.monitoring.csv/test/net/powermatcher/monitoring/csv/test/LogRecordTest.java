package net.powermatcher.monitoring.csv.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.api.monitoring.events.IncomingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingPriceUpdateEvent;
import net.powermatcher.monitoring.csv.BidUpdateLogRecord;
import net.powermatcher.monitoring.csv.LogRecord;
import net.powermatcher.monitoring.csv.PriceUpdateLogRecord;

/**
 * JUnit tests for the {@link LogRecord} class.
 *
 * @author FAN
 * @version 2.1
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
    private IncomingBidUpdateEvent incomingBidEvent;
    private OutgoingBidUpdateEvent outgoingBidEvent;
    private IncomingPriceUpdateEvent incomingPriceUpdateEvent;
    private OutgoingPriceUpdateEvent outgoingPriceUpdateEvent;
    private Bid bid;
    private BidUpdate bidUpdate;
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
        demand = new double[] { 4.0, 3.0, 2.0, 1.0, 0.0 };
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        bid = new Bid(marketBasis, demand);
        bidUpdate = new BidUpdate(bid, bidNumber);
        price = new Price(marketBasis, 10);
        priceUpdate = new PriceUpdate(price, bidNumber);
        incomingBidEvent = new IncomingBidUpdateEvent(clusterId, agentId, sessionId, timeStamp, fromAgentId, bidUpdate);
        outgoingBidEvent = new OutgoingBidUpdateEvent(clusterId, agentId, sessionId, timeStamp, bidUpdate);
        incomingPriceUpdateEvent = new IncomingPriceUpdateEvent(clusterId, agentId, sessionId, timeStamp, priceUpdate);
        outgoingPriceUpdateEvent = new OutgoingPriceUpdateEvent(clusterId, agentId, sessionId, timeStamp, priceUpdate);
    }

    @Test
    public void testBidLogRecordIncomingBid() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        BidUpdateLogRecord bidLogRecord = new BidUpdateLogRecord(incomingBidEvent, logTime, dateFormat);
        assertThat(bidLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(bidLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(bidLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) bidLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(bidLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(bidLogRecord.getBidUpdate().getBid(), is(equalTo(bid)));
    }

    @Test
    public void testBidLogRecordOutgoingBid() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        BidUpdateLogRecord bidLogRecord = new BidUpdateLogRecord(outgoingBidEvent, logTime, dateFormat);
        assertThat(bidLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(bidLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(bidLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) bidLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(bidLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(bidLogRecord.getBidUpdate().getBid(), is(equalTo(bid)));
    }

    @Test
    public void testPriceLogRecordIncomingPrice() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        PriceUpdateLogRecord priceUpdateLogRecord = new PriceUpdateLogRecord(incomingPriceUpdateEvent,
                                                                             logTime,
                                                                             dateFormat);
        assertThat(priceUpdateLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(priceUpdateLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(priceUpdateLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) priceUpdateLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(priceUpdateLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(priceUpdateLogRecord.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

    @Test
    public void testPriceLogRecordOutgoingPrice() {
        Calendar c = Calendar.getInstance();
        c.set(2014, 12, 12);
        Date logTime = c.getTime();
        PriceUpdateLogRecord priceUpdateLogRecord = new PriceUpdateLogRecord(outgoingPriceUpdateEvent,
                                                                             logTime,
                                                                             dateFormat);
        assertThat(priceUpdateLogRecord.getClusterId(), is(equalTo(clusterId)));
        assertThat(priceUpdateLogRecord.getAgentId(), is(equalTo(agentId)));
        assertThat(priceUpdateLogRecord.getEventTimestamp(), is(equalTo(timeStamp)));
        assertThat((SimpleDateFormat) priceUpdateLogRecord.getDateFormat(), is(equalTo(dateFormat)));
        assertThat(priceUpdateLogRecord.getLogTime(), is(equalTo(logTime)));
        assertThat(priceUpdateLogRecord.getPriceUpdate(), is(equalTo(priceUpdate)));
    }

}
