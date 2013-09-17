package net.powermatcher.fpai.test;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;

import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.values.PowerValue;
import org.junit.Test;

public class BidAnalyzerTest {
    private static final MarketBasis MARKET_BASIS = new MarketBasis("Electricity", "EUR", 101, 0, 50, 1, 0);

    private final PowerValue power1 = new PowerValue(10, PowerUnit.WATT);
    private final PowerValue power2 = new PowerValue(0, PowerUnit.WATT);
    private final PowerValue power3 = new PowerValue(-10, PowerUnit.WATT);

    /** test for all prices apart from the lowest and the highest */
    @Test
    public void testAssertStepBid() {
        for (int priceIndex = 1; priceIndex < MARKET_BASIS.getPriceSteps() - 1; priceIndex++) {
            double price = MARKET_BASIS.toPrice(priceIndex);

            BidInfo bid = createBid(power1, power2, price);

            BidAnalyzer.assertStepBid(bid, null, null, null);
            BidAnalyzer.assertStepBid(bid, power1, power2, price);

            BidAnalyzer.assertStepBid(bid, power1, null, null);
            BidAnalyzer.assertStepBid(bid, null, power2, null);
            BidAnalyzer.assertStepBid(bid, null, null, price);

            BidAnalyzer.assertStepBid(bid, power1, power2, null);
            BidAnalyzer.assertStepBid(bid, power1, null, price);
            BidAnalyzer.assertStepBid(bid, null, power2, price);
        }
    }

    /** test that with a must-run bid, the assertion fails */
    @Test(expected = AssertionError.class)
    public void testAssertStepBidMinimumPrice() {
        double price = MARKET_BASIS.toPrice(0);

        BidInfo bid = createBid(power1, power2, price);
        BidAnalyzer.assertStepBid(bid, power1, power2, price);
    }

    /** test that with a must-run bid, the assertion fails */
    @Test(expected = AssertionError.class)
    public void testAssertStepBidMaximumPrice() {
        double price = MARKET_BASIS.toPrice(MARKET_BASIS.getPriceSteps());

        BidInfo bid = createBid(power1, power2, price);
        BidAnalyzer.assertStepBid(bid, power1, power2, price);
    }

    /** test that with a must-run bid, the assertion fails */
    @Test(expected = AssertionError.class)
    public void testAssertStepMultiStep() {
        int price1 = MARKET_BASIS.toNormalizedPrice(MARKET_BASIS.getPriceSteps() / 4);
        int price2 = MARKET_BASIS.toNormalizedPrice(MARKET_BASIS.getPriceSteps() / 4 * 3);

        PricePoint pricePoint1 = new PricePoint(price1, power1.getValueAs(PowerUnit.WATT));
        PricePoint pricePoint2 = new PricePoint(price1, power2.getValueAs(PowerUnit.WATT));
        PricePoint pricePoint3 = new PricePoint(price2, power2.getValueAs(PowerUnit.WATT));
        PricePoint pricePoint4 = new PricePoint(price2, power3.getValueAs(PowerUnit.WATT));

        BidInfo bid = new BidInfo(MARKET_BASIS, new PricePoint[] { pricePoint1, pricePoint2, pricePoint3, pricePoint4 });
        BidAnalyzer.assertStepBid(bid);
    }

    private BidInfo createBid(PowerValue power1, PowerValue power2, double price) {
        int normalizedPrice = MARKET_BASIS.toNormalizedPrice(price);
        PricePoint pricePoint1 = new PricePoint(normalizedPrice, power1.getValueAs(PowerUnit.WATT));
        PricePoint pricePoint2 = new PricePoint(normalizedPrice, power2.getValueAs(PowerUnit.WATT));

        return new BidInfo(MARKET_BASIS, pricePoint1, pricePoint2);
    }
}
