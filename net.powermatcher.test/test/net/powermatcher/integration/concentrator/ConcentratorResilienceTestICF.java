package net.powermatcher.integration.concentrator;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;

import org.junit.Assert;
import org.junit.Test;

/**
 * Functional tests for the Concentrator.
 *
 * @author FAN
 * @version 2.0
 */
public class ConcentratorResilienceTestICF
    extends ConcentratorResilienceTest {

    private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher();

        checkAggregatedBid((ArrayBid) auctioneer.getLastReceivedBid());
    }

    /**
     * A set of agents send a bid to the matcher via the concentrator.
     *
     * Check if the concentrator publishes the correct aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void sendAggregatedBidICF1Test1() throws IOException, DataFormatException {
        performAggregatedBidTest("ICF/ICF1/Test1", null);
    }

    /**
     * A set of agents send a bid to the matcher via the concentrator.
     *
     * Check if the concentrator publishes the correct aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void sendAggregatedBidICF1Test2() throws IOException, DataFormatException {
        performAggregatedBidTest("ICF/ICF1/Test2", null);
    }

    /**
     * A set of agents send a bid to the matcher via the concentrator. Some of the bids will be incorrect (ascending
     * bid). The concentrator should reject the incorrect bid.
     *
     * Check if the concentrator rejected the incorrect bids by validating the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void sendAggregatedBidRejectAscendingICF2() throws IOException, DataFormatException {
        performAggregatedBidTest("ICF/ICF2", null);
    }

    /**
     * A set of agents send a bid to the matcher via the concentrator. The the matcher will send a price to the
     * concentrator. Check if the concentrator receives and forwards the correct price.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void receiveAndForwardPriceICF3() throws IOException, DataFormatException {
        prepareTest("ICF/ICF3", null);

        sendBidsToMatcher();
        cluster.performTasks();
        // Send price
        PriceUpdate equilibrium = new PriceUpdate(new Price(cluster.getMarketBasis(),
                                                            resultsReader.getEquilibriumPrice()), 0);

        auctioneer.publishPrice(equilibrium);
        // Check the received price
        Assert.assertEquals(equilibrium, concentrator.getLastPrice());

        // Check the forwarded price
        Assert.assertEquals(concentrator.getLastPrice(), concentrator.getLastPublishedPriceUpdate());

        // Check the prices received by the agents
        checkEquilibriumPrice();
    }
}
