package net.powermatcher.integration.concentrator;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.integration.base.ResilienceTest;
import net.powermatcher.integration.util.ConcentratorWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.SimpleSession;
import net.powermatcher.test.helpers.PropertieBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.junit.After;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class ConcentratorResilienceTest
    extends ResilienceTest {

    private static final String MATCHERAGENTNAME = "auctioneer";
    private static final String CONCENTRATOR_NAME = "concentrator";

    // The direct upstream matcher for the agents
    protected ConcentratorWrapper concentrator;

    // mock auctioneer
    protected MockMatcherAgent auctioneer;

    protected void prepareTest(String testID, String suffix) throws IOException, DataFormatException {
        // Get the expected results
        resultsReader = new CsvExpectedResultsReader(getExpectedResultsFile(testID, suffix));

        MarketBasis marketBasis = resultsReader.getMarketBasis();
        concentrator = new ConcentratorWrapper();
        concentrator.activate(new PropertieBuilder().agentId(CONCENTRATOR_NAME)
                                                    .desiredParentId(MATCHERAGENTNAME)
                                                    .bidUpdateRate(600)
                                                    .build());

        auctioneer = new MockMatcherAgent(MATCHERAGENTNAME, "testCluster");
        auctioneer.setMarketBasis(marketBasis);
        new SimpleSession(concentrator, auctioneer).connect();

        cluster = new TestClusterHelper(marketBasis, concentrator);

        // Create the bid reader
        bidReader = new CsvBidReader(getBidInputFile(testID, suffix), marketBasis);
    }

    @Override
    protected void sendBidsToMatcher() throws IOException, DataFormatException {
        super.sendBidsToMatcher();
        cluster.performTasks();
    }

    protected void checkEquilibriumPrice() {
        double expPrice = resultsReader.getEquilibriumPrice();

        // Verify the price received by the agents
        for (MockAgent agent : cluster) {
            assertEquals(expPrice, agent.getLastPriceUpdate().getPrice().getPriceValue(), 0);
        }
    }

    @After
    public void tearDown() throws IOException {
        bidReader.closeFile();
        cluster.close();
    }
}
