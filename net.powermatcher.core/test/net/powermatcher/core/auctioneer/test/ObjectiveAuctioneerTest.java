package net.powermatcher.core.auctioneer.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.auctioneer.ObjectiveAuctioneer;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;
import net.powermatcher.mock.MockObjectiveAgent;
import net.powermatcher.test.helpers.PropertieBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for the {@link ObjectiveAuctioneer} class.
 *
 * @author FAN
 * @version 2.0
 */
public class ObjectiveAuctioneerTest {
    // This needs to be the same as the MarketBasis created in the Auctioneer
    private static final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    private static final String OBJECTIVE_AUCTIONEER_NAME = "objectiveauctioneer";
    private static final String OBJECTIVE_AGENT_NAME = "objectiveagent";

    private final ObjectiveAuctioneer objectiveauctioneer = new ObjectiveAuctioneer();
    private final MockObjectiveAgent mockObjectiveAgent = new MockObjectiveAgent(OBJECTIVE_AGENT_NAME);

    private TestClusterHelper cluster;
    private BidCache aggregatedBids;

    @Before
    public void setUp() throws Exception {
        objectiveauctioneer.activate(new PropertieBuilder().agentId(OBJECTIVE_AUCTIONEER_NAME)
                                                           .clusterId("testCluster")
                                                           .marketBasis(marketBasis)
                                                           .minTimeBetweenPriceUpdates(1000)
                                                           .build());
        objectiveauctioneer.addObjectiveEndpoint(mockObjectiveAgent);
        mockObjectiveAgent.setObjectiveBid(new ArrayBid.Builder(marketBasis).demand(100)
                                                                            .demand(50)
                                                                            .demand(50)
                                                                            .demand(0)
                                                                            .build());
        cluster = new TestClusterHelper(marketBasis, objectiveauctioneer);
    }

    @Test
    public void noEquilibriumOnDemandSide() {
        // run 1
        cluster.sendBid(0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        cluster.sendBid(1, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4);
        cluster.sendBid(2, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3);
        cluster.performTasks();
        assertEquals(10.0, getPrice(), 0);

        // run 2
        cluster.sendBid(0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        cluster.sendBid(1, 0, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2);
        cluster.sendBid(2, 0, 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1);
        cluster.performTasks();
        assertEquals(10.0, getPrice(), 0);
    }

    private double getPrice() {
        return cluster.getPriceUpdates().get(0).getPrice().toPriceStep().toPrice().getPriceValue();
    }

    @Test
    public void objectiveAgentTest() {
        cluster.sendBid(0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        cluster.sendBid(1, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4);
        cluster.sendBid(2, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3);
        cluster.performTasks();

        objectiveauctioneer.addObjectiveEndpoint(mockObjectiveAgent);

        aggregatedBids = new BidCache(marketBasis);

        Bid aggregatedBid = aggregatedBids.aggregate();

        Bid finalAggregatedBid = null;
        if (mockObjectiveAgent != null) {
            Bid aggregatedObjectiveBid = mockObjectiveAgent.handleAggregateBid(aggregatedBid);

            finalAggregatedBid = new AggregatedBid.Builder(marketBasis).addBid(aggregatedBid)
                                                                       .addBid(aggregatedObjectiveBid)
                                                                       .build();

            // aggregate again with device agent bid.
            determinePrice(finalAggregatedBid);
        }

        assertArrayEquals(new double[] { 100.0, 50.0, 50.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
                          ((ArrayBid) finalAggregatedBid).getDemand(), 0);
    }

    protected Price determinePrice(Bid aggregatedBid) {
        return aggregatedBid.calculateIntersection(0);
    }

    @Test
    public void noEquilibriumOnSupplySide() {
        // run 1
        cluster.sendBid(0, 0, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5);
        cluster.sendBid(1, 0, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4);
        cluster.sendBid(2, 0, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3);
        cluster.performTasks();
        assertEquals(3.0, getPrice(), 0);

        // run 2
        cluster.sendBid(0, 0, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5);
        cluster.sendBid(1, 0, -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4);
        cluster.sendBid(2, 0, -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3);
        cluster.performTasks();
        assertEquals(3.0, getPrice(), 0);
    }

    @Test
    public void equilibriumSmallNumberOfBids() {
        // run 1
        cluster.sendBid(0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        cluster.sendBid(1, 0, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0);
        cluster.sendBid(2, 0, 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5);
        cluster.performTasks();
        assertEquals(8, getPrice(), 0);

        // run 2
        cluster.sendBid(0, 0, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5);
        cluster.sendBid(1, 0, 0, 0, 0, 0, 0, 0, 0, -4, -4, -4, -4);
        cluster.sendBid(2, 0, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9);
        cluster.performTasks();
        assertEquals(9, getPrice(), 0);
    }

    @Test
    public void equilibriumLargerSet() {
        cluster.sendBid(0, 0, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5);
        cluster.sendBid(1, 0, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4);
        cluster.sendBid(2, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3);
        cluster.sendBid(3, 0, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2);
        cluster.sendBid(4, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
        cluster.sendBid(5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        cluster.sendBid(6, 0, 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0);
        cluster.sendBid(7, 0, 0, 0, 0, 0, 0, 0, -4, -4, -4, -4, -4);
        cluster.sendBid(8, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0);
        cluster.sendBid(9, 0, 0, 0, 0, -2, -2, -2, -2, -2, -2, -2, -2);
        cluster.sendBid(10, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0);
        cluster.sendBid(11, 0, 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0);
        cluster.sendBid(12, 0, 0, 0, 0, -6, -6, -6, -6, -6, -6, -6, -6);
        cluster.sendBid(13, 0, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8);
        cluster.sendBid(14, 0, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9);
        cluster.sendBid(15, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8, -8, -8);
        cluster.sendBid(16, 0, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3);
        cluster.sendBid(17, 0, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0);
        cluster.sendBid(18, 0, -1, -1, -1, -1, -2, -2, -2, -2, -3, -3, -3);
        cluster.sendBid(19, 0, 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0);
        cluster.sendBid(20, 0, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8);
        cluster.performTasks();
        assertEquals(7, getPrice(), 0);
    }

    @After
    public void deactivateTest() {
        objectiveauctioneer.removeObjectiveEndpoint(mockObjectiveAgent);
    }
}
