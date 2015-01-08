package net.powermatcher.integration.base;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.integration.util.AuctioneerWrapper;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockScheduler;

import org.junit.After;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class AuctioneerResilienceTest extends ResilienceTest {

	// The direct upstream matcher for the agents
	protected AuctioneerWrapper auctioneer;

	protected MockScheduler timer;

	protected void prepareTest(String testID, String suffix)
			throws IOException, DataFormatException {
		// Create agent list
		this.agentList = new ArrayList<MockAgent>();

		// Create matcher list
		this.matchers = new ArrayList<MatcherEndpoint>();

		// Get the expected results
		this.resultsReader = new CsvExpectedResultsReader(
				getExpectedResultsFile(testID, suffix));

		this.marketBasis = resultsReader.getMarketBasis();
		this.auctioneer = new AuctioneerWrapper();
		Map<String, Object> auctioneerProperties = new HashMap<>();
		auctioneerProperties.put("id", MATCHERNAME);
		auctioneerProperties.put("matcherId", MATCHERNAME);
		auctioneerProperties.put("agentId", MATCHERNAME);
		auctioneerProperties.put("commodity", "electricity");
		auctioneerProperties.put("currency", "EUR");
		auctioneerProperties.put("priceSteps", marketBasis.getPriceSteps());
		auctioneerProperties.put("minimumPrice", marketBasis.getMinimumPrice());
		auctioneerProperties.put("maximumPrice", marketBasis.getMaximumPrice());
		auctioneerProperties.put("bidTimeout", "600");
		auctioneerProperties.put("priceUpdateRate", "1");
		auctioneerProperties.put("clusterId", "testCluster");

		this.matchers.add(this.auctioneer);
		timer = new MockScheduler();
		auctioneer.setExecutorService(timer);
		auctioneer.setTimeService(new SystemTimeService());
		auctioneer.activate(auctioneerProperties);

		// Session
		this.sessionManager = new SessionManager();
		sessionManager.addMatcherEndpoint(auctioneer);
		sessionManager.activate();

		// Create the bid reader
		this.bidReader = new CsvBidReader(getBidInputFile(testID, suffix),
				this.marketBasis);
	}

	protected void checkEquilibriumPrice() {
		double expPrice = this.resultsReader.getEquilibriumPrice();

		// Actual Scheduler does not work. Use MockScheduler to manually call
		// timertask.
		timer.doTaskOnce();

		// Verify the price received by the agents
		for (MockAgent agent : agentList) {
			assertEquals(expPrice, agent.getLastPriceUpdate().getPrice()
					.getPriceValue(), 0);
		}
	}

	@After
	public void tearDown() throws IOException {
		if (this.bidReader != null) {
			this.bidReader.closeFile();
		}
		removeAgents(agentList, this.auctioneer);
	}
}
