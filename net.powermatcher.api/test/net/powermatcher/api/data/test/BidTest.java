package net.powermatcher.api.data.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.security.InvalidParameterException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PricePoint;

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
    Bid bidInfo0;
    Bid bidInfo1;
    Bid bidInfo2;
    Bid bidInfo3;
    Bid bidInfo4;
    Bid bidInfo5;
    Bid bidInfo6;
    Bid bidInfo7;
    Bid bidInfo8;
    Bid bidInfo9;
    Bid bidInfo10;

    /**
     * @throws InvalidParameterException
     */
    @Before
    public void setUp() throws InvalidParameterException {
        this.marketBasis0 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        this.marketBasis1 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 10, -1.0d, 7.0d);
        this.marketBasis2 = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 255, -127.0d, 127.0d);
        this.bidInfo0 = new Bid(this.marketBasis0);
        this.bidInfo1 = new Bid(this.marketBasis0, this.demand1);
        this.bidInfo2 = new Bid(this.marketBasis0, this.demand2);
        this.bidInfo3 = new Bid(this.marketBasis0, this.points3);
        this.bidInfo4 = new Bid(this.marketBasis0, this.points4);
        this.bidInfo5 = new Bid(this.marketBasis0, this.demand5);
        this.bidInfo6 = new Bid(this.marketBasis0, this.demand6);
        this.bidInfo7 = new Bid(this.marketBasis0, this.demand7);
        this.bidInfo8 = new Bid(this.marketBasis0, this.demand8);
        this.bidInfo9 = new Bid(this.marketBasis0, this.demand9);
        this.bidInfo10 = new Bid(this.marketBasis2, this.points10);
    }

    /**
	 * 
	 */
    @Test
    public void testAggregate() {
        Bid bidInfo;
        PricePoint[] pricePoints;

        bidInfo = new Bid(this.marketBasis0);
        bidInfo = bidInfo.aggregate(this.bidInfo1);
        bidInfo = bidInfo.aggregate(this.bidInfo2);
        pricePoints = bidInfo.getPricePoints();
        assertSame(pricePoints, null);
        pricePoints = bidInfo.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        bidInfo = new Bid(this.marketBasis0, pricePoints);
        assertEquals(200.0d, bidInfo.getDemand()[0], 0.0d);
        assertEquals(150.0d, bidInfo.getDemand()[1], 0.0d);
        assertEquals(150.0d, bidInfo.getDemand()[2], 0.0d);
        assertEquals(100.0d, bidInfo.getDemand()[3], 0.0d);
        assertEquals(100.0d, bidInfo.getDemand()[3], 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testEquilibrium() {
        assertEquals(1.0d, this.bidInfo0.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(5.0d, this.bidInfo1.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bidInfo2.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bidInfo3.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(1.0d, this.bidInfo4.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bidInfo5.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(5.0d, this.bidInfo6.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bidInfo7.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(7.0d, this.bidInfo8.calculateIntersection(0).getCurrentPrice(), 0.0d);
        assertEquals(-1.0d, this.bidInfo9.calculateIntersection(0).getCurrentPrice(), 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testGetDemand() {
        assertEquals(0.0d, this.bidInfo0.getDemand()[0], 0.0d);
        assertEquals(0.0d, this.bidInfo0.getDemand()[4], 0.0d);
        assertArrayEquals(this.demand1, this.bidInfo1.getDemand());
        assertArrayEquals(this.demand2, this.bidInfo2.getDemand());
        assertEquals(50.0d, this.bidInfo3.getDemand()[0], 0.0d);
        assertEquals(50.0d, this.bidInfo3.getDemand()[4], 0.0d);
        assertEquals(50.0d, this.bidInfo4.getDemand()[0], 0.0d);
        assertEquals(0.0d, this.bidInfo4.getDemand()[1], 0.0d);
        assertEquals(0.0d, this.bidInfo4.getDemand()[4], 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testGetPricePoints() {
        PricePoint[] pricePoints;
        pricePoints = this.bidInfo0.getCalculatedPricePoints();
        assertEquals(1, pricePoints.length);

        pricePoints = this.bidInfo1.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        this.bidInfo1 = new Bid(this.bidInfo1.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand1, this.bidInfo1.getDemand());

        pricePoints = this.bidInfo2.getCalculatedPricePoints();
        assertEquals(1, pricePoints.length);
        this.bidInfo2 = new Bid(this.bidInfo2.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand2, this.bidInfo2.getDemand());

        pricePoints = this.bidInfo3.getCalculatedPricePoints();
        assertEquals(1, pricePoints.length);

        pricePoints = this.bidInfo4.getCalculatedPricePoints();
        assertEquals(2, pricePoints.length);

        pricePoints = this.bidInfo5.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        this.bidInfo5 = new Bid(this.bidInfo5.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand5, this.bidInfo5.getDemand());

        pricePoints = this.bidInfo6.getCalculatedPricePoints();
        assertEquals(3, pricePoints.length);
        this.bidInfo6 = new Bid(this.bidInfo6.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand6, this.bidInfo6.getDemand());

        pricePoints = this.bidInfo7.getCalculatedPricePoints();
        assertEquals(2, pricePoints.length);
        this.bidInfo7 = new Bid(this.bidInfo7.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand7, this.bidInfo7.getDemand());

        pricePoints = this.bidInfo8.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        this.bidInfo8 = new Bid(this.bidInfo8.getMarketBasis(), pricePoints);
        assertArrayEquals(this.demand8, this.bidInfo8.getDemand());
    }

    /**
	 * 
	 */
    @Test
    public void testToMarketBasis() {
        Bid bidInfo;
        PricePoint[] pricePoints;

        bidInfo = this.bidInfo0.toMarketBasis(this.marketBasis0);
        assertSame(this.bidInfo0, bidInfo);

        bidInfo = this.bidInfo1.toMarketBasis(this.marketBasis1);
        assertNotSame(this.bidInfo1, bidInfo);
        pricePoints = bidInfo.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        assertEquals(100.0d, bidInfo.getDemand()[0], 0.0d);
        assertEquals(100.0d, bidInfo.getDemand()[1], 0.0d);
        assertEquals(50.0d, bidInfo.getDemand()[2], 0.0d);
        assertEquals(50.0d, bidInfo.getDemand()[3], 0.0d);
        assertEquals(50.0d, bidInfo.getDemand()[4], 0.0d);
        assertEquals(50.0d, bidInfo.getDemand()[5], 0.0d);
        assertEquals(0.0d, bidInfo.getDemand()[6], 0.0d);

        bidInfo = bidInfo.toMarketBasis(this.marketBasis0);
        pricePoints = bidInfo.getCalculatedPricePoints();
        assertEquals(4, pricePoints.length);
        assertEquals(100.0d, bidInfo.getDemand()[0], 0.0d);
        assertEquals(50.0d, bidInfo.getDemand()[1], 0.0d);
        assertEquals(50.0d, bidInfo.getDemand()[2], 0.0d);
        assertEquals(0.0d, bidInfo.getDemand()[3], 0.0d);
    }

    /**
	 * 
	 */
    @Test
    public void testEffectiveDemand() {
        assertEquals(-100.0d, this.bidInfo10.getDemand(20.0), 0.0d);
    }

}
