package net.powermatcher.integration.base;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.integration.util.ConcentratorWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;

import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcentratorResilienceTest {

    private static final String MATCHERAGENTNAME = "auctioneer";
    private static final String MATCHERNAME = "concentrator";
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcentratorResilienceTest.class);

    // Reader for the bid info input file
    protected CsvBidReader bidReader;

    // Reader for the expected results
    protected CsvExpectedResultsReader resultsReader;

    // The market basis
    protected MarketBasis marketBasis;

    // The direct upstream matcher for the agents
    protected ConcentratorWrapper matcherAgent;

    protected MockMatcherAgent matcher;

    // List of matcher agents (for setting market basis)
    protected List<MatcherRole> matchers;

    // List of agents sending bids from
    protected List<MockAgent> agentList;

    // SessionManager to handle the connections between matcher and agents
    protected SessionManager sessionManager;

    // List of active connections
    protected List<String> activeConnections;

    @After
    public void tearDown() throws IOException {
        if (this.bidReader != null) {
            this.bidReader.closeFile();
        }
        removeAgents(agentList, this.matcherAgent);
    }

    protected void addAgent(MockAgent agent) {
        sessionManager.addAgentRole(agent, agent.getAgentProperties());
    }

    protected void removeAgents(List<MockAgent> agents, MatcherRole matcher) {
        for (MockAgent agent : agents) {
            sessionManager.removeAgentRole(agent, agent.getAgentProperties());
        }
    }

    protected void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }

    protected void prepareTest(String testID, String suffix) throws IOException, DataFormatException {
        // Create agent list
        this.agentList = new ArrayList<MockAgent>();

        // Create matcher list
        this.matchers = new ArrayList<MatcherRole>();

        // Active connections
        this.activeConnections = new ArrayList<>();
        // Get the expected results
        this.resultsReader = new CsvExpectedResultsReader(getExpectedResultsFile(testID, suffix));

        this.marketBasis = resultsReader.getMarketBasis();
        this.matcherAgent = new ConcentratorWrapper();
        Map<String, Object> concentratorProperties = new HashMap<>();
        concentratorProperties = new HashMap<String, Object>();
        concentratorProperties.put("matcherId", MATCHERNAME);
        concentratorProperties.put("desiredParentId", MATCHERAGENTNAME);
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", MATCHERNAME);

        this.matchers.add(this.matcherAgent);

        matcherAgent.setExecutorService(new ScheduledThreadPoolExecutor(10));
        matcherAgent.setTimeService(new SystemTimeService());
        matcherAgent.activate(concentratorProperties);

        matcher = new MockMatcherAgent(MATCHERAGENTNAME);
        matcher.setMarketBasis(marketBasis);
        this.matchers.add(matcher);

        Map<String, Object> sessionProperties = new HashMap<>();
        sessionProperties.put("activeConnections", activeConnections);

        // Session
        this.sessionManager = new SessionManager();
        sessionManager.addMatcherRole(matcher, matcher.getMatcherProperties());
        sessionManager.addMatcherRole(matcherAgent, concentratorProperties);
        sessionManager.addAgentRole(matcherAgent, concentratorProperties);
        activeConnections.add(MATCHERNAME + "::" + MATCHERAGENTNAME);
        sessionManager.activate(sessionProperties);

        // Create the bid reader
        this.bidReader = new CsvBidReader(getBidInputFile(testID, suffix), this.marketBasis);
    }

    protected Bid nextBidToMatcher(int id) throws IOException, DataFormatException {
        MockAgent newAgent;
        Bid bid = this.bidReader.nextBid();

        if (bid != null) {
            newAgent = createAgent(id);
            newAgent.sendBid(bid);
        }

        return bid;
    }

    protected void sendBidsToMatcher(MatcherRole matcher) throws IOException, DataFormatException {
        Bid bid = null;
        MockAgent newAgent;

        double[] aggregatedDemand = new double[this.marketBasis.getPriceSteps()];

        boolean stop = false;
        int i = 0;
        do {
            try {
                bid = this.bidReader.nextBid();

                if (bid != null) {
                    // Aggregated demand calculation
                    double[] demand = bid.getDemand();
                    for (int j = 0; j < demand.length; j++) {
                        aggregatedDemand[j] = aggregatedDemand[j] + demand[j];
                    }

                    if (agentList.size() > i) {
                        newAgent = this.agentList.get(i);
                    } else {
                        newAgent = createAgent(i);
                    }
                    newAgent.sendBid(bid);
                    i++;
                } else {
                    stop = true;
                }
            } catch (InvalidParameterException e) {
                LOGGER.error("Incorrect bid specification found: " + e.getMessage());
                bid = null;
            }
            // matcherAgent.publishNewPrice();
        } while (!stop);
        matcherAgent.doBidUpdate();
        // Write aggregated demand array
        LOGGER.info("Aggregated demand: ");
        for (int j = 0; j < aggregatedDemand.length; j++) {
            if (j == (aggregatedDemand.length - 1)) {
                LOGGER.info(String.valueOf(aggregatedDemand[j]));
            } else {
                LOGGER.info(String.valueOf((aggregatedDemand[j] + ",")));
            }
        }
    }

    private MockAgent createAgent(int i) {
        String agentId = "agent" + (i + 1);
        MockAgent newAgent = new MockAgent(agentId);
        this.agentList.add(i, newAgent);

        activeConnections.add(agentId + "::" + MATCHERNAME);

        Map<String, Object> sessionProperties = new HashMap<>();
        sessionProperties.put("activeConnections", activeConnections);
        sessionManager.modified(sessionProperties);

        addAgent(newAgent);
        return newAgent;
    }

    public String getExpectedResultsFile(String testID, String suffix) {
        String csvSuffix = null;
        if (suffix == null) {
            csvSuffix = ".csv";
        } else {
            csvSuffix = suffix + ".csv";
        }
        return "input/" + testID + "/AggBidPrice" + csvSuffix;
    }

    public String getBidInputFile(String testID, String suffix) {
        String csvSuffix = null;
        if (suffix == null) {
            csvSuffix = ".csv";
        } else {
            csvSuffix = suffix + ".csv";
        }
        return "input/" + testID + "/Bids" + csvSuffix;
    }

    protected void checkEquilibriumPrice() {
        double expPrice = this.resultsReader.getEquilibriumPrice();

        // Verify the price received by the agents
        for (MockAgent agent : agentList) {
            assertEquals(expPrice, agent.getLastPriceUpdate().getCurrentPrice(), 0);
        }
    }

    protected void checkAggregatedBid(Bid aggregatedBid) {
        assertArrayEquals(this.resultsReader.getAggregatedBid().getDemand(), aggregatedBid.getDemand(), 0);
    }
}
