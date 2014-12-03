package net.powermatcher.api.data.test;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsEqual.*;

import java.security.InvalidParameterException;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author IBM
 * @version 0.9.0
 */
public class PriceTest {

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";
    private static final double DEMAND = 2.0d;

    MarketBasis marketBasis;
    Price price;

    /**
     * @throws InvalidParameterException
     */
    @Before
    public void setUp() throws InvalidParameterException {
        this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d);
        this.price = new Price(this.marketBasis, DEMAND);
    }

    /**
	 * 
	 */
    @Test
    public void testGetCurrentPrice() {
        assertEquals(DEMAND, this.price.getCurrentPrice(), 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testGetMarketBasis() {
        assertEquals(this.marketBasis, this.price.getMarketBasis());
        MarketBasis marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d);
        assertEquals(marketBasis2, this.price.getMarketBasis());
        marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 20, -1.0d, 8.0d);
        assertFalse(marketBasis2.equals(this.price.getMarketBasis()));
    }

    /**
	 * 
	 */
    @Test
    public void testGetNormalizedPrice() {
        assertEquals(2, this.price.getNormalizedPrice());
    }

    /**
	 * 
	 */
    @Test
    public void testPrice() {
        Price price = new Price(this.marketBasis, 1.0d);
        assertEquals(0.0d, price.getCurrentPrice(), 1.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testPriceMarketBasisDouble() {
        assertNotNull(this.price);
    }

    /**
	 * 
	 */
    @Test
    public void testSetCurrentPrice() {
        this.price = new Price(this.marketBasis, 3.0d);
        assertEquals(3.0d, this.price.getCurrentPrice(), 0.0d);
        assertEquals(3, this.price.getNormalizedPrice());
        this.price = new Price(this.marketBasis, -1.0d);
        assertEquals(-1.0d, this.price.getCurrentPrice(), 0.0d);
        assertEquals(-1, this.price.getNormalizedPrice());
        this.price = new Price(this.marketBasis, 8.0d);
        assertEquals(8.0d, this.price.getCurrentPrice(), 0.0d);
        assertEquals(8, this.price.getNormalizedPrice());
    }

    /**
	 * 
	 */
    @Test
    public void testSetCurrentPriceStep() {
        this.price = new Price(this.marketBasis, 3.0d);
        assertEquals(3.0d, this.price.getCurrentPrice(), 0.0d);
        assertEquals(3, this.price.getNormalizedPrice());
    }
    
    @Test
    public void testToMarketBasis(){
        MarketBasis testBasis = new MarketBasis("electricity", "EURO", 5, 0.0, 10.0);
        Price testPrice =  price.toMarketBasis(testBasis);
        assertThat(testPrice.getCurrentPrice(), is(equalTo(price.getCurrentPrice())));
        assertThat(testPrice.getMarketBasis(), is(equalTo(testBasis)));
    }
    
    @Test
    public void testEquals(){
        // check equals null
        Assert.assertThat(price.equals(null), is(false));

        // check reflection
        Assert.assertThat(price.equals(price), is(true));

        // check symmetry
        Price differentPrice = new Price(marketBasis, DEMAND+2);
        Assert.assertThat(price.equals(differentPrice), is(false));
        Assert.assertThat(differentPrice.equals(price), is(false));

        Price samePrice = new Price(this.marketBasis, DEMAND);
        Assert.assertThat(price.equals(samePrice), is(true));
        Assert.assertThat(samePrice.equals(price), is(true));

        // check transition
        MarketBasis equalsBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d);
        double equalsDemand = 2.0;
        Price otherPrice = new Price(equalsBasis, equalsDemand);
        Assert.assertThat(price.equals(otherPrice), is(true));
        Assert.assertThat(samePrice.equals(otherPrice), is(true));

        // check consistency
        Assert.assertThat(price.equals(null), is(false));
        Assert.assertThat(otherPrice.equals(otherPrice), is(true));
        Assert.assertThat(otherPrice.equals(samePrice), is(true));
    }
    
    @Test
    public void testHashCode(){
        Price copy = new Price(marketBasis, DEMAND);
        assertThat(copy.equals(price), is(true));
        assertThat(price.equals(copy), is(true));
        assertThat(copy.hashCode(), is(equalTo(price.hashCode())));
    }
    
    @Test
    public void testToString(){
         final String expected = "Price{currentPrice=2,bidReference=0}";
         assertThat(price.toString(), is(equalTo(expected)));
    }

}
