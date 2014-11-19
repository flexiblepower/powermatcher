package net.powermatcher.integration.base;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.zip.DataFormatException;

import org.junit.After;

import net.powermatcher.api.MatcherRole;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.integration.util.AuctioneerWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;

public class AuctioneerResilienceTest extends ResilienceTest {

    // The direct upstream matcher for the agents
    protected AuctioneerWrapper auctioneer;
    
    protected void prepareTest(String testID, String suffix) throws IOException, DataFormatException {
        // Create agent list
        this.agentList = new ArrayList<MockAgent>();

        // Create matcher list
        this.matchers = new ArrayList<MatcherRole>();

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

        auctioneer.setExecutorService(new ScheduledThreadPoolExecutor(10));
        auctioneer.setTimeService(new SystemTimeService());
        auctioneer.activate(auctioneerProperties);

        // Session
        this.sessionManager = new SessionManager();
        sessionManager.addMatcherRole(auctioneer);
        sessionManager.activate();

        // Create the bid reader
        this.bidReader = new CsvBidReader(getBidInputFile(testID, suffix), this.marketBasis);
    }
    
    protected void checkEquilibriumPrice() {
        double expPrice = this.resultsReader.getEquilibriumPrice();

        // TODO this is direct call for the Auctioneer to update its prices
        // because the scheduler doesn't work properly. Remove this once it does
        auctioneer.publishNewPrice();

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
        removeAgents(agentList, this.auctioneer);
    }
}
