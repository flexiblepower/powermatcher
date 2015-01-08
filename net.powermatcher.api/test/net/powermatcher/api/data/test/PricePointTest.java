package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the {@link PricePoint} class.
 * 
 * @author FAN
 * @version 2.0
 */
public class PricePointTest {

	private MarketBasis marketBasis;
	private Price price;
	private double demand;

	@Before
	public void setUp() throws Exception {
		marketBasis = new MarketBasis("electricity", "EURO", 5, 0, 10);
		price = new Price(marketBasis, 10);
		demand = 4.0;
	}

	@Test
	public void testHashCode() {
		PricePoint pricePoint = new PricePoint(price, demand);
		PricePoint other = new PricePoint(new Price(marketBasis, 10), 4.0);
		assertThat(pricePoint.hashCode(), is(equalTo(other.hashCode())));
	}

	@Test
	public void testConstructorPriceDemand() {
		PricePoint pricePoint = new PricePoint(price, demand);
		assertThat(pricePoint.getPrice(), is(equalTo(price)));
		assertThat(pricePoint.getDemand(), is(equalTo(demand)));
	}

	@Test
	public void testConstructorMarketBasis() {
		PricePoint pricePoint = new PricePoint(marketBasis, 10, demand);
		assertThat(pricePoint.getPrice(), is(equalTo(price)));
		assertThat(pricePoint.getDemand(), is(equalTo(demand)));
	}

	@Test
	public void testEquals() {
		PricePoint pricePoint = new PricePoint(price, demand);
		PricePoint other = new PricePoint(new Price(marketBasis, 10), 4.0);
		assertThat(pricePoint.equals(null), is(false));
		assertThat(pricePoint.equals(pricePoint), is(true));
		assertThat(pricePoint.equals(other), is(true));
		assertThat(other.equals(pricePoint), is(true));

	}

	@Test
	public void testToString() {
		PricePoint pricePoint = new PricePoint(price, demand);
		String ppString = pricePoint.toString();
		assertThat(ppString.startsWith("PricePoint"), is(true));
		assertThat(ppString.contains("Price"), is(true));
		assertThat(ppString.contains(Double.toString(demand)), is(true));
	}

	@Test
	public void testCompareTo() {
		PricePoint pricePoint = new PricePoint(price, demand);
		PricePoint other = new PricePoint(new Price(marketBasis, 10), 4.0);
		assertThat(pricePoint.compareTo(other), is(equalTo(0)));
	}

}
