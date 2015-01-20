package net.powermatcher.integration.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.integration.base.BidResilienceTest;

import org.junit.Test;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class SendReceiveBidTestCBF1 extends BidResilienceTest {

    /**
     * A set of agents send a bid to the auctioneer via the concentrator. The the auctioneer will send a price update
     * downstream to the agents via the concentrator. The price sent by the auctioneer should be equal to the price
     * received by the agents.
     * 
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void sendBidToUpStreamComponentCBF1() throws IOException, DataFormatException {
        // Prepare the test for reading test input
        prepareTest("CBF/CBF1", null);

        // Send bids to the matcherAgent (concentrator). The auctioneer will
        // publish a new price.
        Bid bid = null;
        boolean stop = false;
        int id = 0;
        do {
            bid = nextBidToMatcher(id);
            if (bid != null) {
                concentrator.doBidUpdate();
                // Check if the concentrator received the bid
                assertEquals(bid, this.concentrator.getLastReceivedBid());

                // Check if the published bid by concentrator is received at the
                // auctioneer
                ArrayBid lastReceivedBid = this.auctioneer.getLastReceivedBid().toArrayBid();
                ArrayBid aggregatedBid = auctioneer.getAggregatedBid(marketBasis).toArrayBid();
                assertThat(lastReceivedBid.getDemand(), is(equalTo(aggregatedBid.getDemand())));
                assertThat(lastReceivedBid.getMarketBasis(), is(equalTo(aggregatedBid.getMarketBasis())));

                auctioneer.publishPrice();
                // Check if the price published by the auctioneer arrives at the
                // agent
                // concentrator.getLastReceived instead of
                // auctioneer.getLastPublished, because
                // lastPublished is not reliable anymore.
                PriceUpdate lastPriceUpdate = this.agentList.get(id).getLastPriceUpdate();
                PriceUpdate lastReceivedPriceUpdate = this.concentrator.getLastReceivedPriceUpdate();
                assertThat(lastPriceUpdate.getPrice(), is(equalTo(lastReceivedPriceUpdate.getPrice())));
            } else {
                stop = true;
            }

            id++;
        } while (stop == false);
    }
}
