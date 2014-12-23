package net.powermatcher.integration.test;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.integration.base.BidResilienceTest;
import net.powermatcher.mock.MockAgent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate the correct behaviour of the components when incorrect prices are inserted.
 * 
 * @author IBM
 * 
 */
public class SendReceivePriceTestCPQ1 extends BidResilienceTest {

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
        auctioneerTimer.doTaskOnce();

        // Validate if concentrator receives correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPrice().getPrice()
                .getPriceValue(), 0);

        // Send null price
        PriceUpdate nullPriceUpdate = null;
        try {
            this.auctioneer.publishPrice(nullPriceUpdate);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is(equalTo("Price cannot be null")));
        }

        // Validate if concentrator has rejected the incorrect price and retained the last correct price.
        // Now uses concentrator.getLastReceived as auctioneer.getLastPublishedPrice is not reliable anymore
        assertEquals(true, (this.concentrator.getLastReceivedPriceUpdate() == null));

        // Check the last received price. The auctioneer should not have published the null
        // price and the last price at the concentrator should be the price that was sent earlier.
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPrice().getPrice()
                .getPriceValue(), 0);

    }

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. The concentrator is sent directly a null
     * price. The concentrator should reject it.
     * 
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    // (expected=IllegalArgumentException.class)
    public void rejectReceivalNullPriceConcentratorCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerTimer.doTaskOnce();
        // Check if concentrator received correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastReceivedPriceUpdate()
                .getPrice().getPriceValue(), 0);

        // Send incorrect price directly to the concentrator
        PriceUpdate falsePriceUpdate = null;
        try {
            this.concentrator.updatePrice(falsePriceUpdate);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        // Check if concentrator retains last received correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPublishedPriceUpdate()
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
        auctioneerTimer.doTaskOnce();
        // Validate if concentrator receives correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPrice().getPrice()
                .getPriceValue(), 0);

        // Send price outside range price
        Price price = new Price(this.marketBasis, 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        this.auctioneer.publishPrice(priceUpdate);

        // Validate if concentrator has received the new price
        // Now uses concentrator.getLastReceived as auctioneer.getLastPublishedPrice is not reliable anymore
        assertEquals(priceUpdate.getPrice().getPriceValue(), this.concentrator.getLastReceivedPriceUpdate().getPrice()
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
        auctioneerTimer.doTaskOnce();
        // Validate if concentrator receives correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPrice().getPrice()
                .getPriceValue(), 0);

        // Send price outside range price
        Price price = new Price(this.marketBasis, 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        this.auctioneer.publishPrice(priceUpdate);

        // Validate if concentrator has received the new price
        assertEquals(price.getPriceValue(), this.concentrator.getLastPrice().getPrice().getPriceValue(), 0);
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
    public void publishPriceOutsideRangeConcentratorCPQ1() throws IOException, DataFormatException {

        // Prepare the test for reading test input
        prepareTest("CPQ/CPQ1", null);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Price 52.0 is out of bounds [0.0, 50.0]");

        // Send bids to the matcherAgent (concentrator)
        sendBidsToMatcher();
        auctioneerTimer.doTaskOnce();
        // Validate if concentrator receives correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPrice().getPrice()
                .getPriceValue(), 0);

        // Send price outside range
        Price price = new Price(this.marketBasis, 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 0);
        this.auctioneer.publishPrice(priceUpdate);

        // Validate if concentrator publishes the price to the agents
        assertEquals(price.getPriceValue(), this.concentrator.getLastPublishedPriceUpdate().getPrice().getPriceValue(),
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
        auctioneerTimer.doTaskOnce();
        // Send incorrect price directly to the concentrator
        LOGGER.info("4. Sending incorrect price (null) by auctioneer");
        PriceUpdate falsePriceUpdate = null;
        try {
            // this.concentrator.updatePrice(falsePrice);
            this.auctioneer.publishPrice(falsePriceUpdate);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        // Check if concentrator retains last received correct price
        assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPrice().getPrice()
                .getPriceValue(), 0);
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
        Price price = new Price(this.marketBasis, 52.0d);
        PriceUpdate priceUpdate = new PriceUpdate(price, 1);
        this.auctioneer.publishPrice(priceUpdate);

        // Verify the price received by the agents
        for (MockAgent agent : agentList) {
            assertEquals(price.getPriceValue(), agent.getLastPriceUpdate().getPrice().getPriceValue(), 0);
        }
    }
}
