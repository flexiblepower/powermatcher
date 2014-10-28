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

/**
 * Validate the correct behaviour of the components when incorrect
 * prices are inserted.
 * 
 * @author IBM
 *
 */
public class SendReceivePriceTestCPQ1 extends ResilienceTest {
	
	private AuctioneerWrapper auctioneer;
	private ConcentratorWrapper concentrator;
	
	/**
	 * Set up the send and receive test by creating an auctioneer and
	 * a concentrator. The agents will send their bids to the concentrator.
	 * The latter will forward the aggregated bid to the auctioneer.
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
	 * The auctioneer is invoked to publish a null price. The auctioneer
	 * will not publish the null price but reset its publish-state. 
	 * 
	 * Check if the value of the last published price is not the null price
	 * but the most recent valid price.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void publicationNullPriceAuctioneerCPQ1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CPQ/CPQ1", null);
		
		// Send bids to the matcherAgent (concentrator)
		sendBidsToMatcher(this.matcherAgent);
				
		// Validate if concentrator receives correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);
		
		// Send null price
		PriceInfo nullPrice = null;
		this.auctioneer.publishPriceInfo(nullPrice);
		
		// Validate if concentrator has rejected the incorrect price and retained the last correct price.
		assertEquals(true, (this.auctioneer.getLastPublishedPriceInfo() == null));
		
		// Check the last received price. The auctioneer should not have published the null
		// price and the last price at the concentrator should be the price that was sent earlier.
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);

	}
	
	/**
	 * A set of agents send a bid to the auctioneer via the concentrator. 
	 * The concentrator is sent directly a null price. The concentrator
	 * should reject it. 
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test //(expected=IllegalArgumentException.class)
	public void rejectReceivalNullPriceConcentratorCPQ1() throws IOException, DataFormatException {
		
		// Prepare the test for reading test input
		prepareTest("CPQ/CPQ1", null);
		
		// Send bids to the matcherAgent (concentrator)
		sendBidsToMatcher(this.matcherAgent);
		
		// Check if concentrator received correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.lastReceivedPriceInfo.getCurrentPrice(), 0);
		
		// Send incorrect price directly to the concentrator
		PriceInfo falsePrice = null;
		try {
			this.concentrator.updatePriceInfo(falsePrice);
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}

		// Check if concentrator retains last received correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPublishedPriceInfo().getCurrentPrice(), 0);

	}
	
	/**
	 *A set of agents send a bid to the auctioneer via the concentrator. After
	 * sending a calculated (valid) price the auctioneer will be forced to send
	 * an price that is outside its local price range. According to the specifications
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
		
		// Send bids to the matcherAgent (concentrator)
		sendBidsToMatcher(this.matcherAgent);
				
		// Validate if concentrator receives correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);
		
		// Send price outside range price
		PriceInfo price = new PriceInfo(this.marketBasis, 52.0d);
		this.auctioneer.publishPriceInfo(price);
		
		// Validate if concentrator has received the new price
		assertEquals(price.getCurrentPrice(), this.auctioneer.getLastPublishedPriceInfo().getCurrentPrice(), 0);

	}
	
	/**
	 * A set of agents send a bid to the auctioneer via the concentrator. After
	 * sending a calculated (valid) price the auctioneer will be forced to send
	 * an price that is outside its local price range. According to the specifications
	 * this is permitted. Connected agents can have a different local price market base.
	 * 
	 * Check if the concentrator accepts the price.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void acceptPriceOutsideRangeConcentratorCPQ1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CPQ/CPQ1", null);
		
		// Send bids to the matcherAgent (concentrator)
		sendBidsToMatcher(this.matcherAgent);
				
		// Validate if concentrator receives correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);
		
		// Send price outside range price
		PriceInfo price = new PriceInfo(this.marketBasis, 52.0d);
		this.auctioneer.publishPriceInfo(price);
		
		// Validate if concentrator has received the new price
		assertEquals(price.getCurrentPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);
	}
	
	/**
	 *  A set of agents send a bid to the auctioneer via the concentrator. After
	 * sending a calculated (valid) price the auctioneer will be forced to send
	 * an price that is outside its local price range. According to the specifications
	 * this is permitted. Connected agents can have a different local price market base.
	 * Check if the concentrator publishes this price.
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
		
		// Send bids to the matcherAgent (concentrator)
		sendBidsToMatcher(this.matcherAgent);		
		
		// Validate if concentrator receives correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);
		
		// Send  price outside range
		PriceInfo price = new PriceInfo(this.marketBasis, 52.0d);
		this.auctioneer.publishPriceInfo(price);

		// Validate if concentrator publishes the price to the agents
		assertEquals(price.getCurrentPrice(), this.concentrator.getLastPublishedPriceInfo().getCurrentPrice(), 0);
	}




	
	/**
	 * A set of agents send a bid to the auctioneer via the concentrator. After
	 * sending a calculated (valid) price the auctioneer will be forced to send
	 * an price that is outside its local price range. According to the specifications
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
		sendBidsToMatcher(this.matcherAgent);
				
		// Send incorrect price directly to the concentrator
		System.out.println("4. Sending incorrect price (null) by auctioneer");
		PriceInfo falsePrice = null;
		try {
			this.concentrator.updatePriceInfo(falsePrice);
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}

		// Check if concentrator retains last received correct price
		assertEquals(this.resultsReader.getEquilibriumPrice(), this.concentrator.getLastPriceInfo().getCurrentPrice(), 0);
	}
	
	/**
	 * A set of agents send a bid to the auctioneer via the concentrator. After
	 * sending a calculated (valid) price the auctioneer will be forced to send
	 * an price that is outside its local price range. According to the specifications
	 * this is permitted. Connected agents can have a different local price market base.
	 * 
	 * Check if the agent accepts the price.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void acceptPriceOutsideRangeByAgentsCPQ1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CPQ/CPQ1", null);
		
		// Send bids to the matcherAgent (concentrator)
		sendBidsToMatcher(this.matcherAgent);
				
		// Send  price outside range
		PriceInfo price = new PriceInfo(this.marketBasis, 52.0d);
		this.auctioneer.publishPriceInfo(price);
		
		// Verify the price received by the agents
		for (MockAgent agent : agentList) {	
			assertEquals(price.getCurrentPrice(), agent.lastPriceUpdate.getCurrentPrice(), 0);
		}
	
	}
	
	@After
	public void tearDownSendReceive() throws Exception {
		this.auctioneer.unbind((AgentService) this.concentrator);
		this.concentrator.unbind((MatcherService) this.auctioneer);
	}
	
}
