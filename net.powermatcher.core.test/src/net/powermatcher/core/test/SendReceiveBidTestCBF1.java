package net.powermatcher.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SendReceiveBidTestCBF1 extends ResilienceTest {
	private AuctioneerWrapper auctioneer;
	private ConcentratorWrapper concentrator;
	
	/**
	 * Set up the send and receive test by creating an auctioneer and
	 * a concentrator. The agents will send their bids to the concentrator.
	 * The latter will forward the aggregated bid to the auctioneer.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUpSendReceiveTest() throws Exception {
		
		// Create the auctioneer
		this.auctioneer = new AuctioneerWrapper();
		Properties auctioneerProperties = new Properties();
		auctioneerProperties.setProperty("id", "auctioneer");
		auctioneerProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.auctioneer.setConfiguration(new BaseConfiguration(auctioneerProperties));
		this.matchers.add(this.auctioneer);
		
		// Create the concentrator
		Properties concentratorProperties = new Properties();
		concentratorProperties.setProperty("id", "concentrator");
		concentratorProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.concentrator = new ConcentratorWrapper(new BaseConfiguration(concentratorProperties));
		this.matchers.add(this.concentrator);
		
		// Bind auctioneer and concentrator
		this.auctioneer.bind((DownMessagable) this.concentrator);
		this.concentrator.bind((UpMessagable) this.auctioneer);
		
		// Set the matcher agent for the agents
		this.matcherAgent = this.concentrator;
	}
	
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
	public void sendBidToUpStreamComponentCBF1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CBF/CBF1", null);
		
		// Send bids to the matcherAgent (concentrator). The auctioneer will publish a new price.
		BidInfo bid = null;
		boolean stop = false;
		int id = 0;
		do {
			try {
				bid = nextBidToMatcher(this.matcherAgent, id);
				if (bid != null) {
					// Check if the concentrator received the bid
					assertEquals(bid, this.concentrator.lastReceivedBidUpdate);
					
					// Check if the published bid by concentrator is received at the auctioneer
					assertEquals(this.concentrator.lastPublishedBidUpdate, this.auctioneer.getAggregatedBid());
					
					// Check if the price published by the auctioneer arrives at the agent
					assertEquals(this.agentList.get(id).lastPriceUpdate, this.auctioneer.getLastPublishedPriceInfo());
				} else {
					stop = true;
				}
			} catch (InvalidParameterException e) {
				System.err.println("Incorrect bid specification found: "+ e.getMessage());
				bid = null;
			}		
			id++;
		} while (stop != false);		
	}
	
	
	@After
	public void tearDownSendReceive() throws Exception {
		this.auctioneer.unbind((DownMessagable) this.concentrator);
		this.concentrator.unbind((UpMessagable) this.auctioneer);
	}
		
}
