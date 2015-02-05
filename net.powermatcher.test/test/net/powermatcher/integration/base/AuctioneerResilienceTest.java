package net.powermatcher.integration.base;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.integration.util.AuctioneerWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockScheduler;
import net.powermatcher.mock.MockTimeService;

import org.junit.After;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class AuctioneerResilienceTest
    extends ResilienceTest {

    // The direct upstream matcher for the agents
    protected AuctioneerWrapper auctioneer;

    protected MockScheduler timer;

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
        timer = new MockScheduler();
        auctioneer.setExecutorService(timer);
        auctioneer.setTimeService(new MockTimeService(0));
        auctioneer.activate(auctioneerProperties);

        // Session
        sessionManager = new SessionManager();
        sessionManager.addMatcherEndpoint(auctioneer);
        sessionManager.activate();

        // Create the bid reader
        bidReader = new CsvBidReader(getBidInputFile(testID, suffix), marketBasis);
    }

    protected void checkEquilibriumPrice() {
        double expPrice = resultsReader.getEquilibriumPrice();

        // Actual Scheduler does not work. Use MockScheduler to manually call
        // timertask.
        timer.doTaskOnce();

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
        removeAgents(agentList, auctioneer);
    }
}
