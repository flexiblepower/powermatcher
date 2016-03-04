package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.security.InvalidParameterException;

import net.powermatcher.api.data.MarketBasis;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit tests for the {@link MarketBasis} class.
 * 
 * @author FAN
 * @version 2.1
 */
public class MarketBasisTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testMarketBasisMaximumPrice() {
        String commodity = "tests";
        String currency = "assertions";
        int priceSteps = 100;
        double minValue = -10.0;
        double maxValue = 10.0;
        MarketBasis testBasis = new MarketBasis(commodity, currency, priceSteps, minValue, maxValue);
        assertThat(testBasis.getCommodity(), is(equalTo(commodity)));
        assertThat(testBasis.getCurrency(), is(equalTo(currency)));
        assertThat(testBasis.getPriceSteps(), is(equalTo(priceSteps)));
        assertThat(testBasis.getMinimumPrice(), is(equalTo(minValue)));
        assertThat(testBasis.getMaximumPrice(), is(equalTo(maxValue)));
    }

    @Test
    public void testMarketBasisNegativeMinimum() {
        expectedException.expect(InvalidParameterException.class);
        expectedException.expectMessage("Price steps must be > 0.");
        new MarketBasis("electricity", "EUR", -1, 0, 1);
    }

    @Test
    public void testMarketBasisMaximumPriceTooSmall() {
        expectedException.expect(InvalidParameterException.class);
        expectedException.expectMessage("Maximum price must be > minimum price.");
        new MarketBasis("electricity", "EUR", 10, 2, 1);
    }
}
