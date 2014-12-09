package net.powermatcher.integration.base;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.integration.util.AuctioneerWrapper;
import net.powermatcher.integration.util.ConcentratorWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockScheduler;

import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidResilienceTest extends ResilienceTest {

    private static final String AUCTIONEER_NAME = "auctioneer";
    private static final String CONCENTRATOR_NAME = "concentrator";
    private static final Logger LOGGER = LoggerFactory.getLogger(BidResilienceTest.class);

    protected MockScheduler auctioneerTimer;
    protected MockScheduler concentratorTimer;

    // The direct upstream matcher for the agents
    protected ConcentratorWrapper concentrator;

    // mock auctioneer
    protected AuctioneerWrapper auctioneer;

    protected void prepareTest(String testID, String suffix) throws IOException, DataFormatException {
        // Create agent list
        this.agentList = new ArrayList<MockAgent>();

        // Create matcher list
        this.matchers = new ArrayList<MatcherEndpoint>();

        // Get the expected results
        this.resultsReader = new CsvExpectedResultsReader(getExpectedResultsFile(testID, suffix));

        this.marketBasis = resultsReader.getMarketBasis();

        this.auctioneer = new AuctioneerWrapper();
        Map<String, Object> auctioneerProperties = new HashMap<>();
        auctioneerProperties.put("id", MATCHERNAME);
        auctioneerProperties.put("matcherId", MATCHERNAME);
        auctioneerProperties.put("agentId", MATCHERNAME);
        auctioneerProperties.put("commodity", "electricity");
        auctioneerProperties.put("currency", "EUR");
        auctioneerProperties.put("priceSteps", marketBasis.getPriceSteps());
        auctioneerProperties.put("minimumPrice", marketBasis.getMinimumPrice());
        auctioneerProperties.put("maximumPrice", marketBasis.getMaximumPrice());
        auctioneerProperties.put("bidTimeout", "600");
        auctioneerProperties.put("priceUpdateRate", "1");
        auctioneerProperties.put("clusterId", "testCluster");

        this.matchers.add(this.auctioneer);
        auctioneerTimer = new MockScheduler();
        auctioneer.setExecutorService(auctioneerTimer);
        auctioneer.setTimeService(new SystemTimeService());
        auctioneer.activate(auctioneerProperties);

        this.concentrator = new ConcentratorWrapper();
        Map<String, Object> concentratorProperties = new HashMap<>();
        concentratorProperties = new HashMap<String, Object>();
        concentratorProperties.put("matcherId", CONCENTRATOR_NAME);
        concentratorProperties.put("desiredParentId", AUCTIONEER_NAME);
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", CONCENTRATOR_NAME);

        this.matchers.add(this.concentrator);
        this.concentratorTimer = new MockScheduler();
        concentrator.setExecutorService(concentratorTimer);
        concentrator.setTimeService(new SystemTimeService());
        concentrator.activate(concentratorProperties);

        // Session
        this.sessionManager = new SessionManager();
        sessionManager.addMatcherEndpoint(auctioneer);
        sessionManager.addMatcherEndpoint(concentrator);
        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.activate();

        // Create the bid reader
        this.bidReader = new CsvBidReader(getBidInputFile(testID, suffix), this.marketBasis);
    }

    @Override
    protected void sendBidsToMatcher() throws IOException, DataFormatException {
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
        } while (!stop);
        concentratorTimer.doTaskOnce();
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

    @Override
    protected Bid nextBidToMatcher(int id) throws IOException, DataFormatException {
        MockAgent newAgent;
        Bid bid = this.bidReader.nextBid();

        if (bid != null) {
            newAgent = createAgent(id);
            newAgent.sendBid(bid);
        }

        return bid;
    }

    private MockAgent createAgent(int i) {
        String agentId = "agent" + (i + 1);
        MockAgent newAgent = new MockAgent(agentId);
        this.agentList.add(i, newAgent);

        newAgent.setDesiredParentId(CONCENTRATOR_NAME);
        addAgent(newAgent);
        return newAgent;
    }

    protected void checkEquilibriumPrice() {
        double expPrice = this.resultsReader.getEquilibriumPrice();

        // Verify the price received by the agents
        for (MockAgent agent : agentList) {
            assertEquals(expPrice, agent.getLastPriceUpdate().getCurrentPrice(), 0);
        }
    }

    @After
    public void tearDown() throws IOException {
        if (this.bidReader != null) {
            this.bidReader.closeFile();
        }
        removeAgents(agentList, this.concentrator);
    }
}
