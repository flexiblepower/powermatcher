package net.powermatcher.integration.oscillation;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockScheduler;

import org.junit.Before;
import org.junit.Test;

/**
 * This test is created to assert that features added to prevent oscillating
 * behavior are in place and work. Oscillating behavior can emerge when an Agent
 * sends Bids asynchronously from the prices of the Auctioneer. If the Agent
 * cannot ascertain which Price is is corresponding to which Bid, it could start
 * sending Bids in response to the wrong Prices.
 * 
 * @author FAN
 * @version 2.0
 */
public class OscillationPreventionTest {

	private static final String AUCTIONEER_ID = "auctioneer";
	private static final String CONCENTRATOR_ID = "concentrator";
	private static final String AGENT_ID = "agent";

	// This needs to be the same as the MarketBasis created in the Auctioneer
	private final MarketBasis marketBasis = new MarketBasis("electricity",
			"EUR", 10, 0, 10);
	private Map<String, Object> auctioneerProperties;
	private Map<String, Object> concentratorProperties;
	private MockScheduler auctioneerScheduler;
	private MockScheduler concentratorScheduler;

	private Auctioneer auctioneer;
	private Concentrator concentrator;
	private MockAgent agent1;
	private MockAgent agent2;
	private MockAgent agent3;

	private SessionManager sessionManager;

	@Before
	public void setUpCluster() {
		// Create auctioneer
		this.auctioneer = new Auctioneer();
		auctioneerProperties = new HashMap<String, Object>();
		auctioneerProperties.put("agentId", AUCTIONEER_ID);
		auctioneerProperties.put("clusterId", "DefaultCluster");
		auctioneerProperties.put("commodity", "electricity");
		auctioneerProperties.put("currency", "EUR");
		auctioneerProperties.put("priceSteps", "10");
		auctioneerProperties.put("minimumPrice", "0");
		auctioneerProperties.put("maximumPrice", "10");
		auctioneerProperties.put("bidTimeout", "600");
		auctioneerProperties.put("priceUpdateRate", "1");

		auctioneerScheduler = new MockScheduler();

		auctioneer.setExecutorService(auctioneerScheduler);
		auctioneer.setTimeService(new SystemTimeService());
		auctioneer.activate(auctioneerProperties);

		// create concentrator
		concentrator = new Concentrator();
		concentratorProperties = new HashMap<String, Object>();
		concentratorProperties.put("matcherId", CONCENTRATOR_ID);
		concentratorProperties.put("desiredParentId", AUCTIONEER_ID);
		concentratorProperties.put("bidTimeout", "600");
		concentratorProperties.put("bidUpdateRate", "30");
		concentratorProperties.put("agentId", CONCENTRATOR_ID);
		concentratorProperties.put("whiteListAgents", new ArrayList<String>());

		concentratorScheduler = new MockScheduler();
		concentrator.setExecutorService(concentratorScheduler);
		concentrator.setTimeService(new SystemTimeService());
		concentrator.activate(concentratorProperties);

		// create agents
		agent1 = new MockAgent(AGENT_ID + "1");
		agent1.setDesiredParentId(CONCENTRATOR_ID);
		agent2 = new MockAgent(AGENT_ID + "2");
		agent2.setDesiredParentId(CONCENTRATOR_ID);
		agent3 = new MockAgent(AGENT_ID + "3");
		agent3.setDesiredParentId(CONCENTRATOR_ID);

		// create sessionManager
		sessionManager = new SessionManager();
		sessionManager.addMatcherEndpoint(auctioneer);
		sessionManager.addAgentEndpoint(concentrator);
		sessionManager.addMatcherEndpoint(concentrator);
		sessionManager.addAgentEndpoint(agent1);
		sessionManager.addAgentEndpoint(agent2);
		sessionManager.addAgentEndpoint(agent3);
		sessionManager.activate();
	}

