package net.powermatcher.integration.concentrator;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.Price;
import net.powermatcher.integration.base.ConcentratorResilienceTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * Functional tests for the Concentrator.
 */
public class ConcentratorResilienceTestICF extends ConcentratorResilienceTest {

    private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher(this.concentrator);

        checkAggregatedBid(this.auctioneer.getLastReceivedBid());
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

        sendBidsToMatcher(this.concentrator);

        // Send price
        Price equilibrium = new Price(this.marketBasis, this.resultsReader.getEquilibriumPrice());
        this.auctioneer.publishPrice(equilibrium);

        // Check the received price
        Assert.assertEquals(equilibrium, this.concentrator.getLastPrice());

        // Check the forwarded price
        Assert.assertEquals(this.concentrator.getLastPrice(), this.concentrator.getLastPublishedPrice());

        // Check the prices received by the agents
        checkEquilibriumPrice();
    }

}
