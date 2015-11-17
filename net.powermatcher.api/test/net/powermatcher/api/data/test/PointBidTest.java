package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBidBuilder;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceStep;

/***
 * JUnit tests for the{
 *
 * @link PointBid} class.
 *
 * @author FAN
 * @version 2.0
 */
public class PointBidTest {
    private static final double DEMAND_ACCURACY = 1e-9;

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MarketBasis marketBasisFiveSteps;
    private MarketBasis marketBasisTenSteps;

    private PricePoint[] pricePoints1;
    private PricePoint[] pricePoints2;

    private Bid bid1;
    private Bid bid2;

    @Before
    public void setUp() throws Exception {
        marketBasisFiveSteps = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        marketBasisTenSteps = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 7.0d);
        pricePoints1 = new PricePoint[] { pricePoint(marketBasisFiveSteps, -1, 10.0),
                                          pricePoint(marketBasisFiveSteps, 7, 2.0) };
        pricePoints2 = new PricePoint[] { pricePoint(marketBasisTenSteps, 1, 25.0),
                                          pricePoint(marketBasisTenSteps, 7, 20.0) };
        bid1 = Bid.createWithPricePoints(marketBasisFiveSteps).addAll(pricePoints1).build();
        bid2 = Bid.createWithPricePoints(marketBasisTenSteps).addAll(pricePoints2).build();
    }

    /**
     * this method makes it easier to create arrays of pricePoints for testing.
     */
    private PricePoint pricePoint(MarketBasis marketBasis, int price, double demand) {
        return new PricePoint(new Price(marketBasis, price), demand);
    }

    @Test
    public void testConstructorNullMarketBasis() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("marketBasis is not allowed to be null");
        new PointBidBuilder(null);
    }

    @Test
    public void testConstructor() {
        Bid bid = Bid.createWithPricePoints(marketBasisFiveSteps)
                     .add(pricePoint(marketBasisFiveSteps, -1, 10.0))
                     .add(pricePoint(marketBasisFiveSteps, 7, 2.0))
                     .build();
        assertThat(bid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(bid.getDemand(), is(equalTo(bid1.getDemand())));
    }

    @Test
    public void testBuilderMarketBasisNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("marketBasis is not allowed to be null");
        Bid.createWithPricePoints(null).build();
    }

    @Test
    public void testGetMaximumDemand() {
        double maximumDemand = bid1.getMaximumDemand();
        assertThat(maximumDemand, is(equalTo(10.0)));
        maximumDemand = bid2.getMaximumDemand();
        assertThat(maximumDemand, is(equalTo(25.0)));
    }

    @Test
    public void testGetMinimumDemand() {
        double minimumDemand = bid1.getMinimumDemand();
        assertThat(minimumDemand, is(equalTo(2.0)));
        minimumDemand = bid2.getMinimumDemand();
        assertThat(minimumDemand, is(equalTo(20.0)));
    }

    @Test
    public void testToArrayBid() {
        double[] expected = new double[] { 10.0, 8.0, 6.0, 4.0, 2.0 };
        assertThat(bid1.getDemand(), is(equalTo(expected)));

        expected = new double[] { 25.0,
                                  25.0,
                                  25.0,
                                  25 - 15.0 / 27.0,
                                  25 - 35.0 / 27.0,
                                  25 - 55.0 / 27.0,
                                  25 - 75.0 / 27.0,
                                  25 - 95.0 / 27.0,
                                  25 - 115.0 / 27.0,
                                  20.0 };
        assertArrayEquals(expected, bid2.getDemand(), 1e-9);
    }

    @Test
    public void testGetDemandAtPriceStep() {
        assertEquals(10.0, bid1.getDemandAt(new PriceStep(bid1.getMarketBasis(), 0)), DEMAND_ACCURACY);
        assertEquals(8.0, bid1.getDemandAt(new PriceStep(bid1.getMarketBasis(), 1)), DEMAND_ACCURACY);
        assertEquals(6.0, bid1.getDemandAt(new PriceStep(bid1.getMarketBasis(), 2)), DEMAND_ACCURACY);
        assertEquals(4.0, bid1.getDemandAt(new PriceStep(bid1.getMarketBasis(), 3)), DEMAND_ACCURACY);
        assertEquals(2.0, bid1.getDemandAt(new PriceStep(bid1.getMarketBasis(), 4)), DEMAND_ACCURACY);

        assertEquals(25, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 0)), DEMAND_ACCURACY);
        assertEquals(25, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 1)), DEMAND_ACCURACY);
        assertEquals(25, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 2)), DEMAND_ACCURACY);
        assertEquals(25 - 15.0 / 27.0, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 3)), DEMAND_ACCURACY);
        assertEquals(25 - 35.0 / 27.0, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 4)), DEMAND_ACCURACY);
        assertEquals(25 - 55.0 / 27.0, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 5)), DEMAND_ACCURACY);
        assertEquals(25 - 75.0 / 27.0, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 6)), DEMAND_ACCURACY);
        assertEquals(25 - 95.0 / 27.0, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 7)), DEMAND_ACCURACY);
        assertEquals(25 - 115.0 / 27.0, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 8)), DEMAND_ACCURACY);
        assertEquals(20, bid2.getDemandAt(new PriceStep(bid2.getMarketBasis(), 9)), DEMAND_ACCURACY);
    }

    @Test
    public void testToString() {
        String bid1String = bid1.toString();
        assertThat(bid1String.startsWith("Bid"), is(true));
    }

    @Test
    public void testEquals() {
        // check equals null
        assertThat(bid1.equals(null), is(false));

        // check reflection
        assertThat(bid1.equals(bid1), is(true));

        // check symmetry
        assertThat(bid1.equals(bid2), is(false));
        assertThat(bid2.equals(bid1), is(false));

        PointBidBuilder builder = Bid.createWithPricePoints(marketBasisFiveSteps);
        for (PricePoint pp : pricePoints1) {
            builder.add(pp);
        }
        Bid testBid = builder.build();
        assertThat(bid1.equals(testBid), is(true));
        assertThat(testBid.equals(bid1), is(true));

        // check transition
        MarketBasis equalsBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        PricePoint[] equalsArray = new PricePoint[] { pricePoint(marketBasisFiveSteps, -1, 10.0),
                                                      pricePoint(marketBasisFiveSteps, 7, 2.0) };
        Bid otherBid = Bid.createWithPricePoints(equalsBasis).addAll(equalsArray).build();
        assertThat(bid1.equals(otherBid), is(true));
        assertThat(testBid.equals(otherBid), is(true));

        // check consistency
        assertThat(bid1.equals(null), is(false));
        assertThat(bid2.equals(bid2), is(true));
        assertThat(otherBid.equals(testBid), is(true));

    }

    @Test
    public void testHashCode() {
        Bid one = Bid.createWithPricePoints(marketBasisTenSteps).addAll(pricePoints2).build();
        Bid other = Bid.createWithPricePoints(marketBasisTenSteps).addAll(pricePoints2).build();
        assertThat(one.equals(other), is(true));
        assertThat(other.equals(one), is(true));
        assertThat(one.hashCode(), is(equalTo(other.hashCode())));

        other = Bid.createWithPricePoints(marketBasisFiveSteps).addAll(pricePoints1).build();
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));

        other = Bid.createWithPricePoints(marketBasisFiveSteps).addAll(pricePoints1).build();
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));
    }

    @Test
    public void testConversionToArray() {
        MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 7, -1, 5);
        Bid bid = Bid.createWithPricePoints(marketBasis)
                     .add(1, 100)
                     .add(1, 50)
                     .add(2, 50)
                     .add(3, 0)
                     .add(4, 0)
                     .add(4, -100)
                     .build();

        double[] demandArray = bid.getDemand();

        double[] expectedDemandArray = new double[] { 100, 100, 50, 50, 0, -100, -100 };

        assertArrayEquals(expectedDemandArray, demandArray, DEMAND_ACCURACY);
    }
}
