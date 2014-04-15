package net.powermatcher.core.agent.concentrator.test;

import static org.junit.Assert.assertArrayEquals;

import java.util.Properties;

import net.powermatcher.core.agent.concentrator.Concentrator;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for the Concentrator
 * 
 * Every test requires a different number agents. In setUp() NR_AGENTS are 
 * instantiated. Every test the desired number of agents can be binded and 
 * unbinded using the functions bindAgents() and unbindAgents().
 */
public class ConcentratorUnitTest {

	private final static int NR_AGENTS = 21;
	private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10, 1, 0);

	private MockMatcherAgent matcher;
	private MockAgent[] agents;
	private Concentrator concentrator;
	
	@Before
	public void setUp() throws Exception {
		// Concentrator to be tested
		Properties concentratorProperties = new Properties();
		concentratorProperties.setProperty("id", "concentrator");
		concentratorProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.concentrator = new Concentrator(new BaseConfiguration(concentratorProperties));
		this.concentrator.updateMarketBasis(marketBasis);
		
		// Matcher
		this.matcher = new MockMatcherAgent("matcher");
		this.matcher.updateMarketBasis(marketBasis);
		
		this.matcher.bind((AgentService) this.concentrator);
		this.concentrator.bind((MatcherService) this.matcher);
		
		// Agents
		agents = new MockAgent[NR_AGENTS];
		for(int i = 0; i < NR_AGENTS; i++) {
			MockAgent newAgent = new MockAgent("agent" + (i+1));
			agents[i] = newAgent;
		}
	}
	

	@After
	public void tearDown() throws Exception {
		this.matcher.unbind((AgentService) this.concentrator);
		this.concentrator.unbind((MatcherService) this.matcher);
	}
	
	private void bindAgents(int number) {
		for(int i = 0; i < number; i++) {
			this.concentrator.bind(agents[i]);
			agents[i].bind(this.concentrator);
		}
	}

	private void unbindAgents(int number) {
		for(int i = 0; i < number; i++) {
			this.concentrator.unbind(agents[i]);
			agents[i].unbind(this.concentrator);
		}
	}	
	
/*
TODO: The behavior tested in this test is still subject of discussion
	@Test
	public void receivePriceAndSendToAgents() {
		bindAgents(1);
		// Run 1
		int[] values1 = {0, 1, 5, 9, 10};
		for(int value : values1) {
			this.matcher.sendPrice(new PriceInfo(this.marketBasis, value));
			assertEquals(value, agents[0].lastPriceUpdate.getNormalizedPrice(), 0);
		}
		// Run 2
		int[] values2 = {20, 11, 15, 40};
		for(int value : values2) {
			this.matcher.sendPrice(new PriceInfo(this.marketBasis, value));
			assertEquals(10, agents[0].lastPriceUpdate.getNormalizedPrice(), 0);
		}
		// Run 3
		int[] values3 = {-20, -11, -15, -1};
		for(int value : values3) {
			this.matcher.sendPrice(new PriceInfo(this.marketBasis, value));
			assertEquals(0, agents[0].lastPriceUpdate.getNormalizedPrice(), 0);
		}
		unbindAgents(1);
	}
*/
	
	@Test
	public void sendAggregatedBidExtreme() {
		bindAgents(3);
		// Run 1
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {-2,-2,-2,-2,-2,-4,-4,-4,-4,-4,-4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {-1,-1,-1,-1,-1,-1,-1,-3,-3,-3,-3}));
		assertArrayEquals(new double[] {-8,-8,-8,-8,-8,-10,-10,-12,-12,-12,-12}, this.matcher.lastReceivedBid.getDemand(), 0);
		// Run 2
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,2,2,2,2,2,2}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,3,1,1,1,1,1,1}));
		assertArrayEquals(new double[] {12,12,12,12,12,8,8,8,8,8,8}, this.matcher.lastReceivedBid.getDemand(), 0);
		// Run 3
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,0,0,0,0,0,0}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,-5,-5,-5,-5,-5,-5}));
		assertArrayEquals(new double[] {9,9,9,9,9,0,0,0,0,0,0}, this.matcher.lastReceivedBid.getDemand(), 0);
		unbindAgents(3);
	}

	@Test
	public void sendAggregatedBidRejectAscending() {
		bindAgents(4);
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,0,0,0,0,0,0}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,-5,-5,-5,-5,-5,-5}));
		agents[4].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,8,8,8,8,8,8}));
		assertArrayEquals(new double[] {9,9,9,9,9,0,0,0,0,0,0}, this.matcher.lastReceivedBid.getDemand(), 0);
		unbindAgents(4);
	}
	
	@Test
	public void sendAggregatedBidLarge() {
		bindAgents(20);
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,3,3,3,3,3,3,3}));
		agents[3].sendBid(new BidInfo(marketBasis, new double[] {-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2}));
		agents[4].sendBid(new BidInfo(marketBasis, new double[] {1,1,1,1,1,1,1,1,1,1,1}));
		agents[5].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,0,0,0,0,0}));
		agents[6].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,0,0,0,0,0,0}));
		agents[7].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,-4,-4,-4,-4,-4}));
		agents[8].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,0,0,0,0,0,0,0}));
		agents[9].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,-2,-2,-2,-2,-2,-2,-2,-2}));
		agents[10].sendBid(new BidInfo(marketBasis, new double[] {1,1,1,1,1,1,1,0,0,0,0}));
		agents[11].sendBid(new BidInfo(marketBasis, new double[] {7,7,7,7,7,7,7,0,0,0,0}));
		agents[12].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,-6,-6,-6,-6,-6,-6,-6,-6}));
		agents[13].sendBid(new BidInfo(marketBasis, new double[] {8,8,8,8,8,8,8,8,8,8,8}));
		agents[14].sendBid(new BidInfo(marketBasis, new double[] {-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9}));
		agents[15].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,0,0,-8,-8,-8}));
		agents[16].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,4,3,3,3,3,3}));
		agents[17].sendBid(new BidInfo(marketBasis, new double[] {2,2,2,2,1,1,1,1,0,0,0}));
		agents[18].sendBid(new BidInfo(marketBasis, new double[] {-1,-1,-1,-1,-2,-2,-2,-2,-3,-3,-3}));
		agents[19].sendBid(new BidInfo(marketBasis, new double[] {6,6,6,6,6,6,0,0,0,0,0}));
		assertArrayEquals(new double[] {29,29,29,21,16,11,0,-8,-18,-18,-18}, this.matcher.lastReceivedBid.getDemand(), 0);
		unbindAgents(20);
	}
	

