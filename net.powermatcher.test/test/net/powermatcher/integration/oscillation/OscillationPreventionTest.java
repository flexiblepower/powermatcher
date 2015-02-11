package net.powermatcher.integration.oscillation;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.mock.MockContext;
import net.powermatcher.test.helpers.PropertieBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.junit.Before;
import org.junit.Test;

/**
 * This test is created to assert that features added to prevent oscillating behavior are in place and work. Oscillating
 * behavior can emerge when an Agent sends Bids asynchronously from the prices of the Auctioneer. If the Agent cannot
 * ascertain which Price is is corresponding to which Bid, it could start sending Bids in response to the wrong Prices.
 *
 * @author FAN
 * @version 2.0
 */
public class OscillationPreventionTest {
    private static final String AUCTIONEER_ID = "auctioneer";
    private static final String CONCENTRATOR_ID = "concentrator";

    // This needs to be the same as the MarketBasis created in the Auctioneer
    private TestClusterHelper cluster;
    private Auctioneer auctioneer;
    private MockContext autioneerContext;
    private Concentrator concentrator;

    @Before
    public void setUpCluster() {
        // Create auctioneer
        auctioneer = new Auctioneer();
        auctioneer.activate(new PropertieBuilder().agentId(AUCTIONEER_ID)
                                                  .clusterId("testCluster")
                                                  .marketBasis(TestClusterHelper.DEFAULT_MB)
                                                  .bidUpdateRate(600)
                                                  .priceUpdateRate(600)
                                                  .build());
        auctioneer.setContext(autioneerContext = new MockContext(0));

        // create concentrator
        concentrator = new Concentrator();
        concentrator.activate(new PropertieBuilder().agentId(CONCENTRATOR_ID)
                                                    .desiredParentId(AUCTIONEER_ID)
                                                    .bidUpdateRate(600)
                                                    .build());

        cluster = new TestClusterHelper(concentrator);

        cluster.connect(concentrator, auctioneer);
        cluster.addAgents(3);
    }

    /**
     * This uses the cluster in a optimal way. The Agent sends a bid, the concentrator aggregates it and sends it to the
     * auctioneer, which generates a price and sends this back. Agent should ascertain which bid the price belongs to.
     */
    @Test
    public void synchrousUpdateTestSimple() {
        // create and send bid by agent
        int bidNumber = 1;
        cluster.sendBid(0, bidNumber, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5);

        act();

        assertThat(cluster.getAgent(0).getLastPriceUpdate().getBidNumber(), is(equalTo(bidNumber)));
    }

    /**
     * This uses the cluster in a optimal way. The Agent sends a bid, the concentrator aggregates it and sends it to the
     * auctioneer, which generates a price and sends this back. Agent should ascertain which bid the price belongs to.
     */
    @Test
    public void synchrousUpdateTestMultipleAgents() {
        // create and send bid by agent
        int bidNumber1 = 1;
        int bidNumber2 = 2;
        int bidNumber3 = 3;
        cluster.sendBid(0, bidNumber1, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5);
        cluster.sendBid(1, bidNumber2, 7, 5, 3, 1, -1, -3, -5, -7, -9, -11, -13);
        cluster.sendBid(2, bidNumber3, 5, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3);

        act();

        List<PriceUpdate> priceUpdates = cluster.getPriceUpdates();
        assertThat(priceUpdates.get(0).getBidNumber(), is(equalTo(bidNumber1)));
        assertThat(priceUpdates.get(1).getBidNumber(), is(equalTo(bidNumber2)));
        assertThat(priceUpdates.get(2).getBidNumber(), is(equalTo(bidNumber3)));
    }

    /**
     * This tries to create oscillating behavior. Have the agents, concentrator and auctioneer sends bids and prices in
     * a different order and see if the agent still receives the right price.
     */
    @Test
    public void asynchrousUpdateTest() {
        // create bids
        int bidNumber1 = 1;
        int bidNumber2 = 2;
        int bidNumber3 = 3;

        cluster.sendBid(0, bidNumber1, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5);
        cluster.sendBid(1, bidNumber2, 7, 5, 3, 1, -1, -3, -5, -7, -9, -11, -12);
        concentratorRun();

        cluster.sendBid(2, bidNumber3, 5, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3);
        auctioneerRun();

        List<PriceUpdate> priceUpdates = cluster.getPriceUpdates();

        // agents receive price
        PriceUpdate savedPriceUpdate0 = priceUpdates.get(0);
        assertThat(savedPriceUpdate0.getPrice(), is(equalTo(priceUpdates.get(1).getPrice())));
        assertThat(savedPriceUpdate0.getBidNumber(), is(equalTo(bidNumber1)));
        assertThat(priceUpdates.get(1).getBidNumber(), is(equalTo(bidNumber2)));
        assertNull(priceUpdates.get(2));

        act();

        priceUpdates = cluster.getPriceUpdates();
        // agent3 bid has effect on price
        assertThat(priceUpdates.get(0).getPrice().getPriceValue(),
                   is(not(equalTo(savedPriceUpdate0.getPrice().getPriceValue()))));
        assertThat(priceUpdates.get(0).getBidNumber(), is(equalTo(bidNumber1)));
        assertThat(priceUpdates.get(1).getBidNumber(), is(equalTo(bidNumber2)));
        PriceUpdate savedPriceUpdate2 = priceUpdates.get(2);
        assertThat(savedPriceUpdate2.getBidNumber(), is(equalTo(bidNumber3)));

        cluster.sendBid(0, ++bidNumber1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        cluster.sendBid(1, ++bidNumber2, 3, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0);

        savedPriceUpdate0 = cluster.getAgent(0).getLastPriceUpdate();
        act();

        priceUpdates = cluster.getPriceUpdates();
        // agents have received new price
        assertThat(priceUpdates.get(0).getBidNumber(), is(equalTo(bidNumber1)));
        assertThat(priceUpdates.get(0).getPrice().getPriceValue(),
                   is(not(equalTo(savedPriceUpdate0.getPrice().getPriceValue()))));
        assertThat(priceUpdates.get(1).getBidNumber(), is(equalTo(bidNumber2)));

        // check agent3 included in new Price
        assertThat(priceUpdates.get(2).getBidNumber(), is(equalTo(bidNumber3)));
        assertThat(priceUpdates.get(2).getPrice().getPriceValue(),
                   is(not(equalTo(savedPriceUpdate2.getPrice().getPriceValue()))));
        assertThat(priceUpdates.get(2).getPrice().getPriceValue(),
                   is(equalTo(priceUpdates.get(0).getPrice().getPriceValue())));
    }

    /*
     * Wrapper that performs all the cluster and auctioneer tasks
     */
    private void act() {
        concentratorRun();
        auctioneerRun();
    }

    private void auctioneerRun() {
        autioneerContext.getMockScheduler().doTaskOnce();
    }

    private void concentratorRun() {
        cluster.performTasks();
    }
}
