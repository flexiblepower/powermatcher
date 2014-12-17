package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;

import org.junit.Before;
import org.junit.Test;

public class PriceUpdateTest {
    
    private MarketBasis marketBasis;
    private Price price;
    private int bidNumber;

    @Before
    public void setUp() throws Exception {
        marketBasis = new MarketBasis("electricity", "EURO", 5, 0, 10);
        price = new Price(marketBasis, 4);
        bidNumber = 1;
    }

    @Test
    public void testHashCode() {
        PriceUpdate priceUpdate = new PriceUpdate(price, bidNumber);
        PriceUpdate other = new PriceUpdate(price, bidNumber);
        assertThat(priceUpdate.hashCode(), is(equalTo(other.hashCode())));
    }

    @Test
    public void testPriceUpdate() {
        PriceUpdate priceUpdate = new PriceUpdate(price, bidNumber);
        assertThat(priceUpdate.getPrice(), is(equalTo(price)));
        assertThat(priceUpdate.getBidNumber(), is(equalTo(bidNumber)));
    }

    @Test
    public void testEquals() {
        PriceUpdate priceUpdate = new PriceUpdate(price, bidNumber);
        PriceUpdate other = new PriceUpdate(price, bidNumber);
        assertThat(priceUpdate.equals(null), is(false));
        assertThat(priceUpdate.equals(priceUpdate), is(true));
        assertThat(priceUpdate.equals(other), is(true));
        assertThat(other.equals(priceUpdate), is(true));
    }

    @Test
    public void testToString() {
        PriceUpdate priceUpdate = new PriceUpdate(price, bidNumber);
        assertThat(priceUpdate.toString().startsWith("PriceUpdate"), is(true));
    }

}
