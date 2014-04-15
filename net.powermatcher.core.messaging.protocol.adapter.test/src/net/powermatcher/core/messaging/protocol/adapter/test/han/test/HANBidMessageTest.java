package net.powermatcher.core.messaging.protocol.adapter.test.han.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.messaging.protocol.adapter.han.AbstractHANMessage;
import net.powermatcher.core.messaging.protocol.adapter.han.HANBidMessage;
import net.powermatcher.core.messaging.protocol.adapter.han.HANMessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.msg.BidMessage;
import net.powermatcher.core.messaging.protocol.adapter.msg.MessageFactory;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class HANBidMessageTest {

	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";

	private MessageFactory factory;
	private MarketBasis marketBasis;
	private MarketBasisCache marketBasisCache;
	private HANBidMessage msg;

	private void sendAndReceive(BidMessage msg) throws InvalidObjectException {
		this.msg = new HANBidMessage(this.marketBasisCache, msg.toBytes());
	}

	private void sendAndReceive() throws InvalidObjectException {
		sendAndReceive(this.msg);
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.factory = HANMessageFactory.getInstance();
		this.marketBasisCache = this.factory.getMarketBasisCache();
		double minimumPrice = AbstractHANMessage.MINIMUM_NORMALIZED_PRICE;
		double maximumPrice = AbstractHANMessage.MAXIMUM_NORMALIZED_PRICE;
		this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, AbstractHANMessage.PRICE_STEPS, minimumPrice, maximumPrice, 0, 128);
		this.factory.getMarketBasisCache().registerExternalMarketBasis(this.marketBasis);
		this.msg = new HANBidMessage(new BidInfo(this.marketBasis));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testEquals() throws InvalidObjectException {
		HANBidMessage msg = this.msg;
		sendAndReceive();
		assertTrue(this.msg.equals(msg));
		double values[] = this.msg.getBidInfo().getDemand();
		values[0] = 23456;
		values[1] = 12345;
		msg = new HANBidMessage(new BidInfo(this.marketBasis, values));
		assertFalse(this.msg.equals(msg));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetBidInfo() throws InvalidObjectException {
		PricePoint pricePoint1 = new PricePoint(-10, 12345);
		PricePoint pricePoint2 = new PricePoint(20, -23456);
		this.msg = new HANBidMessage(new BidInfo(this.marketBasis, pricePoint1, pricePoint2));
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
		this.msg = new HANBidMessage(new BidInfo(this.marketBasis, values));
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
		assertEquals(this.msg.getMsgType(), HANBidMessage.MessageType.BID);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetVersion() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getVersion(), HANBidMessage.Version.VERSION_1);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testHashCode() throws InvalidObjectException {
		HANBidMessage msg = this.msg;
		sendAndReceive();
		assertEquals(this.msg.hashCode(), msg.hashCode());
		double values[] = this.msg.getBidInfo().getDemand();
		values[0] = 23456;
		values[1] = 12345;
		this.msg = new HANBidMessage(new BidInfo(this.marketBasis, values));
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
		this.msg = new HANBidMessage(new BidInfo(this.marketBasis, values));
		System.out.println(this.msg.toString());
	}

}
