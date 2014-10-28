package net.powermatcher.core.agent.concentrator.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;
import net.powermatcher.core.test.ConcentratorWrapper;
import net.powermatcher.core.test.ResilienceTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Functional tests for the Concentrator.
 * 
 * @author NL34937
 *
 */
public class ConcentratorResilienceTestICF extends ResilienceTest {


	private MockMatcherAgent matcher;
	private ConcentratorWrapper concentrator;
	
	@Before
	public void setUpConcentratorTest() throws Exception {
		// Concentrator to be tested
		Properties concentratorProperties = new Properties();
		concentratorProperties.setProperty("id", "concentrator");
		concentratorProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.concentrator = new ConcentratorWrapper(new BaseConfiguration(concentratorProperties));
		this.matchers.add(this.concentrator);
		this.matcherAgent = this.concentrator;
		
		// Matcher
		this.matcher = new MockMatcherAgent("matcher");
		this.matchers.add(this.matcher);

		
		this.matcher.bind((AgentService) this.concentrator);
		this.concentrator.bind((MatcherService) this.matcher);
	}
	

	@After
	public void tearDownConcentratorTest() throws Exception {
		this.matcher.unbind((AgentService) this.concentrator);
		this.concentrator.unbind((MatcherService) this.matcher);
	}
	
	private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
		prepareTest(testID, suffix);
		
		sendBidsToMatcher(this.matcherAgent);
		
		checkAggregatedBid(this.matcher.lastReceivedBid);
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
	 * A set of agents send a bid to the matcher via the concentrator. Some of
	 * the bids will be incorrect (ascending bid). The concentrator should reject
	 * the incorrect bid.
	 *  
	 * Check if the concentrator rejected the incorrect bids by validating the
	 * aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void sendAggregatedBidRejectAscendingICF2() throws IOException, DataFormatException {
		performAggregatedBidTest("ICF/ICF2", null);
	}
	
	/**
	 * A set of agents send a bid to the matcher via the concentrator. The 
	 * the matcher will send a price to the concentrator.
	 * Check if the concentrator receives and forwards the correct price.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void receiveAndForwardPriceICF3() throws IOException, DataFormatException {
		prepareTest("ICF/ICF3", null);
		
		sendBidsToMatcher(this.matcherAgent);
		
		// Send price
		PriceInfo equilibrium = new PriceInfo(this.marketBasis, this.resultsReader.getEquilibriumPrice());
		this.matcher.publishPriceInfo(equilibrium);
		
		// Check the received price
		assertEquals(equilibrium, this.concentrator.getLastPriceInfo());
		
		//Check the forwarded price
		assertEquals(this.concentrator.getLastPriceInfo(), this.concentrator.getLastPublishedPriceInfo());
		
		// Check the prices received by the agents
		checkEquilibriumPrice();
		
	}
	
}
