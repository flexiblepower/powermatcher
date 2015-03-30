package net.powermatcher.integration.concentrator;

import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.test.helpers.PropertiesBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for the Concentrator
 *
 * Every test requires a different number agents.
 *
 * @author FAN
 * @version 2.0
 */
public class ConcentratorTest {
    private static final String AUCTIONEER_NAME = "auctioneer";
    private static final String CONCENTRATOR_NAME = "concentrator";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private TestClusterHelper cluster;
    private Concentrator concentrator;
    private MockMatcherAgent matcher;

    @Before
    public void setUp() throws Exception {
        // Concentrator to be tested
        concentrator = new Concentrator();
        concentrator.activate(new PropertiesBuilder().agentId(CONCENTRATOR_NAME)
                                                    .desiredParentId(AUCTIONEER_NAME)
                                                    .minTimeBetweenBidUpdates(1000)
                                                    .build());

        cluster = new TestClusterHelper(concentrator);

        // Matcher
        matcher = new MockMatcherAgent(AUCTIONEER_NAME, "testCluster");
        matcher.setMarketBasis(TestClusterHelper.DEFAULT_MB);

        cluster.connect(concentrator, matcher);
    }

    @After
    public void tearDown() throws Exception {
        cluster.close();
        concentrator.deactivate();
    }

    @Test
    public void sendAggregatedBidExtreme() {
        // Run 1
        cluster.sendBids(0,
                         new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 },
                         new double[] { -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4 },
                         new double[] { -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3 });
        matcher.assertTotalBid(-8, -8, -8, -8, -8, -10, -10, -12, -12, -12, -12);
        cluster.testPriceSignal(matcher, 0, 1, 2);

        // Run 2
        cluster.sendBids(10,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2 },
                         new double[] { 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1 });
        matcher.assertTotalBid(12, 12, 12, 12, 12, 8, 8, 8, 8, 8, 8);
        cluster.testPriceSignal(matcher, 10, 11, 12);

        // Run 3
        cluster.sendBids(20,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 });
        matcher.assertTotalBid(9, 9, 9, 9, 9, 0, 0, 0, 0, 0, 0);
        cluster.testPriceSignal(matcher, 20, 21, 22);
    }

    @Test
    public void sendAggregatedBidRejectAscending() {
        cluster.sendBids(0,
                         new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                         new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 },
                         new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 });
        matcher.assertTotalBid(9, 9, 9, 9, 9, 0, 0, 0, 0, 0, 0);
        cluster.testPriceSignal(matcher, 0, 1, 2, -1);

        exception.expect(IllegalArgumentException.class);
        cluster.sendBid(3, 3, new double[] { 5, 5, 5, 5, 5, 8, 8, 8, 8, 8, 8 });

        cluster.testPriceSignal(matcher, 0, 1, 2, -1);
    }

    @Test
    public void sendAggregatedBidLarge() {
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

        matcher.assertTotalBid(29, 29, 29, 21, 16, 11, 0, -8, -18, -18, -18);
        cluster.testPriceSignal(matcher, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    }
}
