package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceStep;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the {@link PriceStep} class.
 * 
 * @author FAN
 * @version 2.0
 */
public class PriceStepTest {

	private MarketBasis marketBasis;
	private int step;

	@Before
	public void setUp() throws Exception {
		marketBasis = new MarketBasis("electricity", "EURO", 5, 0, 10);
		step = 4;
	}

	@Test
	public void testHashCode() {
		PriceStep priceStep = new PriceStep(marketBasis, step);
		PriceStep other = new PriceStep(marketBasis, step);
		assertThat(priceStep.hashCode(), is(equalTo(other.hashCode())));
	}

	@Test
	public void testConstructor() {
		PriceStep priceStep = new PriceStep(marketBasis, step);
		assertThat(priceStep.getMarketBasis(), is(equalTo(marketBasis)));
		assertThat(priceStep.getPriceStep(), is(equalTo(step)));
	}

	@Test
	public void testToPrice() {
		PriceStep priceStep = new PriceStep(marketBasis, step);
		Price expected = new Price(marketBasis, 10);
		Price price = priceStep.toPrice();
		assertThat(price, is(equalTo(expected)));
	}

	@Test
	public void testEquals() {
		PriceStep priceStep = new PriceStep(marketBasis, step);
		PriceStep other = new PriceStep(marketBasis, step);
		assertThat(priceStep.equals(null), is(false));
		assertThat(priceStep.equals(priceStep), is(true));
		assertThat(priceStep.equals(other), is(true));
		assertThat(other.equals(priceStep), is(true));
	}

	@Test
	public void testToString() {
		PriceStep priceStep = new PriceStep(marketBasis, step);
		String string = priceStep.toString();
		assertThat(string.startsWith("PriceStep"), is(true));
		assertThat(string.contains(Integer.toString(step)), is(true));
	}

}
