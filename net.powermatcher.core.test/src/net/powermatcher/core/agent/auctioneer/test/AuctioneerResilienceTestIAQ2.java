package net.powermatcher.core.agent.auctioneer.test;

import java.io.IOException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;
import net.powermatcher.core.test.AuctioneerWrapper;
import net.powermatcher.core.test.ResilienceTest;

import org.junit.Before;
import org.junit.Test;

public class AuctioneerResilienceTestIAQ2 extends ResilienceTest {

	private AuctioneerWrapper auctioneer;
	
	/**
	 * Set up the test by creating an auctioneer which
	 * will receive directly the bids from the agents.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUpAuctioneer() throws Exception {
		
		// Init Auctioneer
		this.auctioneer = new AuctioneerWrapper();
		Properties auctioneerProperties = new Properties();
		auctioneerProperties.setProperty("id", "auctioneer");
		auctioneerProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.auctioneer.setConfiguration(new BaseConfiguration(auctioneerProperties));
		this.matchers.add(this.auctioneer);
		
		// Set the matcher agent for the agents
		this.matcherAgent = this.auctioneer;
	}
	
	/**
	 * No equilibrium (demand side). Agents send series of bids
	 * with no-equilibrium price.
	 * 
	 * Check the equilibrium.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityTestIAQ2NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ2/IAF1", null);
	}
	
	/**
	 * No equilibrium (demand side). Agents send series of bids
	 * with no-equilibrium price.
	 * 
	 * Check the aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityAggrgationTestIAQ2NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ2/IAF1", null);		
	}
	
	/**
	 * No equilibrium (supply side) Agents send series of bids
	 * with no-equilibrium price.
	 * 
	 * Check the equilibrium.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityTestIAQ2NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ2/IAF2", null);
	}

	/**
	 * No equilibrium (supply side) Agents send series of bids
	 * with no-equilibrium price.
	 * 
	 * Check the aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityAggregationTestIAQ2NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ2/IAF2", null);
	}

	/**
	 * Agents send series of bids with a guaranteed equilibrium price.
	 * Scenario 1.
	 * 
	 * Check the equilibrium.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityTestIAQ2EquilibriumSmallNumberOfBidsIAF3T1() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ2/IAF3/Test1", null);
	}

	/**
	 * Agents send series of bids with a guaranteed equilibrium price.
	 * Scenario 1.
	 * 
	 * Check the aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityAggregationTestIAQ2EquilibriumSmallNumberOfBidsIAF3T1() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ2/IAF3/Test1", null);
	}
	
	/**
	 * Agents send series of bids with a guaranteed equilibrium price.
	 * Scenario 2.
	 * 
	 * Check the equilibrium.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityTestIAQ2EquilibriumSmallNumberOfBidsIAF3T2() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ2/IAF3/Test2", null);
	}
	
	/**
	 *  Agents send series of bids with a guaranteed equilibrium price.
	 * Scenario 2.
	 * 
	 * Check the aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityAggregationTestIAQ2EquilibriumSmallNumberOfBidsIAF3T2() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ2/IAF3/Test2", null);
	}
	
	/**
	 * Multiple consecutive equilibriums. 
	 * 
	 * Series of bids with a guaranteed equilibrium price, followed by another 
	 * single bid or series of bids with another guaranteed equilibrium price.
	 * The first expected equilibrium price, followed by the second expected equilibrium price.
	 * 
	 * Check the equilibrium.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityTestIAQ2multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ2/IAF4", "1");
		performEquilibriumTest("IAQ/IAQ2/IAF4", "2");
	}

	/**
	 * Multiple consecutive equilibriums. 
	 * 
	 * Series of bids with a guaranteed equilibrium price, followed by another 
	 * single bid or series of bids with another guaranteed equilibrium price.
	 * The first expected equilibrium price, followed by the second expected equilibrium price.
	 * 
	 * Check the aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityAggregationTestIAQ2multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ2/IAF4", "1");
		performAggregatedBidTest("IAQ/IAQ2/IAF4", "2");
	}

	/**
	 * Equilibrium including bid rejection.
	 * 
	 * Series of bids with a guaranteed equilibrium price,including an ascending bid.
	 * Expected outcome is the difened expectedquilibrium price, with the ascending bid
	 * being rejected.
	 * 
	 * Check the equilibrium.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityTestIAQ2equilibriumWithBidRejection() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ2/IAF5", null);
	}
	
	/**
	 * Equilibrium including bid rejection.
	 * 
	 * Series of bids with a guaranteed equilibrium price,including an ascending bid.
	 * Expected outcome is the difened expectedquilibrium price, with the ascending bid
	 * being rejected.
	 * 
	 * 
	 * Check the aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityAggregationTestIAQ2equilibriumWithBidRejection() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ2/IAF5", null);
	}
	
	
	private void performEquilibriumTest(String testID, String suffix) throws IOException, DataFormatException {
		prepareTest(testID, suffix);
		
		sendBidsToMatcher(this.matcherAgent);
		
		checkEquilibriumPrice();
	}
	
	private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
		prepareTest(testID, suffix);
		
		sendBidsToMatcher(this.matcherAgent);
		
		checkAggregatedBid(this.auctioneer.getAggregatedBid());
	}
}
