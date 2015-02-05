package net.powermatcher.integration.base;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.integration.util.AuctioneerWrapper;
import net.powermatcher.integration.util.ConcentratorWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockScheduler;

import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class BidResilienceTest
    extends ResilienceTest {

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
        agentList = new ArrayList<MockAgent>();

        // Create matcher list
        matchers = new ArrayList<MatcherEndpoint>();

        // Get the expected results
        resultsReader = new CsvExpectedResultsReader(getExpectedResultsFile(testID, suffix));

        marketBasis = resultsReader.getMarketBasis();

        auctioneer = new AuctioneerWrapper();
        Map<String, Object> auctioneerProperties = new HashMap<String, Object>();
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

        matchers.add(auctioneer);
        auctioneerTimer = new MockScheduler();
        auctioneer.setExecutorService(auctioneerTimer);
        auctioneer.setTimeService(new SystemTimeService());
        auctioneer.activate(auctioneerProperties);

        concentrator = new ConcentratorWrapper();
        Map<String, Object> concentratorProperties = new HashMap<String, Object>();
        concentratorProperties.put("matcherId", CONCENTRATOR_NAME);
        concentratorProperties.put("desiredParentId", AUCTIONEER_NAME);
        concentratorProperties.put("bidTimeout", "600");
        concentratorProperties.put("bidUpdateRate", "30");
        concentratorProperties.put("agentId", CONCENTRATOR_NAME);
        concentratorProperties.put("whiteListAgents", new ArrayList<String>());
        matchers.add(concentrator);
        concentratorTimer = new MockScheduler();
        concentrator.setExecutorService(concentratorTimer);
        concentrator.setTimeService(new SystemTimeService());
        concentrator.activate(concentratorProperties);

        // Session
        sessionManager = new SessionManager();
        sessionManager.addMatcherEndpoint(auctioneer);
        sessionManager.addMatcherEndpoint(concentrator);
        sessionManager.addAgentEndpoint(concentrator);
        sessionManager.activate();

        // Create the bid reader
        bidReader = new CsvBidReader(getBidInputFile(testID, suffix), marketBasis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void sendBidsToMatcher() throws IOException, DataFormatException {
        ArrayBid bid = null;
        MockAgent newAgent;

        double[] aggregatedDemand = new double[marketBasis.getPriceSteps()];

        boolean stop = false;
        int i = 0;
        do {
            try {
                bid = bidReader.nextBid();

                if (bid != null) {
                    // Aggregated demand calculation
                    double[] demand = bid.getDemand();
                    for (int j = 0; j < demand.length; j++) {
                        aggregatedDemand[j] = aggregatedDemand[j] + demand[j];
                    }

                    if (agentList.size() > i) {
                        newAgent = agentList.get(i);
                    } else {
                        newAgent = createAgent(i);
                    }
                    newAgent.sendBid(bid);
                    i++;
                } else {
                    stop = true;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Incorrect bid specification caught: " + e.getMessage());
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Bid nextBidToMatcher(int id) throws IOException, DataFormatException {
        MockAgent newAgent;
        Bid bid = bidReader.nextBid();

        if (bid != null) {
            newAgent = createAgent(id);
            newAgent.sendBid(bid);
        }

        return bid;
    }

    private MockAgent createAgent(int i) {
        String agentId = "agent" + (i + 1);
        MockAgent newAgent = new MockAgent(agentId);
        agentList.add(i, newAgent);

        newAgent.setDesiredParentId(CONCENTRATOR_NAME);
        addAgent(newAgent);
        return newAgent;
    }

    protected void checkEquilibriumPrice() {
        double expPrice = resultsReader.getEquilibriumPrice();

        // Verify the price received by the agents
        for (MockAgent agent : agentList) {
            assertEquals(expPrice, agent.getLastPriceUpdate().getPrice().getPriceValue(), 0);
        }
    }

    @After
    public void tearDown() throws IOException {
        if (bidReader != null) {
            bidReader.closeFile();
        }
        removeAgents(agentList, concentrator);
    }
}
