package net.powermatcher.integration.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.integration.base.BidResilienceTest;
import net.powermatcher.mock.MockAgent;

import org.junit.Test;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class SendReceivePriceTestCPF1 extends BidResilienceTest {

	/**
	 * A set of agents send a bid to the auctioneer via the concentrator. The
	 * the auctioneer will send a price update downstream to the agents via the
	 * concentrator. The price sent by the auctioneer should be equal to the
	 * price received by the agents.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void sendPriceToDownStreamComponentCPF1() throws IOException,
			DataFormatException {

		// Prepare the test for reading test input
		prepareTest("CPF/CPF1", null);

		// Send bids to the matcherAgent (concentrator). The auctioneer will
		// publish a new price.
		sendBidsToMatcher();

		this.auctioneer.publishPrice();
		// Get the new price calculated and published by the auctioneer
		PriceUpdate priceUpdate = this.concentrator
				.getLastReceivedPriceUpdate();

		// Verify the equilibrium
		assertEquals(this.resultsReader.getEquilibriumPrice(),
				this.concentrator.getLastPrice().getPrice().getPriceValue(),
				0.0);

		// Check received price in concentrator
		assertEquals(priceUpdate, this.concentrator.getLastPrice());

		// Check the published by the concentrator
		assertEquals(this.concentrator.getLastPrice(),
				this.concentrator.getLastPublishedPriceUpdate());

		// Verify the price received by the agents
		// The bidnumber should be zero, since the original bid has a bidnumber
		// of 0
		int bidNumber = 0;
		for (MockAgent agent : agentList) {
			assertEquals(priceUpdate.getPrice(), agent.getLastPriceUpdate()
					.getPrice());
			assertEquals(bidNumber, agent.getLastPriceUpdate().getBidNumber());
		}
	}
}
