package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceStep;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit tests for the {@link ArrayBid} class.
 *
 * @author FAN
 * @version 2.0
 */
public class ArrayBidTest {

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MarketBasis marketBasisFiveSteps;
    private MarketBasis marketBasisTenSteps;
    private final double[] demandFive = new double[] { 100.0, 50.0, 50.0, 0.0, 0.0 };
    private final double[] demandFive2 = new double[] { 100.0, 100.0, 100.0, 100.0, 100.0 };
    private final double[] demandTen = new double[] { 75.0,
                                                     50.0,
                                                     50.0,
                                                     50.0,
                                                     50.0,
                                                     50.0,
                                                     50.0,
                                                     50.0,
                                                     50.0,
                                                     0.0 };
    private final double[] demandTen2 = new double[] { 150.0,
                                                      100.0,
                                                      75.0,
                                                      50.0,
                                                      50.0,
                                                      25.0,
                                                      25.0,
                                                      25.0,
                                                      0.0,
                                                      -25.0 };
    private final double[] ascendingDemand = new double[] { 0.0, 0.0, 5.0, 5.0, 10.0 };
    private final double[] negativeDemand = new double[] { 0.0, 0.0, -10.0, -50.0, -50.0 };
    private ArrayBid bid1;
    private ArrayBid bid2;
    private Bid bid3;
    private Bid bid4;
    private ArrayBid bid5;

    private int bidNumber;

