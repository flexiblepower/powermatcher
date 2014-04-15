package net.powermatcher.core.data.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class PriceInfoTest {

	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";

	MarketBasis marketBasis;
	PriceInfo priceInfo;

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d, 2, 0);
		this.priceInfo = new PriceInfo(this.marketBasis, 2.0d);
	}

	/**
	 * 
	 */
	@Test
	public void testGetCurrentPrice() {
		assertEquals(2.0d, this.priceInfo.getCurrentPrice(), 0.0d);
	}

	/**
	 * 
	 */
	@Test
	public void testGetMarketBasis() {
		assertEquals(this.marketBasis, this.priceInfo.getMarketBasis());
		MarketBasis marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d, 2, 0);
		assertEquals(marketBasis2, this.priceInfo.getMarketBasis());
		marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 20, -1.0d, 8.0d, 2, 0);
		assertFalse(marketBasis2.equals(this.priceInfo.getMarketBasis()));
	}

	/**
	 * 
	 */
	@Test
	public void testGetNormalizedPrice() {
		assertEquals(2, this.priceInfo.getNormalizedPrice());
	}

	/**
	 * 
	 */
	@Test
	public void testPriceInfo() {
		PriceInfo priceInfo = new PriceInfo(this.marketBasis, 1.0d);
		assertEquals(0.0d, priceInfo.getCurrentPrice(), 1.0d);
	}

	/**
	 * 
	 */
	@Test
	public void testPriceInfoMarketBasisDouble() {
		assertNotNull(this.priceInfo);
	}

	/**
	 * 
	 */
	@Test
	public void testSetCurrentPrice() {
		this.priceInfo = new PriceInfo(this.marketBasis, 3.0d);
		assertEquals(3.0d, this.priceInfo.getCurrentPrice(), 0.0d);
		assertEquals(3, this.priceInfo.getNormalizedPrice());
		this.priceInfo = new PriceInfo(this.marketBasis, -1.0d);
		assertEquals(-1.0d, this.priceInfo.getCurrentPrice(), 0.0d);
		assertEquals(-1, this.priceInfo.getNormalizedPrice());
		this.priceInfo = new PriceInfo(this.marketBasis, 8.0d);
		assertEquals(8.0d, this.priceInfo.getCurrentPrice(), 0.0d);
		assertEquals(8, this.priceInfo.getNormalizedPrice());
	}

	/**
	 * 
	 */
	@Test
	public void testSetCurrentPriceStep() {
		this.priceInfo = new PriceInfo(this.marketBasis, 3.0d);
		assertEquals(3.0d, this.priceInfo.getCurrentPrice(), 0.0d);
		assertEquals(3, this.priceInfo.getNormalizedPrice());
	}

}
