package net.powermatcher.core.messaging.protocol.adapter.test.internal.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.messaging.protocol.adapter.internal.InternalBidMessage;
import net.powermatcher.core.messaging.protocol.adapter.internal.InternalMessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.msg.BidMessage;
import net.powermatcher.core.messaging.protocol.adapter.msg.MessageFactory;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class InternalBidMessageTest {

	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";
	/**
	 * Define the minimum normalized price (int) constant.
	 */
	public static final int MINIMUM_NORMALIZED_PRICE = -127;
	/**
	 * Define the maximum normalized price (int) constant.
	 */
	public static final int MAXIMUM_NORMALIZED_PRICE = 127;
	/**
	 * Define the price steps (int) constant.
	 */
	public static final int PRICE_STEPS = MAXIMUM_NORMALIZED_PRICE - MINIMUM_NORMALIZED_PRICE + 1;

	private MessageFactory factory;
	private MarketBasis marketBasis;
	private MarketBasisCache marketBasisCache;
	private InternalBidMessage msg;

	private void sendAndReceive(BidMessage msg) throws InvalidObjectException {
		this.msg = new InternalBidMessage(this.marketBasisCache, msg.toBytes());
	}

	private void sendAndReceive() throws InvalidObjectException {
		sendAndReceive(this.msg);
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.factory = InternalMessageFactory.getInstance();
		this.marketBasisCache = this.factory.getMarketBasisCache();
		double minimumPrice = MINIMUM_NORMALIZED_PRICE;
		double maximumPrice = MAXIMUM_NORMALIZED_PRICE;
		this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, minimumPrice, maximumPrice, 0, 128);
		this.marketBasisCache.registerExternalMarketBasis(this.marketBasis);
		this.msg = new InternalBidMessage(new BidInfo(this.marketBasis));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testEquals() throws InvalidObjectException {
		InternalBidMessage msg = this.msg;
		sendAndReceive();
		assertTrue(this.msg.equals(msg));
		double values[] = this.msg.getBidInfo().getDemand();
		values[0] = 23456;
		values[1] = 12345;
		msg = new InternalBidMessage(new BidInfo(this.marketBasis, values));
		assertFalse(this.msg.equals(msg));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetBidInfo() throws InvalidObjectException {
		PricePoint pricePoint1 = new PricePoint(-10, 12345);
		PricePoint pricePoint2 = new PricePoint(20, -23456);
		this.msg = new InternalBidMessage(new BidInfo(this.marketBasis, pricePoint1, pricePoint2));
		BidInfo bidInfo = this.msg.getBidInfo();
		sendAndReceive(this.factory.toBidMessage(bidInfo));
		assertEquals(pricePoint1.getNormalizedPrice(), this.msg.getBidInfo().getPricePoints()[0].getNormalizedPrice());
		assertEquals(pricePoint1.getDemand(), this.msg.getBidInfo().getPricePoints()[0].getDemand(), 0.0d);
		assertEquals(pricePoint2.getNormalizedPrice(), this.msg.getBidInfo().getPricePoints()[1].getNormalizedPrice());
		assertEquals(pricePoint2.getDemand(), this.msg.getBidInfo().getPricePoints()[1].getDemand(), 0.0d);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetDemandPoints() throws InvalidObjectException {
		double values[] = this.msg.getBidInfo().getDemand();
		values[0] = 23456;
		values[1] = 12345;
		this.msg = new InternalBidMessage(new BidInfo(this.marketBasis, values));
		sendAndReceive();
		assertEquals(values[0], this.msg.getBidInfo().getDemand()[0], 0.0d);
		assertEquals(values[1], this.msg.getBidInfo().getDemand()[1], 0.0d);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMarketRef() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(128, this.msg.getBidInfo().getMarketBasis().getMarketRef());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMsgType() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getMsgType(), InternalBidMessage.MessageType.BID);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetVersion() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getVersion(), InternalBidMessage.Version.VERSION_1);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testHashCode() throws InvalidObjectException {
		InternalBidMessage msg = this.msg;
		sendAndReceive();
		assertEquals(this.msg.hashCode(), msg.hashCode());
		double values[] = this.msg.getBidInfo().getDemand();
		values[0] = 23456;
		values[1] = 12345;
		this.msg = new InternalBidMessage(new BidInfo(this.marketBasis, values));
		assertFalse(this.msg.hashCode() == msg.hashCode());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testToString() throws InvalidObjectException {
		double values[] = this.msg.getBidInfo().getDemand();
		values[0] = 23456;
		values[1] = 12345;
		BidInfo bidInfo = new BidInfo(this.marketBasis, values);
		bidInfo = new BidInfo(bidInfo, 123456789);
		this.msg = new InternalBidMessage(bidInfo);
		sendAndReceive();
		System.out.println(this.msg.toString());
	}

}
