package net.powermatcher.integration.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.Bid;
import net.powermatcher.integration.base.BidResilienceTest;

import org.junit.Test;

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

        // Send bids to the matcherAgent (concentrator). The auctioneer will publish a new price.
        Bid bid = null;
        boolean stop = false;
        int id = 0;
        do {
            bid = nextBidToMatcher(id);
            if (bid != null) {
                concentrator.doBidUpdate();
                // Check if the concentrator received the bid
                assertEquals(bid, this.concentrator.getLastReceivedBid());
                
                 // Check if the published bid by concentrator is received at the auctioneer
                 assertEquals(this.auctioneer.getLastReceivedBid(),
                 this.auctioneer.getAggregatedBid(this.marketBasis));

                // Check if the price published by the auctioneer arrives at the agent
                 //concentrator.getLastReceived instead of auctioneer.getLastPublished, because
                 //lastPublished is not reliable anymore.
                assertEquals(this.agentList.get(id).getLastPriceUpdate(), this.concentrator.getLastReceivedPrice());
            } else {
                stop = true;
            }

            id++;
        } while (stop != false);
    }
}
