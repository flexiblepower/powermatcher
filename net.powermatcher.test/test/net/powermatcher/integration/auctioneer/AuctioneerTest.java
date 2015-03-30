package net.powermatcher.integration.auctioneer;

import static org.junit.Assert.assertEquals;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.test.helpers.PropertiesBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for the Auctioneer
 *
 * Every test requires a different number agents. In setUp() NR_AGENTS are instantiated. Every test the desired number
 * of agents can be added and removed using the functions addAgents() and removeAgents().
 *
 * @author FAN
 * @version 2.0
 */
public class AuctioneerTest {
    private static final String AUCTIONEER_NAME = "auctioneer";

    private TestClusterHelper cluster;
    private Auctioneer auctioneer;

    @Before
    public void setUp() {
        // Init Auctioneer
        auctioneer = new Auctioneer();
        auctioneer.activate(new PropertiesBuilder().agentId(AUCTIONEER_NAME)
                                                  .clusterId("testCluster")
                                                  .minTimeBetweenPriceUpdates(1000)
                                                  .marketBasis(TestClusterHelper.DEFAULT_MB)
                                                  .build());
        cluster = new TestClusterHelper(auctioneer);
    }

    @After
    public void tearDown() {
        cluster.close();
    }

    @Test
    public void noEquilibriumOnDemandSide() {
        // run 1
        cluster.sendBids(0,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4 },
                         new double[] { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 });
        assertAllPricesAre(10);

        // run 2
        cluster.sendBids(10,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2 },
                         new double[] { 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1 });
        assertAllPricesAre(10);
    }

    private void assertAllPricesAre(double price) {
        double[] priceValues = cluster.getPriceValues();
        for (int ix = 0; ix < priceValues.length; ix++) {
            assertEquals("Price for agent " + ix + " not correct" + priceValues[ix],
                         price, priceValues[ix], 0);
        }
    }

    @Test
    public void noEquilibriumOnSupplySide() {
        // run 1
        cluster.sendBids(0,
                         new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 },
                         new double[] { -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4 },
                         new double[] { -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3 });
        assertAllPricesAre(0);

        // run 2
        cluster.sendBids(10,
                         new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 },
                         new double[] { -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4 },
                         new double[] { -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3 });
        assertAllPricesAre(0);
    }

    @Test
    public void equilibriumSmallNumberOfArrayBids() {
        // run 1
        cluster.sendBids(0,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 });
        assertAllPricesAre(7.5);

        // run 2
        cluster.sendBids(10,
                         new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 },
                         new double[] { 0, 0, 0, 0, 0, 0, 0, -4, -4, -4, -4 },
                         new double[] { 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9 });
        assertAllPricesAre(8.5);
    }

    @Test
    public void equilibriumLargeSet() {
        cluster.sendBids(0,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4 },
                         new double[] { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 },
                         new double[] { -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2 },
                         new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                         new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                         new double[] { 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, 0, 0, 0, -4, -4, -4, -4, -4 },
                         new double[] { 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, -2, -2, -2, -2, -2, -2, -2, -2 },
                         new double[] { 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
                         new double[] { 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, -6, -6, -6, -6, -6, -6, -6, -6 },
                         new double[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 },
                         new double[] { -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9 },
                         new double[] { 0, 0, 0, 0, 0, 0, 0, 0, -8, -8, -8 },
                         new double[] { 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3 },
                         new double[] { 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0 },
                         new double[] { -1, -1, -1, -1, -2, -2, -2, -2, -3, -3, -3 },
                         new double[] { 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0 });
        assertAllPricesAre(6);
    }

    @Test
    public void equilibriumLargerSet() {
        cluster.addAgents(21);
        cluster.sendBids(0,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4 },
                         new double[] { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 },
                         new double[] { -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2 },
                         new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                         new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
                         new double[] { 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, 0, 0, 0, -4, -4, -4, -4, -4 },
                         new double[] { 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, -2, -2, -2, -2, -2, -2, -2, -2 },
                         new double[] { 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
                         new double[] { 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, -6, -6, -6, -6, -6, -6, -6, -6 },
                         new double[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 },
                         new double[] { -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9 },
                         new double[] { 0, 0, 0, 0, 0, 0, 0, 0, -8, -8, -8 },
                         new double[] { 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3 },
                         new double[] { 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0 },
                         new double[] { -1, -1, -1, -1, -2, -2, -2, -2, -3, -3, -3 },
                         new double[] { 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0 },
                         new double[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 });
        assertAllPricesAre(7);
    }
}
