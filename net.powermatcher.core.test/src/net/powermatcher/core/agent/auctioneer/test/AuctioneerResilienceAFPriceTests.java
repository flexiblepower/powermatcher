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

/**
 * JUnit test for the Auctioneer
 * 
 */
public class AuctioneerResilienceAFPriceTests extends ResilienceTest {

	private AuctioneerWrapper auctioneer;
	
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
	
	
	@Test
	public void testEquilibriumRoundingIAF6() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF06");
	}
	
	@Test
	public void testEquilibriumRoundingIAF7() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF07");
	}
	
	@Test
	public void testEquilibriumRoundingIAF8() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF08");
	}

	@Test
	public void testEquilibriumRoundingIAF9() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF09");
	}
	
	@Test
	public void testEquilibriumRoundingIAF10() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF10");
	}
	
	@Test
	public void testEquilibriumRoundingIAF11() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF11");
	}
	
	@Test
	public void testEquilibriumRoundingIAF12() throws IOException, DataFormatException {
		performEquilibriumTest("IAF/EquilibriumTests/IAF12");
	}

	
	
	
	private void performEquilibriumTest(String testID) throws IOException, DataFormatException {
		this.performEquilibriumTest(testID, null);
	}
	
	private void performEquilibriumTest(String testID, String suffix) throws IOException, DataFormatException {
		prepareTest(testID, suffix);
		
		sendBidsToMatcher(this.matcherAgent);
		
		checkEquilibriumPrice();
	}
	
}
