package net.powermatcher.api.data.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PointBid.Builder;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PointBidTest {

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MarketBasis marketBasisFiveSteps;
    private MarketBasis marketBasisTenSteps;

    private PricePoint[] pricePoints1;
    private PricePoint[] pricePoints2;

    private int bidNumber;

    private PointBid bid1;
    private PointBid bid2;

    @Before
    public void setUp() throws Exception {
        this.marketBasisFiveSteps = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        this.marketBasisTenSteps = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 7.0d);
        this.bidNumber = 1;
        pricePoints1 = new PricePoint[] { pricePoint(marketBasisFiveSteps, -1, 10.0),
                pricePoint(marketBasisFiveSteps, 7, 2.0) };
        pricePoints2 = new PricePoint[] { pricePoint(marketBasisTenSteps, 1, 20.0),
                pricePoint(marketBasisTenSteps, 7, 25.0) };
        bid1 = new PointBid(marketBasisFiveSteps, bidNumber, pricePoints1);
        bid2 = new PointBid(marketBasisTenSteps, bidNumber, pricePoints2);
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
        new PointBid(null, 0, pricePoints1);
    }

    @Test
    public void testConstructor() {
        PointBid bid = new PointBid(marketBasisFiveSteps, bidNumber, pricePoints1);
        assertThat(bid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        assertThat(bid.getBidNumber(), is(equalTo(bidNumber)));
        assertThat(bid.getPricePoints(), is(equalTo(pricePoints1)));
    }

    @Test
    public void testBuilderMarketBasisNull() {
        PointBid.Builder builder = new Builder(null);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("marketBasis is not allowed to be null");
        builder.build();
    }

    @Test
    public void testBuilderAddPricePoint() {
        PricePoint pricePoint = new PricePoint(new Price(marketBasisFiveSteps, 3), 10);
        PointBid.Builder builder = new Builder(marketBasisFiveSteps);
        builder.add(pricePoint);
        PointBid buildBid = builder.build();
        assertThat(buildBid.getPricePoints()[0], is(equalTo(pricePoint)));
        assertThat(buildBid.getPricePoints().length, is(equalTo(1)));
    }
    
    @Test
    public void testBuilderSetBidNumber() {
        PointBid.Builder builder = new Builder(marketBasisFiveSteps);
        builder.setBidNumber(bidNumber);
        PointBid buildBid = builder.build();
        assertThat(buildBid.getBidNumber(), is(equalTo(bidNumber)));
    }

    @Test
    public void testBuilderAddPriceDemand() {
        double priceValue = 3.0;
        double demand = 5.0;
        Price price = new Price(marketBasisFiveSteps, priceValue);

        PointBid.Builder builder = new Builder(marketBasisFiveSteps);
        builder.add(priceValue, demand);
        PointBid buildBid = builder.build();

        PricePoint pricePoint = new PricePoint(price, demand);
        assertThat(buildBid.getPricePoints()[0], is(equalTo(pricePoint)));
        assertThat(buildBid.getPricePoints().length, is(equalTo(1)));
    }

    @Test
    public void testCalculateIntersection() {
    }

    @Test
    public void testGetMaximumDemand() {
        double maximumDemand = bid1.getMaximumDemand();
        assertThat(maximumDemand, is(equalTo(10.0)));
        maximumDemand = bid2.getMaximumDemand();
        assertThat(maximumDemand, is(equalTo(20.0)));
    }

    @Test
    public void testGetMinimumDemand() {
        double minimumDemand = bid1.getMinimumDemand();
        assertThat(minimumDemand, is(equalTo(2.0)));
        minimumDemand = bid2.getMinimumDemand();
        assertThat(minimumDemand, is(equalTo(25.0)));
    }

    @Test
    public void testToArrayBid() {
        ArrayBid arrayBid = bid1.toArrayBid();
        double[] expected = new double[] { 10.0, 8.0, 6.0, 4.0, 2.0 };
        assertThat(arrayBid.getDemand(), is(equalTo(expected)));

        arrayBid = bid2.toArrayBid();
        expected = new double[] { 20.0, 20.0, 20.0, 20.714285714285715, 21.428571428571427, 22.142857142857142,
                22.857142857142858, 23.571428571428573, 24.285714285714285, 25.0 };
        assertThat(arrayBid.getDemand(), is(equalTo(expected)));
    }

    @Test
    public void testGetDemandAtPrice() {
        Price price = new Price(marketBasisFiveSteps, 4);
        double demandAt = bid1.getDemandAt(price);
        assertThat(demandAt, is(equalTo(5.0)));

        price = new Price(marketBasisFiveSteps, 2);
        demandAt = bid1.getDemandAt(price);
        assertThat(demandAt, is(equalTo(7.0)));

        price = new Price(marketBasisTenSteps, 2);
        demandAt = bid2.getDemandAt(price);
        assertThat(demandAt, is(equalTo(20.833333333333336)));

        price = new Price(marketBasisTenSteps, 3);
        demandAt = bid2.getDemandAt(price);
        assertThat(demandAt, is(equalTo(21.666666666666668)));
        
        price = new Price(marketBasisTenSteps, 7);
        demandAt = bid2.getDemandAt(price);
        assertThat(demandAt, is(equalTo(25.0)));

        price = new Price(marketBasisTenSteps, -1);
        demandAt = bid2.getDemandAt(price);
        assertThat(demandAt, is(equalTo(20.0)));
    }

    @Test
    public void testGetDemandAtPriceStep() {
        Price price = new Price(marketBasisFiveSteps, 4);
        double demandAt = bid1.getDemandAt(price.toPriceStep());
        assertThat(demandAt, is(equalTo(4.0)));

        price = new Price(marketBasisFiveSteps, 2);
        demandAt = bid1.getDemandAt(price.toPriceStep());
        assertThat(demandAt, is(equalTo(6.0)));

        price = new Price(marketBasisTenSteps, 2);
        demandAt = bid2.getDemandAt(price.toPriceStep());
        assertThat(demandAt, is(equalTo(20.555555555555557)));

        price = new Price(marketBasisTenSteps, 3);
        demandAt = bid2.getDemandAt(price.toPriceStep());
        assertThat(demandAt, is(equalTo(22.037037037037038)));
    }

    @Test
    public void testAggregateBid() {
        PointBid other = new PointBid(marketBasisFiveSteps, bidNumber, pricePoints1);
        ArrayBid aggregatedBid = bid1.aggregate(other);
        assertThat(aggregatedBid.getBidNumber(), is(equalTo(0)));
        assertThat(aggregatedBid.getMarketBasis(), is(equalTo(marketBasisFiveSteps)));
        double[] expectedDemand = new double[]{20.0, 16.0, 12.0, 8.0, 4.0};
        assertThat(aggregatedBid.getDemand(), is(equalTo(expectedDemand)));
    }

    @Test
    public void testIterator() {
        PricePoint pp1 = new PricePoint(new Price(marketBasisFiveSteps, 3), 10.0);
        PricePoint pp2 = new PricePoint(new Price(marketBasisFiveSteps, 5), 20.0);
        PricePoint pp3 = new PricePoint(new Price(marketBasisFiveSteps, 7), 30.0);
        PricePoint[] pbArray = new PricePoint[] { pp1, pp2, pp3 };
        PointBid bid = new PointBid(marketBasisFiveSteps, 0, pbArray);
        Iterator<PricePoint> iterator = bid.iterator();

        assertThat(iterator.next(), is(equalTo(pp1)));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(equalTo(pp2)));
        assertThat(iterator.hasNext(), is(true));
        assertThat(iterator.next(), is(equalTo(pp3)));
        assertThat(iterator.hasNext(), is(false));
        expectedException.expect(UnsupportedOperationException.class);
        iterator.remove();
    }

    @Test
    public void testGetPricePoints() {
        PricePoint pp1 = new PricePoint(new Price(marketBasisFiveSteps, 3), 10.0);
        PricePoint pp2 = new PricePoint(new Price(marketBasisFiveSteps, 5), 20.0);
        PricePoint pp3 = new PricePoint(new Price(marketBasisFiveSteps, 7), 30.0);
        PricePoint[] pbArray = new PricePoint[] { pp1, pp2, pp3 };
        PointBid bid = new PointBid(marketBasisFiveSteps, 0, pbArray);
        assertThat(bid.getPricePoints(), is(equalTo(pbArray)));
    }

    @Test
    public void testToString() {
        String bid1String = bid1.toString();
        assertThat(bid1String.startsWith("PointBid"), is(true));
    }

    @Test
    public void testEquals() {
        // check equals null
        Assert.assertThat(bid1.equals(null), is(false));

        // check reflection
        Assert.assertThat(bid1.equals(bid1), is(true));

        // check symmetry
        Assert.assertThat(bid1.equals(bid2), is(false));
        Assert.assertThat(bid2.equals(bid1), is(false));

        Bid testBid = new PointBid(marketBasisFiveSteps, bidNumber, pricePoints1);
        Assert.assertThat(bid1.equals(testBid), is(true));
        Assert.assertThat(testBid.equals(bid1), is(true));

        // check transition
        MarketBasis equalsBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        PricePoint[] equalsArray = new PricePoint[] { pricePoint(marketBasisFiveSteps, -1, 10.0),
                pricePoint(marketBasisFiveSteps, 7, 2.0) };
        Bid otherBid = new PointBid(equalsBasis, bidNumber, equalsArray);
        Assert.assertThat(bid1.equals(otherBid), is(true));
        Assert.assertThat(testBid.equals(otherBid), is(true));

        // check consistency
        Assert.assertThat(bid1.equals(null), is(false));
        Assert.assertThat(bid2.equals(bid2), is(true));
        Assert.assertThat(otherBid.equals(testBid), is(true));

    }

    @Test
    public void testHashCode() {
        PointBid one = new PointBid(marketBasisTenSteps, 0, pricePoints2);
        PointBid other = new PointBid(marketBasisTenSteps, 0, pricePoints2);
        assertThat(one.equals(other), is(true));
        assertThat(other.equals(one), is(true));
        assertThat(one.hashCode(), is(equalTo(other.hashCode())));

        other = new PointBid(marketBasisFiveSteps, 0, pricePoints1);
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));

        other = new PointBid(marketBasisFiveSteps, 1, pricePoints1);
        assertThat(one.hashCode(), is(not(equalTo(other.hashCode()))));
    }

}
