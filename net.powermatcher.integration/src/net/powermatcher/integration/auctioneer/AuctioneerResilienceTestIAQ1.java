package net.powermatcher.integration.auctioneer;

import java.io.IOException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.integration.base.ResilienceTest;
import net.powermatcher.integration.util.AuctioneerWrapper;

import org.junit.Before;
import org.junit.Test;

public class AuctioneerResilienceTestIAQ1 extends ResilienceTest {

    private AuctioneerWrapper auctioneer;
	/**
	 * Set up the test by creating an auctioneer which
	 * will receive directly the bids from the agents.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUpAuctioneer() throws Exception {
		
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
	public void qualityTestIAQ1NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ1/IAF1", null);
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
	public void qualityAggrgationTestIAQ1NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ1/IAF1/", null);		
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
	public void qualityTestIAQ1NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ1/IAF2", null);
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
	public void qualityAggregationTestIAQ1NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ1/IAF2", null);
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
	public void qualityTestIAQ1EquilibriumSmallNumberOfBidsTestIAF3T1() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ1/IAF3/Test1", null);
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
	public void qualityAggregationTestIAQ1EquilibriumSmallNumberOfBidsTest3IAF3T1() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ1/IAF3/Test1", null);
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
	public void qualityTestIAQ1EquilibriumSmallNumberOfBidsTestIAF3T2() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ1/IAF3/Test2", null);
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
	public void qualityAggregationTestIAQ1EquilibriumSmallNumberOfBidsTestIAF3T2() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ1/IAF3/Test2", null);
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
	public void qualityTestIAQ1multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ1/IAF4", "1");
		performEquilibriumTest("IAQ/IAQ1/IAF4", "2");
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
	public void qualityAggregationTestIAQ1multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ1/IAF4", "1");
		performAggregatedBidTest("IAQ/IAQ1/IAF4", "2");
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
	public void qualityTestIAQ1equilibriumWithBidRejection() throws IOException, DataFormatException {
		performEquilibriumTest("IAQ/IAQ1/IAF5", null);
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
	public void qualityAggregationTestIAQ1equilibriumWithBidRejection() throws IOException, DataFormatException {
		performAggregatedBidTest("IAQ/IAQ1/IAF5", null);
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
