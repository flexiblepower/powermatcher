package net.powermatcher.core.concentrator.test;

import static org.junit.Assert.assertArrayEquals;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit test for the Concentrator
 * 
 * Every test requires a different number agents. In setUp() NR_AGENTS are
 * instantiated. Every test the desired number of agents can be added and
 * removed using the functions addAgents() and removeAgents().
 */
public class ConcentratorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final static int NR_AGENTS = 21;
    private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    private SessionManager sessionManager;

    private Concentrator concentrator;
    private Map<String, Object> concentratorProperties;

    private MockMatcherAgent matcher;
    private MockAgent[] agents;

    @Before
    public void setUp() throws Exception {
        List<String> activeConnections = new ArrayList<>();
        // Concentrator to be tested
        concentrator = new Concentrator();
        concentratorProperties = new HashMap<>();
        concentratorProperties.put("matcherId", "concentrator");
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", "concentrator");

        concentrator.setExecutorService(new ScheduledThreadPoolExecutor(10));
        concentrator.setTimeService(new SystemTimeService());
        concentrator.activate(concentratorProperties);

        // Matcher
        matcher = new MockMatcherAgent("matcher");
        matcher.setMarketBasis(marketBasis);

        sessionManager = new SessionManager();
        sessionManager.addMatcherRole(matcher, matcher.getMatcherProperties());
        sessionManager.addMatcherRole(concentrator, concentratorProperties);
        sessionManager.addAgentRole(concentrator, concentratorProperties);
        activeConnections.add("concentrator::matcher");

        // Init MockAgents
        agents = new MockAgent[NR_AGENTS];
        for (int i = 0; i < NR_AGENTS; i++) {
            String agentId = "agent" + (i + 1);
            MockAgent newAgent = new MockAgent(agentId);
            agents[i] = newAgent;
            activeConnections.add(agentId + "::" + "concentrator");
        }

        Map<String, Object> sessionProperties = new HashMap<>();
        sessionProperties.put("activeConnections", activeConnections);
        sessionManager.activate(sessionProperties);

    }

    @After
    public void tearDown() throws Exception {
        sessionManager.removeAgentRole(concentrator, concentratorProperties);
        sessionManager.removeMatcherRole(matcher, matcher.getMatcherProperties());
    }

    private void addAgents(int number) {
        for (int i = 0; i < number; i++) {
            this.sessionManager.addAgentRole(agents[i], agents[i].getAgentProperties());
        }
    }

    private void removeAgents(int number) {
        for (int i = 0; i < number; i++) {
            this.sessionManager.removeAgentRole(agents[i], agents[i].getAgentProperties());
        }
    }

    @Test
    public void sendAggregatedBidExtreme() {
        addAgents(3);
        // Run 1
        agents[0].sendBid(new Bid(marketBasis, new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3 }));
        concentrator.doBidUpdate();
        assertArrayEquals(new double[] { -8, -8, -8, -8, -8, -10, -10, -12, -12, -12, -12 }, this.matcher
                .getLastReceivedBid().getDemand(), 0);
        // Run 2
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1 }));
        concentrator.doBidUpdate();
        assertArrayEquals(new double[] { 12, 12, 12, 12, 12, 8, 8, 8, 8, 8, 8 }, this.matcher.getLastReceivedBid()
                .getDemand(), 0);
        // Run 3
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 }));
        concentrator.doBidUpdate();
        assertArrayEquals(new double[] { 9, 9, 9, 9, 9, 0, 0, 0, 0, 0, 0 }, this.matcher.getLastReceivedBid()
                .getDemand(), 0);
        removeAgents(3);
    }

    @Test
    public void sendAggregatedBidRejectAscending() {
        addAgents(4);
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 }));
        concentrator.doBidUpdate();

        exception.expect(InvalidParameterException.class);
        agents[3].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 8, 8, 8, 8, 8, 8 }));

        removeAgents(4);
    }

    @Test
    public void sendAggregatedBidLarge() {
        addAgents(20);
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3 }));
        agents[3].sendBid(new Bid(marketBasis, new double[] { -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2 }));
        agents[4].sendBid(new Bid(marketBasis, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }));
        agents[5].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }));
        agents[6].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 0, 0, 0, 0, 0, 0 }));
        agents[7].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, 0, -4, -4, -4, -4, -4 }));
        agents[8].sendBid(new Bid(marketBasis, new double[] { 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0 }));
        agents[9].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, -2, -2, -2, -2, -2, -2, -2, -2 }));
        agents[10].sendBid(new Bid(marketBasis, new double[] { 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 }));
        agents[11].sendBid(new Bid(marketBasis, new double[] { 7, 7, 7, 7, 7, 7, 7, 0, 0, 0, 0 }));
        agents[12].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, -6, -6, -6, -6, -6, -6, -6, -6 }));
        agents[13].sendBid(new Bid(marketBasis, new double[] { 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8 }));
        agents[14].sendBid(new Bid(marketBasis, new double[] { -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9 }));
        agents[15].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, 0, 0, 0, -8, -8, -8 }));
        agents[16].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3 }));
        agents[17].sendBid(new Bid(marketBasis, new double[] { 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0 }));
        agents[18].sendBid(new Bid(marketBasis, new double[] { -1, -1, -1, -1, -2, -2, -2, -2, -3, -3, -3 }));
        agents[19].sendBid(new Bid(marketBasis, new double[] { 6, 6, 6, 6, 6, 6, 0, 0, 0, 0, 0 }));
        concentrator.doBidUpdate();
        assertArrayEquals(new double[] { 29, 29, 29, 21, 16, 11, 0, -8, -18, -18, -18 }, this.matcher
                .getLastReceivedBid().getDemand(), 0);
        removeAgents(20);
    }

    /*
     * TODO: The behavior tested in this test is still subject of discussion
     * 
     * @Test public void receivePriceAndSendToAgents() { bindAgents(1); // Run 1
     * int[] values1 = {0, 1, 5, 9, 10}; for(int value : values1) {
     * this.matcher.sendPrice(new PriceInfo(this.marketBasis, value));
     * assertEquals(value, agents[0].lastPriceUpdate.getNormalizedPrice(), 0); }
     * // Run 2 int[] values2 = {20, 11, 15, 40}; for(int value : values2) {
     * this.matcher.sendPrice(new PriceInfo(this.marketBasis, value));
     * assertEquals(10, agents[0].lastPriceUpdate.getNormalizedPrice(), 0); } //
     * Run 3 int[] values3 = {-20, -11, -15, -1}; for(int value : values3) {
     * this.matcher.sendPrice(new PriceInfo(this.marketBasis, value));
     * assertEquals(0, agents[0].lastPriceUpdate.getNormalizedPrice(), 0); }
     * unbindAgents(1); }
     */

    /*
     * TODO: The behavior tested in this test is outside the scope of this
     * version
     * 
     * @Test public void sendAggregatedBidLargeRejectAscending() {
     * bindAgents(21); agents[0].sendBid(new Bid(marketBasis, new double[]
     * {5,5,5,5,5,5,5,5,5,5,5})); agents[1].sendBid(new Bid(marketBasis, new
     * double[] {-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4})); agents[2].sendBid(new
     * Bid(marketBasis, new double[] {3,3,3,3,3,3,3,3,3,3,3}));
     * agents[3].sendBid(new Bid(marketBasis, new double[]
     * {-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2})); agents[4].sendBid(new
     * Bid(marketBasis, new double[] {1,1,1,1,1,1,1,1,1,1,1}));
     * agents[5].sendBid(new Bid(marketBasis, new double[]
     * {0,0,0,0,0,0,0,0,0,0,0})); agents[6].sendBid(new Bid(marketBasis, new
     * double[] {5,5,5,5,5,0,0,0,0,0,0})); agents[7].sendBid(new
     * Bid(marketBasis, new double[] {0,0,0,0,0,0,-4,-4,-4,-4,-4}));
     * agents[8].sendBid(new Bid(marketBasis, new double[]
     * {3,3,3,3,0,0,0,0,0,0,0})); agents[9].sendBid(new Bid(marketBasis, new
     * double[] {0,0,0,-2,-2,-2,-2,-2,-2,-2,-2})); agents[10].sendBid(new
     * Bid(marketBasis, new double[] {1,1,1,1,1,1,1,0,0,0,0}));
     * agents[11].sendBid(new Bid(marketBasis, new double[]
     * {7,7,7,7,7,7,7,0,0,0,0})); agents[12].sendBid(new Bid(marketBasis, new
     * double[] {0,0,0,-6,-6,-6,-6,-6,-6,-6,-6})); agents[13].sendBid(new
     * Bid(marketBasis, new double[] {8,8,8,8,8,8,8,8,8,8,8}));
     * agents[14].sendBid(new Bid(marketBasis, new double[]
     * {-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9})); agents[15].sendBid(new
     * Bid(marketBasis, new double[] {0,0,0,0,0,0,0,0,-8,-8,-8}));
     * agents[16].sendBid(new Bid(marketBasis, new double[]
     * {4,4,4,4,4,4,3,3,3,3,3})); agents[17].sendBid(new Bid(marketBasis, new
     * double[] {2,2,2,2,1,1,1,1,0,0,0})); agents[18].sendBid(new
     * Bid(marketBasis, new double[] {-1,-1,-1,-1,-2,-2,-2,-2,-3,-3,-3}));
     * agents[19].sendBid(new Bid(marketBasis, new double[]
     * {6,6,6,6,6,6,0,0,0,0,0})); agents[20].sendBid(new Bid(marketBasis, new
     * double[] {-5,-5,-5,-5,-5,8,8,8,8,8,8})); assertArrayEquals(new double[]
     * {29,29,29,21,16,11,0,-8,-18,-18,-18},
     * this.matcher.lastReceivedBid.getDemand(), 0); unbindAgents(21); }
     */
}
