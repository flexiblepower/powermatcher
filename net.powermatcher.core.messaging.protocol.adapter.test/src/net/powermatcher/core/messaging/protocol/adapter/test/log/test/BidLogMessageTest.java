package net.powermatcher.core.messaging.protocol.adapter.test.log.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InvalidObjectException;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.log.BidLogMessage;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class BidLogMessageTest {

	private static final String CLUSTER_ID = "c1";
	private static final String AGENT_ID = "a1";
	private static final String QUALIFIER = "q1";
	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";
	private static final int PRICE_STEPS = 5;
	private static final double MINIMUM_PRICE = -1.0d;
	private static final double MAXIMUM_PRICE = 7.0d;
	private static final int MARKET_REF = 123;
	private static final double TEST_PRICE = 55.5d;

	MarketBasis marketBasis;
	double[] demand2 = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };
	PricePoint[] points3 = new PricePoint[] { new PricePoint(-1, 50.0d), new PricePoint(-1, 0.0d) };
	PricePoint[] points4 = new PricePoint[] { new PricePoint(0, 50.0d), new PricePoint(0, 0.0d) };
	BidInfo bidInfo1;
	BidInfo bidInfo2;
	BidInfo bidInfo3;
	BidInfo bidInfo4;

	private BidLogMessage msg1;
	private BidLogMessage msg2;
	private BidLogMessage msg3;
	private BidLogMessage msg4;
	private BidLogMessage msg5;

	private BidLogMessage sendAndReceive(BidLogMessage msg) throws InvalidObjectException {
		return new BidLogMessage(msg.toBytes());
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
		this.bidInfo1 = new BidInfo(this.marketBasis);
		this.bidInfo2 = new BidInfo(this.marketBasis, this.demand2);
		this.bidInfo3 = new BidInfo(this.marketBasis, this.points3);
		this.bidInfo4 = new BidInfo(this.marketBasis, this.points4);
		this.msg1 = setUpMessage(this.bidInfo1, TEST_PRICE);
		this.msg2 = setUpMessage(this.bidInfo2, TEST_PRICE);
		this.msg3 = setUpMessage(this.bidInfo3, TEST_PRICE);
		this.msg4 = setUpMessage(this.bidInfo4, TEST_PRICE);
		BidLogInfo bidLogInfo = this.msg2.getBidLogInfo();
		this.msg5 = new BidLogMessage(new BidLogInfo(CLUSTER_ID, AGENT_ID, QUALIFIER, new Date(), this.marketBasis, bidLogInfo.getEffectivePrice(), bidLogInfo.getEffectiveDemand(), bidLogInfo.getMaximumDemand(), bidLogInfo.getMinimumDemand(), null));
	}

	private BidLogMessage setUpMessage(BidInfo bidInfo, double effectivePrice) {
		double effectiveDemand = bidInfo.getDemand(effectivePrice);
		double maximumDemand = bidInfo.getMaximumDemand();
		double minimumDemand = bidInfo.getMinimumDemand();
		BidLogInfo bidLogInfo = new BidLogInfo(CLUSTER_ID, AGENT_ID, QUALIFIER, new Date(), this.marketBasis, effectivePrice, effectiveDemand, maximumDemand, minimumDemand, bidInfo);
		return new BidLogMessage(bidLogInfo);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testEquals() throws InvalidObjectException {
		assertTrue(sendAndReceive(this.msg1).equals(this.msg1));
		assertTrue(sendAndReceive(this.msg2).equals(this.msg2));
		assertTrue(sendAndReceive(this.msg3).equals(this.msg3));
		assertTrue(sendAndReceive(this.msg4).equals(this.msg4));
		assertTrue(sendAndReceive(this.msg5).equals(this.msg5));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCommodity() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(COMMODITY_ELECTRICITY, msg.getBidLogInfo().getMarketBasis().getCommodity());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCurrency() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(CURRENCY_EUR, msg.getBidLogInfo().getMarketBasis().getCurrency());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetEffectivePrice() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(TEST_PRICE, msg.getBidLogInfo().getEffectivePrice(), TEST_PRICE);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMarketRef() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(MARKET_REF, msg.getBidLogInfo().getMarketBasis().getMarketRef());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMsgType() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(msg.getMsgType(), BidLogMessage.MessageType.BID);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetVersion() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(msg.getVersion(), BidLogMessage.Version.VERSION_1);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testHashCode() throws InvalidObjectException {
		BidLogMessage msg = sendAndReceive(this.msg2);
		assertEquals(msg.hashCode(), this.msg2.hashCode());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testToString() throws InvalidObjectException {
		System.out.println(this.msg1.toString());
		System.out.println(this.msg2.toString());
		System.out.println(this.msg3.toString());
		System.out.println(this.msg4.toString());
		System.out.println(this.msg5.toString());
	}

}
