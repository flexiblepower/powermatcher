package net.powermatcher.core.agent.concentrator.test;

import java.io.IOException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;
import net.powermatcher.core.test.ConcentratorWrapper;
import net.powermatcher.core.test.ResilienceTest;

import org.junit.Before;
import org.junit.Test;

/**
 * Concentrator quality test with focus on scalability.
 * 
 * @author NL34937
 *
 */
public class ConcentratorResilienceTestICQ extends ResilienceTest {

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

		
		this.matcher.bind((DownMessagable) this.concentrator);
		this.concentrator.bind((UpMessagable) this.matcher);
	}
	
	
	/**
	 * A large set of agents send a bid to the matcher via the concentrator. 
	 *  
	 * Check if the concentrator publishes the correct aggregated bid.
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test
	public void qualityScalabilityTestICQ1SendAggregatedLarge() throws IOException, DataFormatException {
		performAggregatedBidTest("ICQ/ICQ1", null);
	}
	
	@Test
	public void qualityScalabilityTestICQ2SendAggregatedBidRejectAscending() throws IOException, DataFormatException {
		performAggregatedBidTest("ICQ/ICQ2", null);
	}
	
	
	private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
		prepareTest(testID, suffix);
		
		sendBidsToMatcher(this.matcherAgent);
		
		checkAggregatedBid(this.matcher.lastReceivedBid);
	}
}
