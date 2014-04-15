package net.powermatcher.core.messaging.protocol.adapter.test.log.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InvalidObjectException;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.log.PriceLogMessage;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class PriceLogMessageTest {

	private static final String CLUSTER_ID = "c1";
	private static final String AGENT_ID = "a1";
	private static final String QUALIFIER = "q1";
	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";
	private static final int PRICE_STEPS = 100;
	private static final double MINIMUM_PRICE = 0.0d;
	private static final double MAXIMUM_PRICE = 99.0d;
	private static final int MARKET_REF = 123;
	private static final double TEST_PRICE = 55.5d;

	private PriceLogMessage msg;

	private void sendAndReceive() throws InvalidObjectException {
		this.msg = new PriceLogMessage(this.msg.toBytes());
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
		PriceInfo priceInfo = new PriceInfo(marketBasis, TEST_PRICE);
		PriceLogInfo priceLogInfo = new PriceLogInfo(CLUSTER_ID, AGENT_ID, QUALIFIER, new Date(), priceInfo);
		this.msg = new PriceLogMessage(priceLogInfo);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testEquals() throws InvalidObjectException {
		PriceLogMessage orgMsg = this.msg;
		sendAndReceive();
		assertTrue(this.msg.equals(orgMsg));
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCommodity() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(COMMODITY_ELECTRICITY, this.msg.getPriceLogInfo().getMarketBasis().getCommodity());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCurrency() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(CURRENCY_EUR, this.msg.getPriceLogInfo().getMarketBasis().getCurrency());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetCurrentPrice() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(TEST_PRICE, this.msg.getPriceLogInfo().getCurrentPrice(), 0.0d);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMarketRef() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(MARKET_REF, this.msg.getPriceLogInfo().getMarketBasis().getMarketRef());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetMsgType() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getMsgType(), PriceLogMessage.MessageType.PRICE);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testGetVersion() throws InvalidObjectException {
		sendAndReceive();
		assertEquals(this.msg.getVersion(), PriceLogMessage.Version.VERSION_1);
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testHashCode() throws InvalidObjectException {
		PriceLogMessage orgMsg = this.msg;
		sendAndReceive();
		assertEquals(this.msg.hashCode(), orgMsg.hashCode());
	}

	/**
	 * @throws InvalidObjectException
	 */
	@Test
	public void testToString() throws InvalidObjectException {
		System.out.println(this.msg.toString());
	}

}
