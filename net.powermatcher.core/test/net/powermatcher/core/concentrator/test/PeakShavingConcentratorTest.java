package net.powermatcher.core.concentrator.test;

import static org.junit.Assert.assertArrayEquals;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.concentrator.PeakShavingConcentrator;
import net.powermatcher.core.sessions.SessionImpl;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.MockScheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PeakShavingConcentratorTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final static int NR_AGENTS = 21;
    private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    private SessionManager sessionManager;
    private MockScheduler timer;

    private PeakShavingConcentrator peakShavingConcentrator;
    private Map<String, Object> concentratorProperties;
    
    private Set<Session> sessions = new HashSet<Session>();

    private MockMatcherAgent matcher;
    private MockAgent[] agents;

    private static final String AUCTIONEER_NAME = "auctioneer";

    private static final String CONCENTRATOR_NAME = "peakshavingconcentrator";

    @Before
    public void setUp() throws Exception {
        // Concentrator to be tested
        peakShavingConcentrator = new PeakShavingConcentrator();
        concentratorProperties = new HashMap<String, Object>();
        concentratorProperties.put("matcherId", CONCENTRATOR_NAME);
        concentratorProperties.put("desiredParentId", AUCTIONEER_NAME);
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", CONCENTRATOR_NAME);
        
        concentratorProperties.put("floor", -10);
        concentratorProperties.put("ceiling", -10);

        timer = new MockScheduler();
        peakShavingConcentrator.setExecutorService(timer);
        peakShavingConcentrator.setTimeService(new SystemTimeService());
        peakShavingConcentrator.activate(concentratorProperties);

        // Matcher
        matcher = new MockMatcherAgent(AUCTIONEER_NAME);
        matcher.setMarketBasis(marketBasis);

        sessionManager = new SessionManager();
        sessionManager.addMatcherEndpoint(matcher);
        sessionManager.addMatcherEndpoint(peakShavingConcentrator);
        sessionManager.addAgentEndpoint(peakShavingConcentrator);

        // Init MockAgents
        agents = new MockAgent[NR_AGENTS];
        for (int i = 0; i < NR_AGENTS; i++) {
            String agentId = "agent" + (i + 1);
            MockAgent newAgent = new MockAgent(agentId);
            newAgent.setDesiredParentId(CONCENTRATOR_NAME);
            agents[i] = newAgent;
        }

        sessionManager.activate();
    }

    @After
    public void tearDown() throws Exception {
        sessionManager.removeAgentEndpoint(peakShavingConcentrator);
        sessionManager.removeMatcherEndpoint(matcher);
    }

    private void addAgents(int number) {
        for (int i = 0; i < number; i++) {
            this.sessionManager.addAgentEndpoint(agents[i]);
        }
    }

    private void removeAgents(int number) {
        for (int i = 0; i < number; i++) {
            this.sessionManager.removeAgentEndpoint(agents[i]);
        }
    }
    
    
    
    
    @Test
    public void sendAggregatedBidExtreme() {
        addAgents(3);
        // Run 1
        agents[0].sendBid(new Bid(marketBasis, new double[] { -5, -5, -5, -5, -5, -5, -5, -5, -5, -5, -5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { -2, -2, -2, -2, -2, -4, -4, -4, -4, -4, -4 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { -1, -1, -1, -1, -1, -1, -1, -3, -3, -3, -3 }));
        timer.doTaskOnce();
        assertArrayEquals(new double[] { -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0, -10.0 }, this.matcher
                .getLastReceivedBid().getDemand(), 0);
        // Run 2
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 3, 3, 3, 3, 3, 1, 1, 1, 1, 1, 1 }));
        timer.doTaskOnce();
        assertArrayEquals(new double[] { 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0, 8.0 }, this.matcher.getLastReceivedBid()
                .getDemand(), 0);
        // Run 3
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 }));
        timer.doTaskOnce();
        
        assertArrayEquals(new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 }, this.matcher.getLastReceivedBid()
                .getDemand(), 0);
        removeAgents(3);
    }

    @Test
    public void sendAggregatedBidRejectAscending() {
        addAgents(4);
        agents[0].sendBid(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        agents[1].sendBid(new Bid(marketBasis, new double[] { 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0 }));
        agents[2].sendBid(new Bid(marketBasis, new double[] { 0, 0, 0, 0, 0, -5, -5, -5, -5, -5, -5 }));
        timer.doTaskOnce();

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
        timer.doTaskOnce();
        assertArrayEquals(new double[] { -18.0, -18.0, -18.0, -18.0, -18.0, -18.0, -18.0, -18.0, -18.0, -18.0, -18.0 }, this.matcher
                .getLastReceivedBid().getDemand(), 0);
        removeAgents(20);
    }
    
    @Test
    public void updatePrice() {
        
        SessionImpl sessionImpl = new SessionImpl(sessionManager, agents[0], agents[0].getAgentId(), peakShavingConcentrator, agents[0].getDesiredParentId(), "1");

        sessionImpl.setMarketBasis(marketBasis);
        sessionImpl.setClusterId("defaultCluster");
        
        sessions.add(sessionImpl);

        Price newPrice = determinePrice(new Bid(marketBasis, new double[] { 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 }));
        
        for (Session session : this.sessions) {
            session.updatePrice(newPrice);
      }
    }
 
    protected Price determinePrice(Bid aggregatedBid) {
        return aggregatedBid.calculateIntersection(0);
    }
    
}
