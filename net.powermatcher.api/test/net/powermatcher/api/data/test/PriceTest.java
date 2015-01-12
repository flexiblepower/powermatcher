package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.security.InvalidParameterException;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit tests for the {@link Price} class.
 * 
 * @author FAN
 * @version 2.0
 */
public class PriceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";
    private static final double DEMAND = 2.0d;

    MarketBasis marketBasis;
    Price price;

    @Before
    public void setUp() throws InvalidParameterException {
        this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d);
        this.price = new Price(this.marketBasis, DEMAND);
    }

    @Test
    public void testGetPriceValue() {
        assertEquals(DEMAND, this.price.getPriceValue(), 0.0d);
    }

    @Test
    public void testGetMarketBasis() {
        assertEquals(this.marketBasis, this.price.getMarketBasis());
        MarketBasis marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 8.0d);
        assertEquals(marketBasis2, this.price.getMarketBasis());
        marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 20, -1.0d, 8.0d);
        assertFalse(marketBasis2.equals(this.price.getMarketBasis()));
    }

    @Test
    public void testPrice() {
        Price price = new Price(this.marketBasis, 1.0d);
        assertEquals(0.0d, price.getPriceValue(), 1.0d);
    }

    @Test
    public void testPriceMarketBasisNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("marketBasis not allowed to be null");
        new Price(null, 1.0d);
    }

    @Test
    public void testPriceNaN() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Price NaN is not valid");
        new Price(marketBasis, Double.NaN);
    }

    @Test
    public void testPriceOutOfBoundsUpper() {
        expectedException.expect(IllegalArgumentException.class);
        new Price(marketBasis, 9.0);
    }

    @Test
    public void testPriceOutOfBoundsLoper() {
        expectedException.expect(IllegalArgumentException.class);
        new Price(marketBasis, -2.0);
    }

    @Test
    public void testPriceMarketBasisDouble() {
        assertNotNull(this.price);
    }

    @Test
    public void testSetPriceValue() {
        this.price = new Price(this.marketBasis, 3.0d);
        assertEquals(3.0d, this.price.getPriceValue(), 0.0d);
        this.price = new Price(this.marketBasis, -1.0d);
        assertEquals(-1.0d, this.price.getPriceValue(), 0.0d);
        this.price = new Price(this.marketBasis, 8.0d);
        assertEquals(8.0d, this.price.getPriceValue(), 0.0d);
    }

    @Test
    public void testSetPriceValueStep() {
        this.price = new Price(this.marketBasis, 3.0d);
        assertEquals(3.0d, this.price.getPriceValue(), 0.0d);
    }

    @Test
    public void testEquals() {
        // check equals null
        Assert.assertThat(price.equals(null), is(false));

        // check reflection
        Assert.assertThat(price.equals(price), is(true));

        // check symmetry
        Price differentPrice = new Price(marketBasis, DEMAND + 2);
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
    public void testHashCode() {
        Price copy = new Price(marketBasis, DEMAND);
        assertThat(copy.equals(price), is(true));
        assertThat(price.equals(copy), is(true));
        assertThat(copy.hashCode(), is(equalTo(price.hashCode())));
    }

    @Test
    public void testToString() {
        final String expected = "Price{priceValue=2}";
        assertThat(price.toString(), is(equalTo(expected)));
    }

}
