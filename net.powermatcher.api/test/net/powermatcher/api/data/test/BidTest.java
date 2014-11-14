package net.powermatcher.api.data.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.security.InvalidParameterException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PricePoint;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author IBM
 * @version 0.9.0
 */
public class BidTest {

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";

    private static void assertArrayEquals(final double[] expected, final double[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals(expected[i], actual[i], 0.0d);
        }
    }

    MarketBasis marketBasis0;
    MarketBasis marketBasis1;
    MarketBasis marketBasis2;
    double[] demand1 = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };
    double[] demand2 = new double[] { 100.0d, 100.0d, 100.0d, 100.0d, 100.0d };
    PricePoint[] points3 = new PricePoint[] { new PricePoint(-1, 50.0d) };
    PricePoint[] points4 = new PricePoint[] { new PricePoint(0, 50.0d), new PricePoint(0, 0.0d) };
    double[] demand5 = new double[] { 100.0d, 50.0d, 50.0d, 50.0d, 0.0d };
    double[] demand6 = new double[] { 100.0d, 75.0d, 50.0d, 0.0d, 0.0d };
    double[] demand7 = new double[] { 100.0d, 75.0d, 50.0d, 25.0d, 0.0d };
    double[] demand8 = new double[] { 100.0d, 75.0d, 50.0d, 30.0d, 0.0d };
    double[] demand9 = new double[] { 0.0d, 0.0d, 0.0d, -50.0d, -50.0d };
    PricePoint[] points10 = new PricePoint[] { new PricePoint(20, 0.0d), new PricePoint(20, -100.0d) };
    Bid bid0;
    Bid bid1;
    Bid bid2;
    Bid bid3;
    Bid bid4;
    Bid bid5;
    Bid bid6;
    Bid bid7;
    Bid bid8;
    Bid bid9;
    Bid bid10;

    /**
     * @throws InvalidParameterException
     */
    @Before
    public void setUp() throws InvalidParameterException {
        this.marketBasis0 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        this.marketBasis1 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 7.0d);
        this.marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 255, -127.0d, 127.0d);
        this.bid0 = new Bid(this.marketBasis0);
        this.bid1 = new Bid(this.marketBasis0, this.demand1);
        this.bid2 = new Bid(this.marketBasis0, this.demand2);
        this.bid3 = new Bid(this.marketBasis0, this.points3);
        this.bid4 = new Bid(this.marketBasis0, this.points4);
        this.bid5 = new Bid(this.marketBasis0, this.demand5);
        this.bid6 = new Bid(this.marketBasis0, this.demand6);
        this.bid7 = new Bid(this.marketBasis0, this.demand7);
        this.bid8 = new Bid(this.marketBasis0, this.demand8);
        this.bid9 = new Bid(this.marketBasis0, this.demand9);
        this.bid10 = new Bid(this.marketBasis2, this.points10);
    }

    /**
	 * 
	 */
    @Test
    public void testAggregate() {
        Bid bid;
        PricePoint[] pricePoints;

        bid = new Bid(this.marketBasis0);
        bid = bid.aggregate(this.bid1);
        bid = bid.aggregate(this.bid2);
        pricePoints = bid.getPricePoints();
        assertSame(pricePoints, null);
        pricePoints = bid.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        bid = new Bid(this.marketBasis0, pricePoints);
        assertEquals(200.0d, bid.getDemand()[0], 0.0d);
        assertEquals(150.0d, bid.getDemand()[1], 0.0d);
        assertEquals(150.0d, bid.getDemand()[2], 0.0d);
        assertEquals(100.0d, bid.getDemand()[3], 0.0d);
        assertEquals(100.0d, bid.getDemand()[3], 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testEquilibrium() {
        assertEquals(1.0d, this.bid0.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(5.0d, this.bid1.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bid2.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bid3.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(1.0d, this.bid4.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bid5.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(5.0d, this.bid6.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bid7.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bid8.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(-1.0d, this.bid9.calculateIntersection(0).getCurrentPrice(), 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testGetDemand() {
        assertEquals(0.0d, this.bid0.getDemand()[0], 0.0d);
        assertEquals(0.0d, this.bid0.getDemand()[4], 0.0d);
        assertArrayEquals(this.demand1, this.bid1.getDemand());
        assertArrayEquals(this.demand2, this.bid2.getDemand());
        assertEquals(50.0d, this.bid3.getDemand()[0], 0.0d);
        assertEquals(50.0d, this.bid3.getDemand()[4], 0.0d);
        assertEquals(50.0d, this.bid4.getDemand()[0], 0.0d);
        assertEquals(0.0d, this.bid4.getDemand()[1], 0.0d);
        assertEquals(0.0d, this.bid4.getDemand()[4], 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testGetPricePoints() {
        PricePoint[] pricePoints;
        pricePoints = this.bid0.getCalculatedPricePoints();
        assertEquals(1, pricePoints.length);

        pricePoints = this.bid1.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        this.bid1 = new Bid(this.bid1.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand1, this.bid1.getDemand());

        pricePoints = this.bid2.getCalculatedPricePoints();
        assertEquals(1, pricePoints.length);
        this.bid2 = new Bid(this.bid2.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand2, this.bid2.getDemand());

        pricePoints = this.bid3.getCalculatedPricePoints();
        assertEquals(1, pricePoints.length);

        pricePoints = this.bid4.getCalculatedPricePoints();
        assertEquals(2, pricePoints.length);

        pricePoints = this.bid5.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        this.bid5 = new Bid(this.bid5.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand5, this.bid5.getDemand());

        pricePoints = this.bid6.getCalculatedPricePoints();
        assertEquals(3, pricePoints.length);
        this.bid6 = new Bid(this.bid6.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand6, this.bid6.getDemand());

        pricePoints = this.bid7.getCalculatedPricePoints();
        assertEquals(2, pricePoints.length);
        this.bid7 = new Bid(this.bid7.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand7, this.bid7.getDemand());

        pricePoints = this.bid8.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        this.bid8 = new Bid(this.bid8.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand8, this.bid8.getDemand());
    }

    /**
	 * 
	 */
    @Test
    public void testToMarketBasis() {
        Bid bid;
        PricePoint[] pricePoints;

        bid = this.bid0.toMarketBasis(this.marketBasis0);
        assertSame(this.bid0, bid);

        bid = this.bid1.toMarketBasis(this.marketBasis1);
        assertNotSame(this.bid1, bid);
        pricePoints = bid.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        assertEquals(100.0d, bid.getDemand()[0], 0.0d);
        assertEquals(100.0d, bid.getDemand()[1], 0.0d);
        assertEquals(50.0d, bid.getDemand()[2], 0.0d);
        assertEquals(50.0d, bid.getDemand()[3], 0.0d);
        assertEquals(50.0d, bid.getDemand()[4], 0.0d);
        assertEquals(50.0d, bid.getDemand()[5], 0.0d);
        assertEquals(0.0d, bid.getDemand()[6], 0.0d);

        bid = bid.toMarketBasis(this.marketBasis0);
        pricePoints = bid.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        assertEquals(100.0d, bid.getDemand()[0], 0.0d);
        assertEquals(50.0d, bid.getDemand()[1], 0.0d);
        assertEquals(50.0d, bid.getDemand()[2], 0.0d);
        assertEquals(0.0d, bid.getDemand()[3], 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testEffectiveDemand() {
        assertEquals(-100.0d, this.bid10.getDemand(20.0), 0.0d);
    }
    
    /**
     * Tests the equals method of the Bid class. An override equals method should be
     * reflexive, transitive. symmetric and consistent 
     */
    @Test
    public void testEquals(){
        //check equals null
        Assert.assertThat(bid0.equals(null), is(false));
        
        //check reflection
        Assert.assertThat(bid1.equals(bid1), is(true));
        
        // check symmetry
        Assert.assertThat(bid3.equals(bid4), is(false));
        Assert.assertThat(bid4.equals(bid3), is(false));
        
        Bid testBid = new Bid(marketBasis0, demand1);
        Assert.assertThat(bid1.equals(testBid), is(true));
        Assert.assertThat(testBid.equals(bid1), is(true));
        
        //check transition
        MarketBasis equalsBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        double[] equalsArray = new double[]{100.0d, 50.0d, 50.0d, 0.0d, 0.0d};
        Bid otherBid = new Bid(equalsBasis, equalsArray);
        Assert.assertThat(bid1.equals(otherBid), is(true));
        Assert.assertThat(testBid.equals(otherBid), is(true));
        
        //check consistency
        Assert.assertThat(bid0.equals(null), is(false));
        Assert.assertThat(bid2.equals(bid2), is(true));
        Assert.assertThat(otherBid.equals(testBid), is(true));
    }

}
