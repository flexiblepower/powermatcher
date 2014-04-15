package net.powermatcher.core.messaging.protocol.adapter.test.han.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.messaging.protocol.adapter.han.AbstractHANMessage;
import net.powermatcher.core.messaging.protocol.adapter.han.HANMessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.han.HANPriceInfoMessage;
import net.powermatcher.core.messaging.protocol.adapter.msg.MessageFactory;
import net.powermatcher.core.messaging.protocol.adapter.msg.PriceInfoMessage;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class HANPriceInfoMessageTest {

	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";

	private MessageFactory factory;
	private MarketBasis marketBasis;
	private MarketBasisCache marketBasisCache;
	private HANPriceInfoMessage msg;

	private void sendAndReceive(PriceInfoMessage msg) throws InvalidObjectException {
		this.msg = new HANPriceInfoMessage(this.marketBasisCache, msg.toBytes());
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
		this.msg = new HANPriceInfoMessage(new PriceInfo(this.marketBasis, 0.0d));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testEquals() throws InvalidObjectException {
		HANPriceInfoMessage msg = this.msg;
		sendAndReceive();
		assertTrue(this.msg.equals(msg));
		msg = new HANPriceInfoMessage(new PriceInfo(this.marketBasis, 1.0d));
		assertFalse(this.msg.equals(msg));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCommodity() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(COMMODITY_ELECTRICITY, this.msg.getPriceInfo().getMarketBasis().getCommodity());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCurrency() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(CURRENCY_EUR, this.msg.getPriceInfo().getMarketBasis().getCurrency());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCurrentPrice() throws InvalidObjectException {
		double value = -123.0;
		this.msg = new HANPriceInfoMessage(new PriceInfo(this.marketBasis, value));
		sendAndReceive();
		assertEquals(value, this.msg.getPriceInfo().getCurrentPrice(), 0.0d);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMarketRef() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(128, this.msg.getPriceInfo().getMarketBasis().getMarketRef());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMsgType() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getMsgType(), HANPriceInfoMessage.MessageType.PRICE);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetVersion() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getVersion(), HANPriceInfoMessage.Version.VERSION_1);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testHashCode() throws InvalidObjectException {
		HANPriceInfoMessage msg = this.msg;
		sendAndReceive();
		assertEquals(this.msg.hashCode(), msg.hashCode());
		msg = new HANPriceInfoMessage(new PriceInfo(this.marketBasis, 1.0d));
		assertFalse(this.msg.hashCode() == msg.hashCode());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testToString() throws InvalidObjectException {
		this.msg = new HANPriceInfoMessage(new PriceInfo(this.marketBasis, 1.0d));
		System.out.println(this.msg.toString());
	}
}
