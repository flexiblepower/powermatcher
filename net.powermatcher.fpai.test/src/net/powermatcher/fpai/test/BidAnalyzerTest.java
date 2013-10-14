package net.powermatcher.fpai.test;

import static javax.measure.unit.SI.WATT;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Power;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;

public class BidAnalyzerTest extends TestCase {
    private static final MarketBasis MARKET_BASIS = new MarketBasis("Electricity", "EUR", 101, 0, 50, 1, 0);

    private final Measurable<Power> power1 = Measure.valueOf(10, WATT);
    private final Measurable<Power> power2 = Measure.valueOf(0, WATT);
    private final Measurable<Power> power3 = Measure.valueOf(-10, WATT);

    /** test for all prices apart from the lowest and the highest */
    public void testBidAnalyzer() {
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
    public void testAssertStepBidMinimumPrice() {
        try {
            double price = MARKET_BASIS.toPrice(0);

            BidInfo bid = createBid(power1, power2, price);
            BidAnalyzer.assertStepBid(bid, power1, power2, price);
            fail("Expected Exception");
        } catch (AssertionFailedError e) {
        }
    }

    /** test that with a must-run bid, the assertion fails */
    public void testAssertStepBidMaximumPrice() {
        try {
            double price = MARKET_BASIS.toPrice(MARKET_BASIS.getPriceSteps());

            BidInfo bid = createBid(power1, power2, price);
            BidAnalyzer.assertStepBid(bid, power1, power2, price);

            fail("Expected Exception");
        } catch (AssertionFailedError e) {
        }
    }

    /** test that with a must-run bid, the assertion fails */
    public void testAssertStepMultiStep() {
        try {
            int price1 = MARKET_BASIS.toNormalizedPrice(MARKET_BASIS.getPriceSteps() / 4);
            int price2 = MARKET_BASIS.toNormalizedPrice(MARKET_BASIS.getPriceSteps() / 4 * 3);

            PricePoint pricePoint1 = new PricePoint(price1, power1.doubleValue(WATT));
            PricePoint pricePoint2 = new PricePoint(price1, power2.doubleValue(WATT));
            PricePoint pricePoint3 = new PricePoint(price2, power2.doubleValue(WATT));
            PricePoint pricePoint4 = new PricePoint(price2, power3.doubleValue(WATT));

            BidInfo bid = new BidInfo(MARKET_BASIS, new PricePoint[] { pricePoint1,
                                                                      pricePoint2,
                                                                      pricePoint3,
                                                                      pricePoint4 });
            BidAnalyzer.assertStepBid(bid);

            fail("Expected Exception");
        } catch (AssertionFailedError e) {
        }
    }

    private BidInfo createBid(Measurable<Power> power1, Measurable<Power> power2, double price) {
        int normalizedPrice = MARKET_BASIS.toNormalizedPrice(price);
        PricePoint pricePoint1 = new PricePoint(normalizedPrice, power1.doubleValue(WATT));
        PricePoint pricePoint2 = new PricePoint(normalizedPrice, power2.doubleValue(WATT));

        return new BidInfo(MARKET_BASIS, pricePoint1, pricePoint2);
    }
}
