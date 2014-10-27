package net.powermatcher.core.test;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Properties;
import java.util.zip.DataFormatException;

import net.powermatcher.core.agent.framework.service.DownMessagable;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SendReceiveBidTestCBQ1 extends ResilienceTest {
	private AuctioneerWrapper auctioneer;
	private ConcentratorWrapper concentrator;
	
	/**
	 * Set up the send and receive test by creating an auctioneer and
	 * a concentrator. The agents will send their bids to the concentrator.
	 * The latter will forward the aggregated bid to the auctioneer.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUpSendReceiveTest() throws Exception {
		
		// Create the auctioneer
		this.auctioneer = new AuctioneerWrapper();
		Properties auctioneerProperties = new Properties();
		auctioneerProperties.setProperty("id", "auctioneer");
		auctioneerProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.auctioneer.setConfiguration(new BaseConfiguration(auctioneerProperties));
		this.matchers.add(this.auctioneer);
		
		// Create the concentrator
		Properties concentratorProperties = new Properties();
		concentratorProperties.setProperty("id", "concentrator");
		concentratorProperties.setProperty(ActiveObjectConfiguration.UPDATE_INTERVAL_PROPERTY, "0");
		this.concentrator = new ConcentratorWrapper(new BaseConfiguration(concentratorProperties));
		this.matchers.add(this.concentrator);
		
		// Bind auctioneer and concentrator
		this.auctioneer.bind((DownMessagable) this.concentrator);
		this.concentrator.bind((UpMessagable) this.auctioneer);
		
		// Set the matcher agent for the agents
		this.matcherAgent = this.concentrator;
	}
	
	/**
	 * Sending an invalid bid in an agent hierarchy is difficult
	 * because the creation of a bid info instance prohibits this.
	 * 
	 * Test 1: Create a bid with a demand array that is too long.
	 * 
	 * Expected result: BidInfo constructor generates an InvalidParameterException
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test(expected=InvalidParameterException.class)
	public void createInvalidBid1CPF1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CBQ/CBQ1/Test1", null);

		this.bidReader.nextBidInfo();
	}

	/**
	 * Sending an invalid bid in an agent hierarchy is difficult
	 * because the creation of a bid info instance prohibits this.
	 * 
	 * Test 2: Create a bid with a demand array that is too short.
	 * 
	 * Expected result: BidInfo constructor generates an InvalidParameterException
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test(expected=InvalidParameterException.class)
	public void createInvalidBid2CPF1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CBQ/CBQ1/Test2", null);

		this.bidReader.nextBidInfo();
	}
	
	/**
	 * Sending an invalid bid in an agent hierarchy is difficult
	 * because the creation of a bid info instance prohibits this.
	 * 
	 * Test 3: Create a bid with a demand array that is too short.
	 * 
	 * Expected result: BidInfo constructor generates an InvalidParameterException
	 * 
	 * @throws IOException
	 * @throws DataFormatException
	 */
	@Test(expected=InvalidParameterException.class)
	public void createInvalidBid3CPF1() throws IOException, DataFormatException {
			
		// Prepare the test for reading test input
		prepareTest("CBQ/CBQ1/Test3", null);

		this.bidReader.nextBidInfo();
	}
	
	@After
	public void tearDownSendReceive() throws Exception {
		this.auctioneer.unbind((DownMessagable) this.concentrator);
		this.concentrator.unbind((UpMessagable) this.auctioneer);
	}
		
}
