package net.powermatcher.peakshaving.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.SimpleSession;
import net.powermatcher.peakshaving.PeakShavingConcentrator;
import net.powermatcher.test.helpers.PropertieBuilder;

import org.junit.Test;

/**
 * JUnit test for the {@link PeakShavingConcentrator} class.
 *
 * @author FAN
 * @version 2.0
 */
public class PeakShavingConcentratorTest {
    private static final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    private static final String CLUSTER_ID = "testCluster";
    private static final String AUCTIONEER_ID = "auctioneer";
    private static final String CONCENTRATOR_NAME = "peakshavingconcentrator";
    private static final String DEVICE_AGENT_ID = "deviceAgent";

    private final MockContext context = new MockContext(0);

    private final MockMatcherAgent matcher = new MockMatcherAgent(AUCTIONEER_ID, CLUSTER_ID);
    private final PeakShavingConcentrator peakShavingConcentrator = new PeakShavingConcentrator();
    private final MockAgent deviceAgent = new MockAgent(DEVICE_AGENT_ID);

    public void setUp(double floor, double ceiling) throws Exception {
        peakShavingConcentrator.activate(new PropertieBuilder().agentId(CONCENTRATOR_NAME)
                                                               .desiredParentId(AUCTIONEER_ID)
                                                               .bidUpdateRate(600)
                                                               .add("floor", floor)
                                                               .add("ceiling", ceiling)
                                                               .build());
        peakShavingConcentrator.setContext(context);

        // Set the market basis for the matcher
        matcher.setMarketBasis(marketBasis);

        // Connect the 3 parts to each other
        new SimpleSession(peakShavingConcentrator, matcher).connect();
        new SimpleSession(deviceAgent, peakShavingConcentrator).connect();
    }

    @Test
    public void testUpdatePriceNoTransformation() throws Exception {
        setUp(-10, 10);

        Bid bid = new ArrayBid(marketBasis, 0, new double[] { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 });
        deviceAgent.sendBid(bid);
        context.getMockScheduler().doTaskOnce();
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
        context.getMockScheduler().doTaskOnce();
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
        Bid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        deviceAgent.sendBid(arrayBid);
        context.getMockScheduler().doTaskOnce();
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
        context.getMockScheduler().doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 0);
        assertThat(matcher.getLastReceivedBid(), is(equalTo(expectedBid)));
    }
}
