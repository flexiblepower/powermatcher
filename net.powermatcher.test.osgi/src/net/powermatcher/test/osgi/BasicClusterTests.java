package net.powermatcher.test.osgi;

import java.util.List;
import java.util.Map;

import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidUpdateEvent;
import net.powermatcher.core.BaseMatcherEndpoint;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.Freezer;
import net.powermatcher.examples.PVPanelAgent;
import net.powermatcher.examples.StoringObserver;

import org.osgi.service.cm.Configuration;

/**
 * Basic cluster tests and tests buildup and agent removal.
 * 
 * @author FAN
 * @version 2.0
 */
public class BasicClusterTests extends OsgiTestCase {

	private Configuration auctioneerConfig;
	
	private Concentrator concentrator;
	
	private Configuration concentratorConfig;
	
	private Configuration pvPanelConfig;
	
	private Configuration freezerConfig;
	
	private StoringObserver observer;
	
    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests.
     * Cluster consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testSimpleClusterBuildUp() throws Exception {
    	// Create simple cluster
    	setupCluster();
    	
    	//Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }

    /**
     * Tests whether agent removal actually makes the bid obsolete of this agent
     * The agent should also not receive any price updates.
     */
    public void testAgentRemoval() throws Exception {
    	// Create simple cluster
    	setupCluster();
    	
    	// Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    	
    	// disconnect Freezer
    	clusterHelper.disconnectAgent(configAdmin, freezerConfig.getPid());
    	
    	// Checking to see if the Freezer is no longer participating
    	observer.clearEvents();
    	Thread.sleep(10000);
    	checkBidsClusterNoFreezer(observer, concentrator);
    	
    	// Re-add Freezer agent, it should not receive bids from previous freezer
    	observer.clearEvents();
    	freezerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_FREEZER, 
    			clusterHelper.getFreezerProperties(clusterHelper.AGENT_ID_FREEZER , clusterHelper.AGENT_ID_CONCENTRATOR , 4));
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }

