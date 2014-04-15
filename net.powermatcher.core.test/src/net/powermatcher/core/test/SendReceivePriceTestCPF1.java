package net.powermatcher.core.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.concentrator.test.MockAgent;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SendReceivePriceTestCPF1 extends ResilienceTest {
	
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
		this.auctioneer.bind((AgentService) this.concentrator);
		this.concentrator.bind((MatcherService) this.auctioneer);
		
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
	public void sendPriceToDownStreamComponentCPF1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CPF/CPF1", null);
		
		// Send bids to the matcherAgent (concentrator). The auctioneer will publish a new price.
		sendBidsToMatcher(this.matcherAgent);

		// Get the new price calcuated and published by the auctioneer
		PriceInfo currentPrice = this.auctioneer.getLastPublishedPriceInfo();
		
		// Verify the equilibrium
		assertEquals( this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0.0);	

		// Check received price in concentrator
		assertEquals( currentPrice, this.concentrator.getLastPriceInfo());

		// Check the published by the concentrator
		assertEquals( this.concentrator.getLastPriceInfo(), this.concentrator.getLastPublishedPriceInfo());
		
		// Verify the price received by the agents
		for (MockAgent agent : agentList) {	
			assertEquals(currentPrice, agent.lastPriceUpdate);
		}
		
	}
	
	
	@After
	public void tearDownSendReceive() throws Exception {
		this.auctioneer.unbind((AgentService) this.concentrator);
		this.concentrator.unbind((MatcherService) this.auctioneer);
	}
	
}
