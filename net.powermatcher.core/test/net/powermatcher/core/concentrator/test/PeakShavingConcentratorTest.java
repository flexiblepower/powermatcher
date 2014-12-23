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

    private String auctioneerId;
    private String concentratorId;

    private final static int NR_AGENTS = 21;
    private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10);
    private SessionManager sessionManager;
    private MockScheduler timer;

    private PeakShavingConcentrator peakShavingConcentrator;
    private Map<String, Object> concentratorProperties;

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

        auctioneerId = "auctioneer";
        concentratorId = "peakshavingconcentrator";

        concentratorProperties.put("floor", -10);
        concentratorProperties.put("ceiling", 10);

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
    }

    @After
    public void tearDown() throws Exception {
        sessionManager.removeAgentEndpoint(peakShavingConcentrator);
        sessionManager.removeMatcherEndpoint(matcher);
    }

    @Test
    public void testUpdatePrice() {

        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(peakShavingConcentrator);
        sessionManager.addMatcherEndpoint(peakShavingConcentrator);

        sessionManager.addAgentEndpoint(mockAgent);
        int bidNumber = 1;
        Bid bid = new ArrayBid(marketBasis, bidNumber, new double[] { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 });
        mockAgent.sendBid(bid);
        timer.doTaskOnce();
        PriceUpdate expected = new PriceUpdate(new Price(marketBasis, 5.0), bidNumber);
        PriceUpdate error = new PriceUpdate(new Price(marketBasis, 6.0), 2);
        peakShavingConcentrator.updatePrice(expected);
        peakShavingConcentrator.updatePrice(error);
        assertThat(mockAgent.getLastPriceUpdate(), is(equalTo(expected)));
    }

    @Test
    public void testUpdateBid() {
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(peakShavingConcentrator);
        sessionManager.addMatcherEndpoint(peakShavingConcentrator);

        sessionManager.addAgentEndpoint(mockAgent);
        double[] demandArray = new double[] { 2, 1, 0, -1, -2, -3, -4, -5, -6, -7, -8 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        peakShavingConcentrator.updateBid(mockAgent.getSession(), arrayBid);
        timer.doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 1);
        assertThat(mockMatcherAgent.getLastReceivedBid(), is(equalTo(expectedBid)));
    }

    @Test
    public void testUpdateBidPeakShaving() {
        concentratorProperties.put("floor", -1);
        concentratorProperties.put("ceiling", 1);
        peakShavingConcentrator.activate(concentratorProperties);
        MockMatcherAgent mockMatcherAgent = new MockMatcherAgent(auctioneerId);
        mockMatcherAgent.setMarketBasis(marketBasis);
        mockMatcherAgent.setDesiredParentId("test");
        MockAgent mockAgent = new MockAgent("testAgent");
        mockAgent.setDesiredParentId(concentratorId);

        SessionManager sessionManager = new SessionManager();
        sessionManager.activate();
        sessionManager.addAgentEndpoint(mockMatcherAgent);
        sessionManager.addMatcherEndpoint(mockMatcherAgent);

        sessionManager.addAgentEndpoint(peakShavingConcentrator);
        sessionManager.addMatcherEndpoint(peakShavingConcentrator);

        sessionManager.addAgentEndpoint(mockAgent);
        double[] demandArray = new double[] { 1, 1, 0, -1, -1, -1, -1, -1, -1, -1, -1 };
        ArrayBid arrayBid = new ArrayBid(marketBasis, 1, demandArray);
        peakShavingConcentrator.updateBid(mockAgent.getSession(), arrayBid);
        timer.doTaskOnce();
        Bid expectedBid = new ArrayBid(arrayBid, 1);
        assertThat(mockMatcherAgent.getLastReceivedBid(), is(equalTo(expectedBid)));
    }

    protected PriceUpdate determinePrice(Bid aggregatedBid) {
        return new PriceUpdate(aggregatedBid.calculateIntersection(0), aggregatedBid.getBidNumber());
    }

    @After
    public void deactivatePeakshaver() {
        this.peakShavingConcentrator.deactivate();
    }
}
