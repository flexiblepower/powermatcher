//package net.powermatcher.core.test;
//
//import static org.hamcrest.core.Is.is;
//import static org.hamcrest.core.IsEqual.equalTo;
//import static org.junit.Assert.assertThat;
//import net.powermatcher.api.data.ArrayBid;
//import net.powermatcher.api.data.MarketBasis;
//import net.powermatcher.api.data.PointBid;
//import net.powermatcher.api.data.Price;
//import net.powermatcher.api.data.PricePoint;
//import net.powermatcher.core.BidCacheElement;
//import net.powermatcher.core.time.SystemTimeService;
//
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * JUnit tests for the {@link BidCacheElement} class.
// * 
// * @author FAN
// * @version 2.1
// */
//public class BidCacheElementTest {
//
//    private SystemTimeService timeService;
//    private MarketBasis marketBasis;
//    private ArrayBid arrayBid;
//    private PointBid pointBid;
//
//    private double[] demand;
//    private PricePoint[] pricePoints;
//
//    @Before
//    public void setUp() throws Exception {
//        marketBasis = new MarketBasis("electricity", "EUR", 5, 0, 10);
//        timeService = new SystemTimeService();
//
//        demand = new double[] { 4, 3, 2, 1, 0 };
//        pricePoints = new PricePoint[] { new PricePoint(new Price(marketBasis, 1), 30),
//                new PricePoint(new Price(marketBasis, 10), 20) };
//
//        arrayBid = new ArrayBid(marketBasis, 0, demand);
//        pointBid = new PointBid(marketBasis, 0, pricePoints);
//    }
//
//    @Test
//    public void testBidCacheElement() {
//        long stamp1 = timeService.currentTimeMillis();
//        BidCacheElement arrayBidCacheElement = new BidCacheElement(arrayBid, stamp1);
//        assertThat(arrayBidCacheElement.getBid(), (is(equalTo(arrayBid))));
//        assertThat(arrayBidCacheElement.getTimestamp(), is(equalTo(stamp1)));
//        long stamp2 = timeService.currentTimeMillis();
//        BidCacheElement pointBidCacheElement = new BidCacheElement(pointBid, stamp2);
//        double[] expectedDemand = new double[] { 30.0, 27.5, 25.0, 22.5, 20.0 };
//        assertThat(pointBidCacheElement.getBid().getDemand(), (is(equalTo(expectedDemand))));
//        assertThat(pointBidCacheElement.getTimestamp(), is(equalTo(stamp2)));
//    }
//
//    @Test
//    public void testSetBid() {
//        long stamp1 = timeService.currentTimeMillis();
//        BidCacheElement arrayBidCacheElement = new BidCacheElement(arrayBid, stamp1);
//        arrayBidCacheElement.setBid(pointBid);
//        double[] expectedDemand = new double[] { 30.0, 27.5, 25.0, 22.5, 20.0 };
//        assertThat(arrayBidCacheElement.getBid().getDemand(), (is(equalTo(expectedDemand))));
//        assertThat(arrayBidCacheElement.getTimestamp(), is(equalTo(stamp1)));
//    }
//
//    @Test
//    public void testSetTimestamp() {
//        long stamp1 = timeService.currentTimeMillis();
//        BidCacheElement arrayBidCacheElement = new BidCacheElement(arrayBid, stamp1);
//        long newStamp = 232;
//        arrayBidCacheElement.setTimestamp(newStamp);
//        assertThat(arrayBidCacheElement.getTimestamp(), is(equalTo(newStamp)));
//    }
//
//}