    @Before
    public void setUp() {
        marketBasisFiveSteps = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0, 7.0);
        marketBasisTenSteps = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0, 7.0);
        bidNumber = 1;
        bid1 = new ArrayBid(marketBasisFiveSteps, bidNumber, demandFive);
        bid2 = new ArrayBid(marketBasisFiveSteps, bidNumber, demandFive2);
        bid3 = new ArrayBid(marketBasisTenSteps, bidNumber, demandTen);
        bid4 = new ArrayBid(marketBasisTenSteps, bidNumber, demandTen2);
        bid5 = new ArrayBid(marketBasisFiveSteps, bidNumber, negativeDemand);
    }

    @Test
    public void testConstructorNullMarketBasis() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("marketBasis is not allowed to be null");
        new ArrayBid(null, 0, new double[10]);
    }

    @Test
    public void testConstructorDivergingLength() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Length of the demandArray is not equal to the number of price steps");
        new ArrayBid(marketBasisFiveSteps, 0, new double[10]);
    }

    @Test
    public void testConstructor() {
        assertThat(bid1.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(bid1.getBidNumber(), is(equalTo(bidNumber)));
        assertThat(bid1.getDemand(), is(equalTo(demandFive)));
    }

    @Test
    public void testCopyConstructorBidNumber() {
        int bidNumber = 20;
        ArrayBid copiedBid = new ArrayBid(bid1, bidNumber);
        assertThat(copiedBid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(copiedBid.getBidNumber(), is(equalTo(bidNumber)));
        assertThat(copiedBid.getDemand(), is(equalTo(demandFive)));
    }

    @Test
    public void testCopyConstructor() {
        ArrayBid copiedBid = new ArrayBid(bid1);
        assertThat(copiedBid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(copiedBid.getBidNumber(), is(equalTo(bidNumber)));
        assertThat(copiedBid.getDemand(), is(equalTo(demandFive)));
    }

    @Test
    public void testBuilderNoDemand() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Demand array contains no demand that can be extended");
        builder.build();
    }

    @Test
    public void testBuilderSetDemandAscendingDemand() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        builder.setDemand(1.0);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The demand can not be ascending");
        builder.setDemand(2.0);
    }

    @Test
    public void testBuilderSetDemandOutOfBounds() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        builder.setDemand(3.0);
        builder.setDemand(2.0);
        builder.setDemand(1.0);
        builder.setDemand(0.0);
        builder.setDemand(-1.0);
        expectedException.expect(ArrayIndexOutOfBoundsException.class);
        expectedException.expectMessage("Demand array has already been filled to maximum");
        builder.setDemand(-2.0);
    }

    @Test
    public void testBuilderSetDemandArrayAscending() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The demand can not be ascending");
        builder.setDemandArray(ascendingDemand);
    }

    @Test
    public void testBuilderSetDemandArrayWrongSize() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("supplied array is not same size as number of priceSteps in MarketBasis");
        builder.setDemandArray(demandTen);
    }

    @Test
    public void testBuilderBuildSetDemandArray() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        builder.setBidNumber(bidNumber);
        builder.setDemandArray(demandFive);
        ArrayBid buildBid = builder.build();
        assertThat(buildBid.getBidNumber(), is(equalTo(bidNumber)));
        assertThat(buildBid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(buildBid.getDemand(), is(equalTo(demandFive)));
    }

    @Test
    public void testBuilderBuildSetDemand() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        builder.setBidNumber(bidNumber);
        builder.setDemand(demandFive[0]);
        builder.setDemand(demandFive[1]);
        builder.setDemand(demandFive[2]);
        builder.setDemand(demandFive[3]);
        builder.setDemand(demandFive[4]);
        ArrayBid buildBid = builder.build();
        assertThat(buildBid.getBidNumber(), is(equalTo(bidNumber)));
        assertThat(buildBid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(buildBid.getDemand(), is(equalTo(demandFive)));
    }

    @Test
    public void testBuilderFillArrayToPriceStepNoExtenableDemand() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Demand array contains no demand that can be extended");
        builder.fillArrayToPriceStep(marketBasisFiveSteps.getPriceSteps());
    }

    @Test
    public void testBuilderFillArrayToOutOfBoundsPriceStep() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        double demand = 10.0;
        builder.setDemand(demand);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The supplied priceStep is out of bounds");
        builder.fillArrayToPriceStep(marketBasisFiveSteps.getPriceSteps() + 1);
    }

    @Test
    public void testBuilderFillArrayToPriceStep() {
        ArrayBid.Builder builder = new ArrayBid.Builder(marketBasisFiveSteps);
        double demand = 10.0;
        builder.setDemand(demand);
        builder.fillArrayToPriceStep(marketBasisFiveSteps.getPriceSteps());
        ArrayBid buildBid = builder.build();
        double[] expectedDemand = new double[] { 10.0, 10.0, 10.0, 10.0, 10.0 };
        assertThat(buildBid.getDemand(), is(equalTo(expectedDemand)));
    }

    private final class IntersectionTester {
        private final double[] demand;
        private int bidNr = 0;
        private double intersectionAt = 0;

        public IntersectionTester(double... demand) {
            this.demand = demand;
        }

        public IntersectionTester withBidNr(int bidNr) {
            this.bidNr = bidNr;
            return this;
        }

        public IntersectionTester thatIntersectsAt(double intersectionAt) {
            this.intersectionAt = intersectionAt;
            return this;
        }

        public void assertIntersectionEquals(double expectedPrice) {
            Bid bid = new ArrayBid(demand.length == 5 ? marketBasisFiveSteps : marketBasisTenSteps, bidNr, demand);
            Price price = bid.calculateIntersection(intersectionAt);
            assertEquals(expectedPrice, price.getPriceValue(), 0);
        }
    }

    private IntersectionTester forDemand(double... demand) {
        return new IntersectionTester(demand);
    }

    @Test
    public void testCalculateIntersection() {
        // 5 long
        forDemand(100.0, 100.0, 100.0, 100.0, 100.0).withBidNr(1).assertIntersectionEquals(7.0);
        forDemand(150.0, 100.0, 100.0, 100.0, 100.0).thatIntersectsAt(100).assertIntersectionEquals(4.0);
        forDemand(100.0, 100.0, 100.0, 100.0, 100.0).thatIntersectsAt(100).assertIntersectionEquals(3.0);
        forDemand(100.0, 75.0, 50.0, 50.0, 0.0).assertIntersectionEquals(7.0);
        forDemand(100.0, 75.0, 50.0, 0.0, 0.0).assertIntersectionEquals(6.0);
        forDemand(100.0, 50.0, 50.0, 0.0, 0.0).assertIntersectionEquals(6.0);
        forDemand(100.0, 0.0, 0.0, 0.0, 0.0).assertIntersectionEquals(4.0);
        forDemand(0.0, 0.0, 0.0, 0.0, 0.0).assertIntersectionEquals(3.0);

        // 10 long
        forDemand(75.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 0.0, 0.0, 0.0).assertIntersectionEquals(55.0 / 9.0);
    }

    @Test
    public void testGetMaximumDemand() {
        double maxDemand = demandFive[0];
        assertThat(bid1.getMaximumDemand(), is(equalTo(maxDemand)));
        maxDemand = demandTen[0];
        assertThat(bid3.getMaximumDemand(), is(equalTo(maxDemand)));
    }

    @Test
    public void testGetMinimumDemand() {
        double minDemand = demandFive2[demandFive2.length - 1];
        assertThat(bid2.getMinimumDemand(), is(equalTo(minDemand)));
        minDemand = demandTen2[demandTen2.length - 1];
        assertThat(bid4.getMinimumDemand(), is(equalTo(minDemand)));
    }

    @Test
    public void testToArrayBid() {
        ArrayBid arrayBid = bid1.toArrayBid();
        assertThat(arrayBid, is(equalTo(bid1)));
    }

    @Test
    public void testToPointBid() {
        PointBid pointBid = bid1.toPointBid();
        assertThat(pointBid.getBidNumber(), is(equalTo(bid1.getBidNumber())));
        assertThat(pointBid.getMarketBasis(), is(equalTo(bid1.getMarketBasis())));
        ArrayBid arrayBid = pointBid.toArrayBid();
        assertThat(arrayBid.getDemand(), is(equalTo(bid1.getDemand())));

        Bid desc = new ArrayBid(marketBasisFiveSteps, 0, new double[] { 7.0, 6.0, 5.0, 4.0, 3.0 });
        pointBid = desc.toPointBid();
        PricePoint[] expectedPricePoints = new PricePoint[] { new PricePoint(new Price(marketBasisFiveSteps, -1), 7.0),
                                                             new PricePoint(new Price(marketBasisFiveSteps, 7), 3.0) };
        assertThat(pointBid.getPricePoints(), is(equalTo(expectedPricePoints)));
    }

    @Test
    public void testGetDemand() {
        double[] demand = bid1.getDemand();
        assertThat(demand, is(equalTo(demandFive)));
    }

    @Test
    public void testGetDemandAtPrice() {
        Price price = new Price(marketBasisFiveSteps, 7.0);
        double demand = bid1.getDemandAt(price);
        assertThat(demand, is(equalTo(0.0)));

        price = new Price(marketBasisFiveSteps, 1.0);
        demand = bid1.getDemandAt(price);
        assertThat(demand, is(equalTo(50.0)));

        price = new Price(marketBasisTenSteps, 4.0);
        demand = bid3.getDemandAt(price);
        assertThat(demand, is(equalTo(50.0)));
    }

    @Test
    public void testGetDemandAtPriceStep() {
        PriceStep priceStep = new PriceStep(marketBasisFiveSteps, 4);
        double demand = bid1.getDemandAt(priceStep);
        assertThat(demand, is(equalTo(0.0)));

        priceStep = new PriceStep(marketBasisFiveSteps, 2);
        demand = bid1.getDemandAt(priceStep);
        assertThat(demand, is(equalTo(50.0)));

        priceStep = new PriceStep(marketBasisTenSteps, 2);
        demand = bid3.getDemandAt(priceStep);
        assertThat(demand, is(equalTo(50.0)));
    }

    @Test
    public void testGetDemandAtPriceStepDifferingMarkerBasis() {
        PriceStep priceStep = new PriceStep(marketBasisTenSteps, 4);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The marketbasis of the pricestep does not equal this market basis");
        bid1.getDemandAt(priceStep);
    }

    @Test
    public void testAggregateBid() {
        Bid other = new ArrayBid(marketBasisFiveSteps, 0, new double[] { 75.0, 50.0, 50.0, 50.0, 50.0 });
        ArrayBid aggregatedBid = bid1.aggregate(other);
        double[] expectedDemand = new double[] { 175.0, 100.0, 100.0, 50.0, 50.0 };
        assertThat(aggregatedBid.getDemand(), is(equalTo(expectedDemand)));
    }

    @Test
    public void testAggregateBidDifferentMarketBasis() {
        Bid other = new ArrayBid(marketBasisTenSteps, 0, new double[10]);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("The marketbasis of the supplied bid does not equal this market basis");
        bid1.aggregate(other);
    }

    @Test
    public void testCalculatePricePoints() {

        PointBid pointBid = bid1.toPointBid();
        // These are the expected PricePoints with an demand array of { 100.0,
        // 50.0, 50.0, 0.0, 0.0 }
        PricePoint[] expectedPricePoints = new PricePoint[] {
                                                             new PricePoint(new Price(marketBasisFiveSteps, -1.0),
                                                                            100.0),
                                                             new PricePoint(new Price(marketBasisFiveSteps, 1), 50.0),
                                                             new PricePoint(new Price(marketBasisFiveSteps, 3.0), 50.0),
                                                             new PricePoint(new Price(marketBasisFiveSteps, 5.0), 0.0) };
        assertThat(pointBid.getPricePoints(), is(equalTo(expectedPricePoints)));

        pointBid = bid2.toPointBid();
        // These are the expected PricePoints with an demand array of { 100.0,
        // 50.0, 50.0, 0.0, 0.0 }
        expectedPricePoints = new PricePoint[] { new PricePoint(new Price(marketBasisFiveSteps, 7.0), 100.0) };
        assertThat(pointBid.getPricePoints(), is(equalTo(expectedPricePoints)));
    }

    @Test
    public void testSubtract() {
        double[] expected = new double[] { 0.0, -50.0, -50.0, -100.0, -100.0 };
        ArrayBid subtractedBid = bid1.subtract(bid2);
        assertThat(subtractedBid.getDemand(), is(equalTo(expected)));
        assertThat(subtractedBid.getMarketBasis(), is(equalTo(bid1.getMarketBasis())));
        assertThat(subtractedBid.getBidNumber(), is(equalTo(bid1.getBidNumber())));
    }

    @Test
    public void testTranspose() {
        ArrayBid transposedBid = bid1.transpose(10.0);
        double[] expectedDemand = new double[] { 110.0, 60.0, 60.0, 10.0, 10.0 };
        assertThat(transposedBid.getDemand(), is(equalTo(expectedDemand)));

        transposedBid = bid1.transpose(-10.0);
        expectedDemand = new double[] { 90.0, 40.0, 40.0, -10.0, -10.0 };
        assertThat(transposedBid.getDemand(), is(equalTo(expectedDemand)));

        // transpose negative numbers
        transposedBid = bid5.transpose(-10.0);
        expectedDemand = new double[] { -10.0, -10.0, -20.0, -60.0, -60.0 };
        assertThat(transposedBid.getDemand(), is(equalTo(expectedDemand)));
    }

    @Test
    public void testEquals() {
        // check equals null
        Assert.assertThat(bid1.equals(null), is(false));

        // check reflection
        Assert.assertThat(bid1.equals(bid1), is(true));

        // check symmetry
        Assert.assertThat(bid3.equals(bid4), is(false));
        Assert.assertThat(bid4.equals(bid3), is(false));

        Bid testBid = new ArrayBid(marketBasisFiveSteps, bidNumber, demandFive);
        Assert.assertThat(bid1.equals(testBid), is(true));
        Assert.assertThat(testBid.equals(bid1), is(true));

        // check transition
        MarketBasis equalsBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0, 7.0);
        double[] equalsArray = new double[] { 100.0, 50.0, 50.0, 0.0, 0.0 };
        Bid otherBid = new ArrayBid(equalsBasis, bidNumber, equalsArray);
        Assert.assertThat(bid1.equals(otherBid), is(true));
        Assert.assertThat(testBid.equals(otherBid), is(true));

        // check consistency
        Assert.assertThat(bid1.equals(null), is(false));
        Assert.assertThat(bid2.equals(bid2), is(true));
        Assert.assertThat(otherBid.equals(testBid), is(true));
    }

    @Test
    public void testGetMarketBasis() {
        assertThat(marketBasisFiveSteps, is(bid1.getMarketBasis()));
        assertThat(marketBasisTenSteps, is(bid3.getMarketBasis()));
    }

    @Test
    public void testGetBidNumber() {
        assertThat(bid1.getBidNumber(), is(equalTo(bidNumber)));
    }

    @Test
    public void testHashCode() {
        Bid one = new ArrayBid(marketBasisFiveSteps, 0, new double[] { 3.0, 2.0, 1.0, 0.0, -1.0 });
        Bid other = new ArrayBid(marketBasisFiveSteps, 0, new double[] { 3.0, 2.0, 1.0, 0.0, -1.0 });
        assertThat(one.equals(other), is(true));
        assertThat(other.equals(one), is(true));
        assertThat(one.hashCode(), is(equalTo(other.hashCode())));

        other = new ArrayBid(marketBasisTenSteps, 0,
                             new double[] { 4.0, 4.0, 4.0, 2.0, 2.0, 1.0, 0.0, -1.0, -1.0, -1.0 });
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));

        other = new ArrayBid(marketBasisFiveSteps, 1, new double[] { 3.0, 2.0, 1.0, 0.0, -1.0 });
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));

        other = new ArrayBid(marketBasisFiveSteps, 0, new double[] { 4.0, 2.0, 1.0, 0.0, -1.0 });
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));
    }

    @Test
    public void testToString() {
        String bid1String = bid1.toString();
        assertThat(bid1String.startsWith("ArrayBid"), is(true));
    }
}