    /**
     * Tests whether auctioneer removal stops complete cluster but continues when Auctioneer is started again.
     */
    public void testAuctioneerRemoval() throws Exception {
    	// Create simple cluster
    	setupCluster();
    	
    	// Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    	
    	// disconnect Auctioneer 
    	clusterHelper.disconnectAgent(configAdmin, auctioneerConfig.getPid());
    	
    	//Checking to see if any bids were sent when the autioneer was down.
    	observer.clearEvents();
    	Thread.sleep(10000);
    	checkBidsClusterNoAuctioneer(observer);
    	
    	// connect auctioneer, bid should start again
    	auctioneerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_AUCTIONEER, 
    			clusterHelper.getAuctioneerProperties(clusterHelper.AGENT_ID_AUCTIONEER, 5000));
    	clusterHelper.checkActive(scrService, clusterHelper.FACTORY_PID_AUCTIONEER);
    
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }
    
    /**
     * Disconnect Concentrator and reconnect Concentrator. Check if agents will receive bidUpdates again.
     * Cluster consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testConcentratorRemoval() throws Exception {
	    // Create simple cluster
    	setupCluster();
    	
    	// Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    	
    	// disconnect Concentrator 
    	clusterHelper.disconnectAgent(configAdmin, concentratorConfig.getPid());
    	
    	// Checking to see if any bids were sent when the concentrator was down.
    	observer.clearEvents();
    	Thread.sleep(10000);
    	
    	checkBidsClusterNoConcentrator(observer);
    	
    	// Connect concentrator, bid should start again
    	concentratorConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_CONCENTRATOR, 
    			clusterHelper.getConcentratorProperties(clusterHelper.AGENT_ID_CONCENTRATOR, clusterHelper.AGENT_ID_AUCTIONEER, 5000));
    	clusterHelper.checkActive(scrService, clusterHelper.FACTORY_PID_CONCENTRATOR);
    
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }
    
    private void setupCluster() throws Exception {
    	// Create Auctioneer
    	auctioneerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_AUCTIONEER, 
    			clusterHelper.getAuctioneerProperties(clusterHelper.AGENT_ID_AUCTIONEER, 5000));

    	// Wait for Auctioneer to become active
    	clusterHelper.checkServiceByPid(context, auctioneerConfig.getPid(), Auctioneer.class);
    	
    	// Create Concentrator
    	concentratorConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_CONCENTRATOR, 
    			clusterHelper.getConcentratorProperties(clusterHelper.AGENT_ID_CONCENTRATOR, clusterHelper.AGENT_ID_AUCTIONEER, 5000));
    	
    	// Wait for Concentrator to become active
    	concentrator = clusterHelper.getServiceByPid(context, concentratorConfig.getPid(), Concentrator.class);
    	
    	// Create PvPanel
    	pvPanelConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_PV_PANEL, 
    			clusterHelper.getPvPanelProperties(clusterHelper.AGENT_ID_PV_PANEL, clusterHelper.AGENT_ID_CONCENTRATOR, 4));
    	
    	// Wait for PvPanel to become active
    	clusterHelper.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);

    	// Create Freezer
    	freezerConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_FREEZER, 
    			clusterHelper.getFreezerProperties(clusterHelper.AGENT_ID_FREEZER, clusterHelper.AGENT_ID_CONCENTRATOR, 4));
    	
    	// Wait for Freezer to become active
    	clusterHelper.checkServiceByPid(context, freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// check Auctioneer alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.FACTORY_PID_AUCTIONEER));
    	// check Concentrator alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.FACTORY_PID_CONCENTRATOR));
    	// check PvPanel alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.FACTORY_PID_PV_PANEL));
    	// check Freezer alive
    	assertEquals(true, clusterHelper.checkActive(scrService, clusterHelper.FACTORY_PID_FREEZER));
    	
    	//Create StoringObserver
    	Configuration storingObserverConfig = clusterHelper.createConfiguration(configAdmin, clusterHelper.FACTORY_PID_OBSERVER, clusterHelper.getStoringObserverProperties());
    	
    	// Wait for StoringObserver to become active
    	observer = clusterHelper.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    }
    
    private void checkBidsFullCluster(StoringObserver observer) {
    	// Are any bids available for each agent (at all)
    	assertFalse(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
    	assertFalse(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_PV_PANEL).isEmpty());
    	assertFalse(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_FREEZER).isEmpty());
    	
    	// Validate bidnumbers
    	checkBidNumbers(observer, clusterHelper.AGENT_ID_CONCENTRATOR);
    	checkBidNumbers(observer, clusterHelper.AGENT_ID_FREEZER);
    	checkBidNumbers(observer, clusterHelper.AGENT_ID_PV_PANEL);
    }

    private void checkBidNumbers(StoringObserver observer, String agentId) {
    	// Validate bidnumber incoming from concentrator for correct agent
    	List<OutgoingBidUpdateEvent> agentBids = observer.getOutgoingBidUpdateEvents(agentId);
    	List<IncomingPriceUpdateEvent> receivedPrices = observer.getIncomingPriceUpdateEvents(agentId);

    	for (IncomingPriceUpdateEvent priceEvent : receivedPrices) {
    		int priceBidnumber = priceEvent.getPriceUpdate().getBidNumber();
    		boolean validBidNumber = false;
    		
    		for (OutgoingBidUpdateEvent bidEvent : agentBids) {
    			if (bidEvent.getBidUpdate().getBidNumber() == priceBidnumber) {
    				validBidNumber = true;
    			}
    		}

    		assertTrue("Price bidnumber " + priceBidnumber + " is unknown in bids for agent " + agentId, validBidNumber);
    	}
    }
    
    private void checkBidsClusterNoFreezer(StoringObserver observer, Concentrator concentrator) {
    	assertFalse(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
    	assertFalse(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_PV_PANEL).isEmpty());
    	assertTrue(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_FREEZER).isEmpty());

    	// Check aggregated bid does no longer contain freezer, by checking last aggregated against panel bids
    	List<OutgoingBidUpdateEvent> concentratorBids = observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_CONCENTRATOR);
    	List<OutgoingBidUpdateEvent> panelBids = observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_PV_PANEL);
    	
    	OutgoingBidUpdateEvent concentratorBid = concentratorBids.get(concentratorBids.size()-1);
    	boolean foundBid = false;
    	for (OutgoingBidUpdateEvent panelBid : panelBids) {
    		if (panelBid.getBidUpdate().getBid().toArrayBid().equals(concentratorBid.getBidUpdate().getBid().toArrayBid())) {
    			foundBid = true;
    		}
    	}
    	
    	assertTrue("Concentrator still contains freezer bid", foundBid);
    	
    	// Validate last aggregated bid contains only pvPanel
    	BaseMatcherEndpoint matcherPart = clusterHelper.getPrivateField(concentrator, "matcherPart", BaseMatcherEndpoint.class);
    	BidCache bidCache = clusterHelper.getPrivateField(matcherPart, "bidCache", BidCache.class);
    	AggregatedBid lastBid = clusterHelper.getPrivateField(bidCache, "lastBid", AggregatedBid.class);
    	assertTrue(lastBid.getAgentBidReferences().containsKey(clusterHelper.AGENT_ID_PV_PANEL));
    	assertFalse(lastBid.getAgentBidReferences().containsKey(clusterHelper.AGENT_ID_FREEZER));
    	
    	// Validate bidcache no longer conatins any reference to Freezer
    	@SuppressWarnings("unchecked")
		Map<String, BidUpdate> agentBids = (Map<String, BidUpdate>)clusterHelper.getPrivateField(bidCache, "agentBids", Object.class);
    	assertFalse(agentBids.containsKey(clusterHelper.AGENT_ID_FREEZER));
    	assertTrue(agentBids.containsKey(clusterHelper.AGENT_ID_PV_PANEL));
    }
    
    private void checkBidsClusterNoAuctioneer(StoringObserver observer) {
    	assertTrue(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_CONCENTRATOR).isEmpty());
    	assertTrue(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_PV_PANEL).isEmpty());
    	assertTrue(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_FREEZER).isEmpty());
    }
    
    private void checkBidsClusterNoConcentrator(StoringObserver observer) {
    	assertTrue(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_PV_PANEL).isEmpty());
    	assertTrue(observer.getOutgoingBidUpdateEvents(clusterHelper.AGENT_ID_FREEZER).isEmpty());
    }
}
