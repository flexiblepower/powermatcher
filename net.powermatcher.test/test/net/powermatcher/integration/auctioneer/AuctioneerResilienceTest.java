package net.powermatcher.integration.auctioneer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.integration.base.ResilienceTest;
import net.powermatcher.integration.util.AuctioneerWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.test.helpers.PropertiesBuilder;
import net.powermatcher.test.helpers.TestClusterHelper;

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

    protected void prepareTest(String testID, String suffix) throws IOException, DataFormatException {
        // Get the expected results
        resultsReader = new CsvExpectedResultsReader(getExpectedResultsFile(testID, suffix));

        MarketBasis marketBasis = resultsReader.getMarketBasis();
        auctioneer = new AuctioneerWrapper();
        Map<String, Object> auctioneerProperties = new PropertiesBuilder().agentId("auctioneer")
                                                                         .marketBasis(marketBasis)
                                                                         .minTimeBetweenPriceUpdates(1000)
                                                                         .clusterId("testCluster")
                                                                         .build();
        auctioneer.activate(auctioneerProperties);

        cluster = new TestClusterHelper(marketBasis, auctioneer);

        // Create the bid reader
        bidReader = new CsvBidReader(getBidInputFile(testID, suffix), marketBasis);
    }

    protected void checkEquilibriumPrice() {
        double expPrice = resultsReader.getEquilibriumPrice();

        // Actual Scheduler does not work. Use MockScheduler to manually call
        // timertask.
        cluster.performTasks();

        // Verify the price received by the agents
        for (MockAgent agent : cluster) {
            assertEquals(expPrice, agent.getLastPriceUpdate().getPrice().toPriceStep().toPrice().getPriceValue(), 0);
        }
    }

    @After
    public void tearDown() throws IOException {
        bidReader.closeFile();
        cluster.close();
    }
}