/*
TODO: The behavior tested in this test is outside the scope of this version
	@Test
	public void sendAggregatedBidLargeRejectAscending() {
		bindAgents(21);
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,3,3,3,3,3,3,3}));
		agents[3].sendBid(new BidInfo(marketBasis, new double[] {-2,-2,-2,-2,-2,-2,-2,-2,-2,-2,-2}));
		agents[4].sendBid(new BidInfo(marketBasis, new double[] {1,1,1,1,1,1,1,1,1,1,1}));
		agents[5].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,0,0,0,0,0}));
		agents[6].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,0,0,0,0,0,0}));
		agents[7].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,-4,-4,-4,-4,-4}));
		agents[8].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,0,0,0,0,0,0,0}));
		agents[9].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,-2,-2,-2,-2,-2,-2,-2,-2}));
		agents[10].sendBid(new BidInfo(marketBasis, new double[] {1,1,1,1,1,1,1,0,0,0,0}));
		agents[11].sendBid(new BidInfo(marketBasis, new double[] {7,7,7,7,7,7,7,0,0,0,0}));
		agents[12].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,-6,-6,-6,-6,-6,-6,-6,-6}));
		agents[13].sendBid(new BidInfo(marketBasis, new double[] {8,8,8,8,8,8,8,8,8,8,8}));
		agents[14].sendBid(new BidInfo(marketBasis, new double[] {-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9}));
		agents[15].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,0,0,-8,-8,-8}));
		agents[16].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,4,3,3,3,3,3}));
		agents[17].sendBid(new BidInfo(marketBasis, new double[] {2,2,2,2,1,1,1,1,0,0,0}));
		agents[18].sendBid(new BidInfo(marketBasis, new double[] {-1,-1,-1,-1,-2,-2,-2,-2,-3,-3,-3}));
		agents[19].sendBid(new BidInfo(marketBasis, new double[] {6,6,6,6,6,6,0,0,0,0,0}));
		agents[20].sendBid(new BidInfo(marketBasis, new double[] {-5,-5,-5,-5,-5,8,8,8,8,8,8}));
		assertArrayEquals(new double[] {29,29,29,21,16,11,0,-8,-18,-18,-18}, this.matcher.lastReceivedBid.getDemand(), 0);
		unbindAgents(21);
	}
	*/
}