	/**
	 * This uses the cluster in a optimal way. The Agent sends a bid, the
	 * concentrator aggregates it and sends it to the auctioneer, which
	 * generates a price and sends this back. Agent should ascertain which bid
	 * the price belongs to.
	 */
	@Test
	public void synchrousUpdateTestSimple() {
		// create and send bid by agent
		int bidNumber = 1;
		double[] demandArray = new double[] { 5, 4, 3, 2, 1, 0, -1, -2, -3, -4 };
		ArrayBid bid = new ArrayBid(marketBasis, bidNumber, demandArray);
		agent1.sendBid(bid);

		// concentrator aggregate and send bid
		concentratorDoBidUpdate();

		// auctioneer generate and send price
		auctioneerPublishNewPrice();

		assertThat(agent1.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber)));
	}

	/**
	 * This uses the cluster in a optimal way. The Agent sends a bid, the
	 * concentrator aggregates it and sends it to the auctioneer, which
	 * generates a price and sends this back. Agent should ascertain which bid
	 * the price belongs to.
	 */
	@Test
	public void synchrousUpdateTestMultipleAgents() {
		// create and send bid by agent
		int bidNumber1 = 1;
		int bidNumber2 = 2;
		int bidNumber3 = 3;
		double[] demandArray = new double[] { 5, 4, 3, 2, 1, 0, -1, -2, -3, -4 };
		ArrayBid bid = new ArrayBid(marketBasis, bidNumber1, demandArray);
		agent1.sendBid(bid);
		demandArray = new double[] { 7, 5, 3, 1, -1, -3, -5, -7, -9, -11 };
		bid = new ArrayBid(marketBasis, bidNumber2, demandArray);
		agent2.sendBid(bid);
		demandArray = new double[] { 5, 4, 3, 3, 3, 3, 3, 3, 3, 3 };
		bid = new ArrayBid(marketBasis, bidNumber3, demandArray);
		agent3.sendBid(bid);

		// concentrator aggregate and send bid
		concentratorDoBidUpdate();

		// auctioneer generate and send price
		auctioneerPublishNewPrice();

		assertThat(agent1.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber1)));
		assertThat(agent2.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber2)));
		assertThat(agent3.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber3)));
	}

	/**
	 * This tries to create oscillating behavior. Have the agents, concentrator
	 * and auctioneer sends bids and prices in a different order and see if the
	 * agent still receives the right price.
	 */
	@Test
	public void asynchrousUpdateTest() {
		// create bids
		int bidNumber1 = 1;
		int bidNumber2 = 2;
		int bidNumber3 = 3;
		double[] demand1 = new double[] { 5, 4, 3, 2, 1, 0, -1, -2, -3, -4 };
		ArrayBid bid1 = new ArrayBid(marketBasis, bidNumber1, demand1);
		double[] demand2 = new double[] { 7, 5, 3, 1, -1, -3, -5, -7, -9, -11 };
		ArrayBid bid2 = new ArrayBid(marketBasis, bidNumber2, demand2);
		double[] demand3 = new double[] { 5, 4, 3, 3, 3, 3, 3, 3, 3, 3 };
		ArrayBid bid3 = new ArrayBid(marketBasis, bidNumber3, demand3);

		// sends bids and prices asynchrously
		agent1.sendBid(bid1);
		agent2.sendBid(bid2);

		// agent3 sends bid after concentrator aggregates
		concentratorDoBidUpdate();
		agent3.sendBid(bid3);
		auctioneerPublishNewPrice();

		PriceUpdate lastPriceUpdate1 = agent1.getLastPriceUpdate();
		PriceUpdate lastPriceUpdate2 = agent2.getLastPriceUpdate();

		// agents receive price
		assertThat(lastPriceUpdate1.getPrice(),
				is(equalTo(lastPriceUpdate2.getPrice())));
		assertThat(lastPriceUpdate1.getBidNumber(), is(equalTo(bidNumber1)));
		assertThat(lastPriceUpdate2.getBidNumber(), is(equalTo(bidNumber2)));
		assertThat(agent3.getLastPriceUpdate().getBidNumber(), is(0));

		concentratorDoBidUpdate();
		auctioneerPublishNewPrice();
		PriceUpdate lastPriceUpdate3 = agent3.getLastPriceUpdate();

		// agent3 bid has effect on price
		assertThat(agent1.getLastPriceUpdate().getPrice().getPriceValue(),
				is(not(equalTo(lastPriceUpdate1.getPrice().getPriceValue()))));
		// assertThat(agent1.getLastPriceUpdate().getPrice().getPriceValue(),
		// is(equalTo(2.0)));
		assertThat(agent1.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber1)));
		assertThat(agent2.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber2)));
		assertThat(lastPriceUpdate3.getBidNumber(), is(equalTo(bidNumber3)));

		demand1 = new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
		bid1 = new ArrayBid(marketBasis, ++bidNumber1, demand1);
		agent1.sendBid(bid1);
		demand2 = new double[] { 3, 3, 3, 3, 3, 0, 0, 0, 0, 0 };
		bid2 = new ArrayBid(marketBasis, ++bidNumber2, demand1);
		agent2.sendBid(bid2);

		// agent 1 and 2 send bids,
		lastPriceUpdate1 = agent1.getLastPriceUpdate();
		PriceUpdate oldPrice3 = new PriceUpdate(lastPriceUpdate3.getPrice(),
				lastPriceUpdate3.getBidNumber());
		concentratorDoBidUpdate();
		auctioneerPublishNewPrice();

		// agents have received new price
		PriceUpdate newAgent1Price = agent1.getLastPriceUpdate();
		assertThat(newAgent1Price.getBidNumber(), is(equalTo(bidNumber1)));
		assertThat(newAgent1Price.getPrice().getPriceValue(),
				is(not(equalTo(lastPriceUpdate1.getPrice().getPriceValue()))));
		assertThat(agent2.getLastPriceUpdate().getBidNumber(),
				is(equalTo(bidNumber2)));

		// check agent3 included in new Price
		lastPriceUpdate3 = agent3.getLastPriceUpdate();
		assertThat(lastPriceUpdate3.getBidNumber(), is(equalTo(bidNumber3)));
		assertThat(lastPriceUpdate3.getPrice().getPriceValue(),
				is(not(equalTo(oldPrice3.getPrice().getPriceValue()))));
		assertThat(lastPriceUpdate3.getPrice().getPriceValue(),
				is(equalTo(newAgent1Price.getPrice().getPriceValue())));
	}

	/*
	 * wrapper method to make it more clear what happens during the test. By
	 * calling the doTaskOnce() of the concentratorScheduler, the concentrators
	 * doBidUpdate is called
	 */
	private void concentratorDoBidUpdate() {
		this.concentratorScheduler.doTaskOnce();
	}

	/*
	 * wrapper method to make it more clear what happens during the test. By
	 * calling the doTaskOnce() of the auctioneerScheduler, the auctioneers
	 * publishNewPrice is called
	 */
	private void auctioneerPublishNewPrice() {
		this.auctioneerScheduler.doTaskOnce();
	}

}
