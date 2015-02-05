package net.powermatcher.integration.concentrator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.MockScheduler;
import net.powermatcher.mock.MockTimeService;
import net.powermatcher.mock.SimpleSession;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for the Concentrator
 *
 * Every test requires a different number agents. In setUp() NR_AGENTS are instantiated. Every test the desired number
 * of agents can be added and removed using the functions addAgents() and removeAgents().
 *
 * @author FAN
 * @version 2.0
 */
public class ConcentratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final static int NR_AGENTS = 21;
    private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    private MockScheduler timer;

    private Concentrator concentrator;
    private Map<String, Object> concentratorProperties;

    private MockMatcherAgent matcher;
    private MockAgent[] agents;
    private SimpleSession[] sessions;

    private static final String AUCTIONEER_NAME = "auctioneer";

    private static final String CONCENTRATOR_NAME = "concentrator";

    @Before
    public void setUp() throws Exception {
        // Concentrator to be tested
        concentrator = new Concentrator();
        concentratorProperties = new HashMap<String, Object>();
        concentratorProperties.put("matcherId", CONCENTRATOR_NAME);
        concentratorProperties.put("desiredParentId", AUCTIONEER_NAME);
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", CONCENTRATOR_NAME);
        concentratorProperties.put("whiteListAgents", new ArrayList<String>());

        timer = new MockScheduler();
        concentrator.activate(concentratorProperties);
        concentrator.setExecutorService(timer);
        concentrator.setTimeService(new MockTimeService(0));

        // Matcher
        matcher = new MockMatcherAgent(AUCTIONEER_NAME, "testCluster");
        matcher.setMarketBasis(marketBasis);

        new SimpleSession(concentrator, matcher).connect();

        // Init MockAgents
        agents = new MockAgent[NR_AGENTS];
        sessions = new SimpleSession[NR_AGENTS];
        for (int i = 0; i < NR_AGENTS; i++) {
            String agentId = "agent" + (i + 1);
            MockAgent newAgent = new MockAgent(agentId);
            newAgent.setDesiredParentId(CONCENTRATOR_NAME);
            agents[i] = newAgent;
            sessions[i] = new SimpleSession(newAgent, concentrator);
        }
    }

    @After
    public void tearDown() throws Exception {
        for (SimpleSession session : sessions) {
            session.disconnect();
        }
    }

    private void addAgents(int number) {
        for (int i = 0; i < number; i++) {
            sessions[i].connect();
        }
    }

    private void sendBid(int agentIx, int bidNr, double... demandArray) {
        agents[agentIx].sendBid(new ArrayBid(marketBasis, bidNr, demandArray));
    }

    private void sendBids(int baseId, double[]... demandArrays) {
        for (int ix = 0; ix < demandArrays.length; ix++) {
            sendBid(ix, baseId + ix, demandArrays[ix]);
        }
        timer.doTaskOnce();
    }

    private void testPriceSignal(int... expectedIds) {
        Price price = new Price(marketBasis, Math.random() * marketBasis.getMaximumPrice());
        matcher.publishPrice(new PriceUpdate(price, matcher.getLastReceivedBid().getBidNumber()));
        for (int i = 0; i < expectedIds.length; i++) {
            if (expectedIds[i] < 0) {
                assertNull(agents[i].getLastPriceUpdate());
            } else {
                assertEquals(price, agents[i].getLastPriceUpdate().getPrice());
                assertEquals(expectedIds[i], agents[i].getLastPriceUpdate().getBidNumber());
            }
        }
    }

    private void assertTotalBid(double... demandArray) {
        assertArrayEquals(demandArray, ((ArrayBid) matcher.getLastReceivedBid()).getDemand(), 0);
    }

    @Test
    public void sendAggregatedBidExtreme() {
        addAgents(3);

        // Run 1
        sendBids(0,
                 new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 },
                 new double[] { -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4 },
                 new double[] { -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3 });
        assertTotalBid(-8, -8, -8, -8, -8, -10, -10, -12, -12, -12, -12);
        testPriceSignal(0, 1, 2);

        // Run 2
        sendBids(10,
                 new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                 new double[] { 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2 },
                 new double[] { 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1 });
        assertTotalBid(12, 12, 12, 12, 12, 8, 8, 8, 8, 8, 8);
        testPriceSignal(10, 11, 12);

        // Run 3
        sendBids(20,
                 new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                 new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 },
                 new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 });
        assertTotalBid(9, 9, 9, 9, 9, 0, 0, 0, 0, 0, 0);
        testPriceSignal(20, 21, 22);
    }

    @Test
    public void sendAggregatedBidRejectAscending() {
        addAgents(4);

        sendBids(0,
                 new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 },
                 new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 },
                 new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 });
        assertTotalBid(9, 9, 9, 9, 9, 0, 0, 0, 0, 0, 0);
        testPriceSignal(0, 1, 2, -1);

        exception.expect(IllegalArgumentException.class);
        sendBid(3, 3, new double[] { 5, 5, 5, 5, 5, 8, 8, 8, 8, 8, 8 });

        testPriceSignal(0, 1, 2, -1);
    }

    @Test
    public void sendAggregatedBidLarge() {
        addAgents(20);
        sendBids(0,
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

        assertTotalBid(29, 29, 29, 21, 16, 11, 0, -8, -18, -18, -18);
        testPriceSignal(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
    }
}
