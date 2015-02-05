package net.powermatcher.core.concentrator.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.concentrator.PeakShavingConcentrator;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.MockScheduler;
import net.powermatcher.mock.SimpleSession;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for the {@link PeakShavingConcentrator} class.
 *
 * @author FAN
 * @version 2.0
 */
public class PeakShavingConcentratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String CLUSTER_ID = "testCluster";
    private static final String AUCTIONEER_ID = "auctioneer";
    private static final String CONCENTRATOR_NAME = "peakshavingconcentrator";
    private static final String DEVICE_AGENT_ID = "deviceAgent";

    private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);

    private final MockScheduler scheduler = new MockScheduler();
    private final MockContext context = new MockContext(0);

    private MockMatcherAgent matcher;
    private PeakShavingConcentrator peakShavingConcentrator;
    private MockAgent deviceAgent;

    public void setUp(double floor, double ceiling) throws Exception {
        // Concentrator to be tested
        peakShavingConcentrator = new PeakShavingConcentrator();
        Map<String, Object> concentratorProperties = new HashMap<String, Object>();
        concentratorProperties.put("matcherId", CONCENTRATOR_NAME);
        concentratorProperties.put("desiredParentId", AUCTIONEER_ID);
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", CONCENTRATOR_NAME);

        concentratorProperties.put("floor", floor);
        concentratorProperties.put("ceiling", ceiling);

        peakShavingConcentrator.activate(concentratorProperties);
        peakShavingConcentrator.setContext(context);

        // Matcher
        matcher = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
        matcher.setMarketBasis(marketBasis);
        matcher.setContext(context);

        new SimpleSession(peakShavingConcentrator, matcher).connect();

        // Init MockAgent
        deviceAgent = new MockAgent(DEVICE_AGENT_ID);
        deviceAgent.setContext(context);
        new SimpleSession(deviceAgent, peakShavingConcentrator).connect();
    }

    @Test
    public void testUpdatePriceNoTransformation() throws Exception {
        setUp(-10, 10);

        Bid bid = new ArrayBid(marketBasis, 0, new double[] { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 });
        deviceAgent.sendBid(bid);
        scheduler.doTaskOnce();
        int bidNumber = matcher.getLastReceivedBid().getBidNumber();
        PriceUpdate expected = new PriceUpdate(new Price(marketBasis, 0.0), bidNumber);
        matcher.publishPrice(expected);
        assertThat(deviceAgent.getLastPriceUpdate().getPrice(), is(equalTo(expected.getPrice())));
    }

    @Test
    public void testUpdatePriceWithTransformation() throws Exception {
        setUp(-1, 1);

        Bid bid = new ArrayBid(marketBasis, 0, new double[] { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 });
        deviceAgent.sendBid(bid);
        scheduler.doTaskOnce();
        int bidNumber = matcher.getLastReceivedBid().getBidNumber();
        PriceUpdate sentPrice = new PriceUpdate(new Price(marketBasis, 10.0), bidNumber);
        PriceUpdate expectedPrice = new PriceUpdate(new Price(marketBasis, 3.0), bidNumber);
        matcher.publishPrice(sentPrice);
        assertThat(deviceAgent.getLastPriceUpdate().getPrice(), is(equalTo(expectedPrice.getPrice())));
    }

    @Test
    public void testUpdateBidWithTransformation() throws Exception {
        setUp(-1, 1);

        double[] demandArray = new double[] { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        deviceAgent.sendBid(arrayBid);
        scheduler.doTaskOnce();
        double[] transformedDemandArray = new double[] { 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1 };
        Bid expectedBid = new ArrayBid(marketBasis, 0, transformedDemandArray);
        assertThat(matcher.getLastReceivedBid(), is(equalTo(expectedBid)));
    }

    @Test
    public void testUpdateBidNoTransformation() throws Exception {
        setUp(-1, 1);

        double[] demandArray = new double[] { 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        deviceAgent.sendBid(arrayBid);
        scheduler.doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 0);
        assertThat(matcher.getLastReceivedBid(), is(equalTo(expectedBid)));
    }
}
