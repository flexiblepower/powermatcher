package net.powermatcher.integration.concentrator;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
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

    private void removeAgents(int number) {
        for (int i = 0; i < number; i++) {
            sessions[i].disconnect();
        }
    }

    @Test
    public void sendAggregatedBidExtreme() {
        addAgents(3);
        int counter = 0;
        // Run 1
        agents[0].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 }));
        agents[1].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4 }));
        agents[2].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3 }));
        timer.doTaskOnce();
        assertArrayEquals(new double[] { -8, -8, -8, -8, -8, -10, -10, -12, -12, -12, -12 },
                          ((ArrayBid) matcher.getLastReceivedBid()).getDemand(), 0);
        // Run 2
        agents[0].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2 }));
        agents[2].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1 }));
        timer.doTaskOnce();
        assertArrayEquals(new double[] { 12, 12, 12, 12, 12, 8, 8, 8, 8, 8, 8 },
                          ((ArrayBid) matcher.getLastReceivedBid()).getDemand(), 0);
        // Run 3
        agents[0].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 }));
        agents[2].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 }));
        timer.doTaskOnce();
        assertArrayEquals(new double[] { 9, 9, 9, 9, 9, 0, 0, 0, 0, 0, 0 },
                          ((ArrayBid) matcher.getLastReceivedBid()).getDemand(), 0);
        removeAgents(3);
    }

    @Test
    public void sendAggregatedBidRejectAscending() {
        addAgents(4);
        int counter = 0;
        agents[0].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 }));
        agents[2].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 }));
        timer.doTaskOnce();

        exception.expect(IllegalArgumentException.class);
        agents[3].sendBid(new ArrayBid(marketBasis, counter++, new double[] { 5, 5, 5, 5, 5, 8, 8, 8, 8, 8, 8 }));

        removeAgents(4);
    }

    @Test
    public void sendAggregatedBidLarge() {
        int counter = 0;

        addAgents(20);
        agents[0].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4 }));
        agents[2].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 }));
        agents[3].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2 }));
        agents[4].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }));
        agents[5].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }));
        agents[6].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0 }));
        agents[7].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 0, 0, 0, 0, 0, 0, -4, -4, -4, -4, -4 }));
        agents[8].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0 }));
        agents[9].sendBid(new ArrayBid(marketBasis, counter++,
                                       new double[] { 0, 0, 0, -2, -2, -2, -2, -2, -2, -2, -2 }));
        agents[10].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 }));
        agents[11].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0 }));
        agents[12].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 0, 0, 0, -6, -6, -6, -6, -6, -6, -6, -6 }));
        agents[13].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 }));
        agents[14].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9 }));
        agents[15].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 0, 0, 0, 0, 0, 0, 0, 0, -8, -8, -8 }));
        agents[16].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3 }));
        agents[17].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0 }));
        agents[18].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { -1, -1, -1, -1, -2, -2, -2, -2, -3, -3, -3 }));
        agents[19].sendBid(new ArrayBid(marketBasis, counter++,
                                        new double[] { 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0 }));
        timer.doTaskOnce();
        assertArrayEquals(new double[] { 29, 29, 29, 21, 16, 11, 0, -8, -18, -18, -18 },
                          ((ArrayBid) matcher.getLastReceivedBid()).getDemand(), 0);
        removeAgents(20);
    }
}
