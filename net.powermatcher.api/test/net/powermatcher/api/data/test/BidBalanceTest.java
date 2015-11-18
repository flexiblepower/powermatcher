package net.powermatcher.api.data.test;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

public class BidBalanceTest {

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 100, 0, 1);

    @Test
    public void testOneFlexible() {
        Bid bid1 = Bid.flatDemand(marketBasis, -1500);
        Bid bid2 = Bid.create(marketBasis).add(0.434, 1700).add(0.434, 0).build();

        Bid aggregated = bid1.aggregate(bid2);

        Price price = aggregated.calculateIntersection(0);

        assertEquals(-1500, bid1.getDemandAt(price), 0.001);
        assertEquals(1700, bid2.getDemandAt(price), 0.001);
    }

    @Test
    public void testTwoFlexible() {
        Bid bid1 = Bid.create(marketBasis).add(0.5, 1999).add(0.5, 0).build();
        Bid bid2 = Bid.create(marketBasis).add(0.5, 0).add(0.5, -2000).build();

        Bid aggregated = bid1.aggregate(bid2);

        Price price = aggregated.calculateIntersection(0);

        assertEquals(1999, bid1.getDemandAt(price), 0.001);
        assertEquals(0, bid2.getDemandAt(price), 0.001);
    }

}
