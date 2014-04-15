package net.powermatcher.core.agent.auctioneer.test;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import net.powermatcher.core.agent.auctioneer.Auctioneer;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test for the Auctioneer
 * 
 * Every test requires a different number agents. In setUp() NR_AGENTS are 
 * instantiated. Every test the desired number of agents can be binded and 
 * unbinded using the functions bindAgents() and unbindAgents().
 */
public class AuctioneerUnitTest {

	private final static int NR_AGENTS = 21;
	private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 11, 0, 10, 1, 0);

	private Auctioneer auctioneer;
	private MockAgent[] agents;

	@Before
	public void setUp() throws Exception {
		// Init Auctioneer
		this.auctioneer = new Auctioneer();
		Properties auctioneerProperties = new Properties();
		auctioneerProperties.setProperty("id", "auctioneer");
		auctioneerProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.auctioneer.setConfiguration(new BaseConfiguration(auctioneerProperties));
		this.auctioneer.updateMarketBasis(marketBasis);

		// Init MockAgents
		this.agents = new MockAgent[NR_AGENTS];
		for(int i = 0; i < NR_AGENTS; i++) {
			MockAgent newAgent = new MockAgent("agent" + (i+1));
			newAgent.updateMarketBasis(marketBasis);
			agents[i] = newAgent;
		}
	}

	private void bindAgents(int number) {
		for(int i = 0; i < number; i++) {
			this.auctioneer.bind(this.agents[i]);
			this.agents[i].bind(this.auctioneer);
		}
	}

	private void unbindAgents(int number) {
		for(int i = 0; i < number; i++) {
			this.auctioneer.unbind(this.agents[i]);
			this.agents[i].unbind(this.auctioneer);
		}
	}

	@Test
	public void noEquilibriumOnDemandSide() {
		bindAgents(3);
		// run 1
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,4,4,4,4,4,4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,3,3,3,3,3,3,3}));
		assertEquals(10, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		// run 2
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,2,2,2,2,2,2}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {3,3,3,3,3,1,1,1,1,1,1}));
		assertEquals(10, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		unbindAgents(3);
	}

	@Test
	public void noEquilibriumOnSupplySide() {
		bindAgents(3);
		// run 1
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {-4,-4,-4,-4,-4,-4,-4,-4,-4,-4,-4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {-3,-3,-3,-3,-3,-3,-3,-3,-3,-3,-3}));
		assertEquals(0, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		// run 2
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {-2,-2,-2,-2,-2,-4,-4,-4,-4,-4,-4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {-1,-1,-1,-1,-1,-1,-1,-3,-3,-3,-3}));
		assertEquals(0, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		unbindAgents(3);
	}

	@Test
	public void equilibriumSmallNumberOfBids() {
		bindAgents(3);
		// run 1
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,0,0,0,0,0,0}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,-5,-5,-5,-5,-5,-5}));
		assertEquals(5, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		// run 2
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,-5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,0,0,-4,-4,-4,-4}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {9,9,9,9,9,9,9,9,9,9,9}));
		assertEquals(7, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		unbindAgents(3);
	}

	@Test
	public void equilibriumLargeSet() {
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
		assertEquals(6, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		unbindAgents(20);
	}

	@Test
	public void equilibriumLargerSet() {
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
		agents[20].sendBid(new BidInfo(marketBasis, new double[] {8,8,8,8,8,8,8,8,8,8,8}));
		assertEquals(7, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		unbindAgents(21);
	}
	
/*
TODO: The behavior tested in this test is outside the scope of this version
	@Test
	public void rejectBid() {
		bindAgents(4);
		agents[0].sendBid(new BidInfo(marketBasis, new double[] {5,5,5,5,5,5,5,5,5,5,5}));
		agents[1].sendBid(new BidInfo(marketBasis, new double[] {4,4,4,4,4,0,0,0,0,0,0}));
		agents[2].sendBid(new BidInfo(marketBasis, new double[] {0,0,0,0,0,-5,-5,-5,-5,-5,-5}));
		agents[3].sendBid(new BidInfo(marketBasis, new double[] {-9,-9,-9,-9, -9,1,1,1,1,1,1}));
		assertEquals(5, agents[0].lastPriceUpdate.getCurrentPrice(), 0);
		unbindAgents(4);
	}
*/
}
