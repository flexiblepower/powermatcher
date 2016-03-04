package net.powermatcher.integration.bids;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.mock.MockDeviceAgent;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate the correct behaviour of the components when incorrect prices are inserted.
 *
 * @author FAN
 * @version 2.1
 */
public class SendReceivePriceTestCPQ1
    extends BidResilienceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendReceivePriceTestCPQ1.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * The auctioneer is invoked to publish a null price. The auctioneer will not publish the null price but reset its
     * publish-state.
     *
     * Check if the value of the last published price is not the null price but the most recent valid price.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void publicationNullPriceAuctioneerCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerContext.doTaskOnce();

        // Validate if concentrator receives correct price
        assertEquals(resultsReader.getEquilibriumPrice(), concentrator.getLastPrice().getPrice()
                                                                      .getPriceValue(), 0);

        // Send null price
        PriceUpdate nullPriceUpdate = null;
        try {
            auctioneer.publishPrice(nullPriceUpdate);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(equalTo("Price cannot be null")));
        }

        // Validate if concentrator has rejected the incorrect price and
        // retained the last correct price.
        // Now uses concentrator.getLastReceived as
        // auctioneer.getLastPublishedPrice is not reliable anymore
        assertNull(concentrator.getLastReceivedPriceUpdate());

        // Check the last received price. The auctioneer should not have
        // published the null
        // price and the last price at the concentrator should be the price that
        // was sent earlier.
        assertEquals(resultsReader.getEquilibriumPrice(),
                     concentrator.getLastPrice().getPrice().getPriceValue(), 0);

    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. The concentrator is sent directly a null
     * price. The concentrator should reject it.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    // (expected=IllegalArgumentException.class)
    @Test
    public void rejectReceivalNullPriceConcentratorCPQ1() throws IOException, DataFormatException {
        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerContext.doTaskOnce();
        // Check if concentrator received correct price
        assertEquals(resultsReader.getEquilibriumPrice(), concentrator.getLastReceivedPriceUpdate()
                                                                      .getPrice().getPriceValue(), 0);

        // Send incorrect price directly to the concentrator
        PriceUpdate falsePriceUpdate = null;
        try {
            concentrator.handlePriceUpdate(falsePriceUpdate);
            fail("Expected an IllegalArgumentException");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        // Check if concentrator retains last received correct price
        assertEquals(resultsReader.getEquilibriumPrice(), concentrator.getLastPublishedPriceUpdate()
                                                                      .getPrice().getPriceValue(), 0);
    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. After sending a calculated (valid) price the
     * auctioneer will be forced to send an price that is outside its local price range. According to the specifications
     * this is permitted. Connected agents can have a different local price market base.
     *
     * Check if the auctioneer publishes the price.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void sendPriceOutsideMarketBaseAuctioneerCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Price 52.0 is out of bounds [0.0, 50.0]");

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerContext.doTaskOnce();
        // Validate if concentrator receives correct price
        assertEquals(resultsReader.getEquilibriumPrice(), concentrator.getLastPrice().getPrice()
                                                                      .getPriceValue(), 0);

        // Send price outside range price
        Price price = new Price(cluster.getMarketBasis(), 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        auctioneer.publishPrice(priceUpdate);

        // Validate if concentrator has received the new price
        // Now uses concentrator.getLastReceived as
        // auctioneer.getLastPublishedPrice is not reliable anymore
        assertEquals(priceUpdate.getPrice().getPriceValue(), concentrator.getLastReceivedPriceUpdate().getPrice()
                                                                         .getPriceValue(), 0);

    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. After sending a calculated (valid) price the
     * auctioneer will be forced to send an price that is outside its local price range. According to the specifications
     * this is permitted. Connected agents can have a different local price market base.
     *
     * Check if the concentrator accepts the price.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void rejectPriceOutsideRangeConcentratorCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Price 52.0 is out of bounds [0.0, 50.0]");

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerContext.doTaskOnce();
        // Validate if concentrator receives correct price
        assertEquals(resultsReader.getEquilibriumPrice(), concentrator.getLastPrice().getPrice()
                                                                      .getPriceValue(), 0);

        // Send price outside range price
        Price price = new Price(cluster.getMarketBasis(), 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        auctioneer.publishPrice(priceUpdate);

        // Validate if concentrator has received the new price
        assertEquals(price.getPriceValue(), concentrator.getLastPrice().getPrice().getPriceValue(), 0);
    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. After sending a calculated (valid) price the
     * auctioneer will be forced to send an price that is outside its local price range. According to the specifications
     * this is permitted. Connected agents can have a different local price market base. Check if the concentrator
     * publishes this price.
     *
     *
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    @Ignore("You can not send a price outside the range of the auctioneer")
    public void publishPriceOutsideRangeConcentratorCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerContext.doTaskOnce();
        // Validate if concentrator receives correct price
        assertEquals(resultsReader.getEquilibriumPrice(),
                     concentrator.getLastPrice().getPrice().getPriceValue(), 0);

        // Send price outside range
        Price price = new Price(cluster.getMarketBasis(), 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        auctioneer.publishPrice(priceUpdate);

        // Validate if concentrator publishes the price to the agents
        assertEquals(price.getPriceValue(),
                     concentrator.getLastPublishedPriceUpdate().getPrice().getPriceValue(),
                     0);
    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. After sending a calculated (valid) price the
     * auctioneer will be forced to send an price that is outside its local price range. According to the specifications
     * this is permitted. Connected agents can have a different local price market base.
     *
     * Check if the concentrator accepts the price and forwards the price to the agents.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void retainmentLastValidPriceConcentratorCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerContext.doTaskOnce();
        // Send incorrect price directly to the concentrator
        LOGGER.info("4. Sending incorrect price (null) by auctioneer");
        PriceUpdate falsePriceUpdate = null;
        try {
            // this.concentrator.updatePrice(falsePrice);
            auctioneer.publishPrice(falsePriceUpdate);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        // Check if concentrator retains last received correct price
        assertEquals(resultsReader.getEquilibriumPrice(),
                     concentrator.getLastPrice().getPrice().getPriceValue(), 0);
    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. After sending a calculated (valid) price the
     * auctioneer will be forced to send an price that is outside its local price range. Connected agents can have a
     * different local price market base.
     *
     * Check if the agent rejects the price.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void rejectPriceOutsideRangeByAgentsCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Price 52.0 is out of bounds [0.0, 50.0]");

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();

        // Send price outside range
        Price price = new Price(cluster.getMarketBasis(), 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 1);
        auctioneer.publishPrice(priceUpdate);

        // Verify the price received by the agents
        for (MockDeviceAgent agent : cluster) {
            assertEquals(price.getPriceValue(), agent.getLastPriceUpdate().getPrice().getPriceValue(), 0);
        }
    }
}
