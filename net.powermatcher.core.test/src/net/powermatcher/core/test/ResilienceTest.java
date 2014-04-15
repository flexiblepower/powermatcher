package net.powermatcher.core.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.concentrator.test.MockAgent;
import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.test.util.CsvBidReader;
import net.powermatcher.core.test.util.CsvExpectedResultsReader;

import org.junit.After;
import org.junit.Before;



public class ResilienceTest {
	// Reader for the bid info input file
	protected CsvBidReader bidReader;
	
	// Reader for the expected results
	protected CsvExpectedResultsReader resultsReader;
	
	// The market basis
	protected  MarketBasis marketBasis;
	
	// The direct upstream matcher for the agents
	protected MatcherAgent matcherAgent;
	
	// List of matcher agents (for setting market basis)
	protected List<MatcherAgent> matchers;
	
	// List of agents sending bids from
	protected List<MockAgent> agentList;
	
	
	@Before
	public void setUp() throws Exception {

		// Create agent list
		this.agentList = new ArrayList<MockAgent>();
		
		// Create matcher list
		this.matchers  = new ArrayList<MatcherAgent>();
	}
	
	@After
	public void tearDown() throws IOException {
		if (this.bidReader != null) {
			this.bidReader.closeFile();
		}
		this.unbindAgents(agentList, this.matcherAgent);
	}
	
	protected void bindAgent(MockAgent agent, MatcherAgent matcher) {
		matcher.bind(agent);
		agent.bind(matcher);
	}
	
	private void unbindAgents(List<MockAgent> list, MatcherAgent matcher) {
		for (MockAgent agent : list) {
			matcher.unbind(agent);
			agent.unbind(matcher);	
		}
		list.clear();
	}
	
	protected void setMarketBasis(MarketBasis marketBasis) {
		this.marketBasis = marketBasis;
		for (MatcherAgent agent : matchers) {
			agent.updateMarketBasis(marketBasis);
		}
	}
	
	protected void prepareTest(String testID, String suffix) throws IOException, DataFormatException {
		// Get the expected results
		this.resultsReader = new CsvExpectedResultsReader(getExpectedResultsFile(testID, suffix));
		
		// Set the market basis
		setMarketBasis(this.resultsReader.getMarketBasis());
		
		// Create the bid reader
		this.bidReader = new CsvBidReader(getBidInputFile(testID, suffix), this.marketBasis);
	}
	
	
	protected BidInfo nextBidToMatcher(MatcherAgent matcher, int id) throws IOException, DataFormatException {
		MockAgent newAgent;
		BidInfo bid = this.bidReader.nextBidInfo();

		if (bid != null) {
			newAgent = new MockAgent("agent" + (id+1));
			newAgent.updateMarketBasis(marketBasis);
			this.agentList.add(id, newAgent);
			bindAgent(newAgent, matcher);

			newAgent.sendBid(bid);
		}
		
		return bid;
	}
	
	protected void sendBidsToMatcher(MatcherAgent matcher) throws IOException, DataFormatException {
		BidInfo bid = null;
		MockAgent newAgent;

		double[] aggregatedDemand = new double[this.marketBasis.getPriceSteps()];
		
		boolean stop = false;
		int i = 0;
		do {
			try {
				bid = this.bidReader.nextBidInfo();
				
				if (bid != null) {
					// Aggregated demand calculation
					double demand[] = bid.getDemand();
					for (int j = 0; j < demand.length; j++) {
						aggregatedDemand[j] = aggregatedDemand[j] + demand[j];
					}
					
					if (agentList.size() > i) {
						newAgent = this.agentList.get(i);
					}
					else {
						newAgent = new MockAgent("agent" + (i+1));
						newAgent.updateMarketBasis(marketBasis);
						this.agentList.add(i, newAgent);
						bindAgent(newAgent, matcher);
					}
					newAgent.sendBid(bid);
					i++;
				}
				else {
					stop = true;
				}
			}
			catch (InvalidParameterException e) {
				System.err.println("Incorrect bid specification found: "+ e.getMessage());
				bid = null;
			}					
						
		} while (!stop);

		// Write aggregated demand array
		System.out.print("Aggregated demand: ");
		for (int j = 0; j < aggregatedDemand.length; j++) {
			if (j == (aggregatedDemand.length -1)) {
				System.out.println(aggregatedDemand[j]);
			}
			else {
				System.out.print(aggregatedDemand[j] + ",");
			}
		}

	}
	
	public String getExpectedResultsFile(String testID, String suffix) {
		String csvSuffix = null;
		if (suffix == null)
			csvSuffix = ".csv";
		else
			csvSuffix = suffix + ".csv";
		
		return "input/" + testID + "/AggBidPrice" + csvSuffix;
	}
	
	public String getBidInputFile(String testID, String suffix) {
		String csvSuffix = null;
		if (suffix == null)
			csvSuffix = ".csv";
		else
			csvSuffix = suffix + ".csv";
		
		return "input/" + testID + "/Bids" + csvSuffix;
	}
	
	protected void checkEquilibriumPrice() {		
		double expPrice = this.resultsReader.getEquilibriumPrice();
						
		// Verify the price received by the agents
		for (MockAgent agent : agentList) {	
			assertEquals(expPrice, agent.lastPriceUpdate.getCurrentPrice(), 0);
		}
	}

	protected void checkAggregatedBid(BidInfo aggregatedBid) {
		// Verify the aggregated bid
		assertArrayEquals(this.resultsReader.getAggregatedBid().getDemand(), aggregatedBid.getDemand(), 0);
	}
}
