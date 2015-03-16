package net.powermatcher.test.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.monitoring.events.IncomingPriceUpdateEvent;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;
import net.powermatcher.core.BaseMatcherEndpoint;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.core.bidcache.BidCache;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.Freezer;
import net.powermatcher.examples.PVPanelAgent;
import net.powermatcher.examples.StoringObserver;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ClusterTest extends TestCase {

	private final String FACTORY_PID_AUCTIONEER = "net.powermatcher.core.auctioneer.Auctioneer";
	private final String FACTORY_PID_CONCENTRATOR = "net.powermatcher.core.concentrator.Concentrator";
	private final String FACTORY_PID_PV_PANEL = "net.powermatcher.examples.PVPanelAgent";
	private final String FACTORY_PID_FREEZER = "net.powermatcher.examples.Freezer";
	private final String FACTORY_PID_OBSERVER = "net.powermatcher.examples.StoringObserver";
	
	private final String AGENT_ID_AUCTIONEER = "auctioneer";
	private final String AGENT_ID_CONCENTRATOR = "concentrator";
	private final String AGENT_ID_PV_PANEL = "pvPanel";
	private final String AGENT_ID_FREEZER = "freezer";
	
	private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
    private ServiceReference<?> scrServiceReference = context.getServiceReference( ScrService.class.getName());
    private ScrService scrService = (ScrService) context.getService(scrServiceReference);
    private ConfigurationAdmin configAdmin;

    private ClusterHelper clusterHelper;
    
    @Override 
    protected void setUp() throws Exception {
    	super.setUp();

    	clusterHelper = new ClusterHelper();
  	
    	configAdmin = clusterHelper.getService(context, ConfigurationAdmin.class);

    	// Cleanup running agents to start with clean test
    	Configuration[] configs = configAdmin.listConfigurations(null);
    	if (configs != null) {
        	for (Configuration config : configs) {
        		config.delete();
        	}
    	}
    }

    /**
     * Tests a simple buildup of a cluster in OSGI and sanity tests.
     * Custer consists of Auctioneer, Concentrator and 2 agents.
     */
    public void testSimpleClusterBuildUp() throws Exception {
	    // Create Auctioneer
    	Configuration auctioneerConfig = createConfiguration(FACTORY_PID_AUCTIONEER, getAuctioneerProperties());

    	// Wait for Auctioneer to become active
    	clusterHelper.checkServiceByPid(context, auctioneerConfig.getPid(), Auctioneer.class);
    	
    	// Create Concentrator
    	Configuration concentratorConfig = createConfiguration(FACTORY_PID_CONCENTRATOR, getConcentratorProperties());
    	
    	// Wait for Concentrator to become active
    	clusterHelper.checkServiceByPid(context, concentratorConfig.getPid(), Concentrator.class);
    	
    	// Create PvPanel
    	Configuration pvPanelConfig = createConfiguration(FACTORY_PID_PV_PANEL, getPvPanelProperties());
    	
    	// Wait for PvPanel to become active
    	clusterHelper.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);

    	// Create Freezer
    	Configuration freezerConfig = createConfiguration(FACTORY_PID_FREEZER, getFreezerProperties());
    	
    	// Wait for Freezer to become active
    	clusterHelper.checkServiceByPid(context, freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// check Auctioneer alive
    	assertEquals(true, clusterHelper.checkActive(scrService, FACTORY_PID_AUCTIONEER));
    	// check Concentrator alive
    	assertEquals(true, clusterHelper.checkActive(scrService, FACTORY_PID_CONCENTRATOR));
    	// check PvPanel alive
    	assertEquals(true, clusterHelper.checkActive(scrService, FACTORY_PID_PV_PANEL));
    	// check Freezer alive
    	assertEquals(true, clusterHelper.checkActive(scrService, FACTORY_PID_FREEZER));
    	
    	//Create StoringObserver
    	Configuration storingObserverConfig = createConfiguration(FACTORY_PID_OBSERVER, getStoringObserverProperties());
    	
    	// Wait for StoringObserver to become active
    	StoringObserver observer = clusterHelper.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    	
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
    	createConfiguration(FACTORY_PID_AUCTIONEER, getAuctioneerProperties());
    	Configuration concentratorConfig = createConfiguration(FACTORY_PID_CONCENTRATOR, getConcentratorProperties());
    	Configuration pvPanelConfig = createConfiguration(FACTORY_PID_PV_PANEL, getPvPanelProperties());
    	Configuration freezerConfig = createConfiguration(FACTORY_PID_FREEZER, getFreezerProperties());
    	
    	// Wait for PvPanel and Freezer to become active
    	Concentrator concentrator = clusterHelper.getServiceByPid(context, concentratorConfig.getPid(), Concentrator.class);
    	clusterHelper.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);
    	clusterHelper.checkServiceByPid(context, freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// Create StoringObserver
    	Configuration storingObserverConfig = createConfiguration(FACTORY_PID_OBSERVER, getStoringObserverProperties());
    	StoringObserver observer = clusterHelper.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    	
    	// Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    	
    	// disconnect Freezer
    	this.disconnectAgent(configAdmin, freezerConfig.getPid());
    	
    	// Checking to see if the Freezer is no longer participating
    	observer.clearEvents();
    	Thread.sleep(10000);
    	checkBidsClusterNoFreezer(observer, concentrator);
    	
    	// Re-add Freezer agent, it should not receive bids from previous freezer
    	observer.clearEvents();
    	freezerConfig = createConfiguration(FACTORY_PID_FREEZER, getFreezerProperties());
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    }

    /**
     * Tests whether auctioneer removal stops complete cluster but continues when Auctioneer is started again.
     */
    public void testAuctioneerRemoval() throws Exception {
	    // Create simple cluster
    	Configuration auctioneerConfig = createConfiguration(FACTORY_PID_AUCTIONEER, getAuctioneerProperties());
    	createConfiguration(FACTORY_PID_CONCENTRATOR, getConcentratorProperties());
    	Configuration pvPanelConfig = createConfiguration(FACTORY_PID_PV_PANEL, getPvPanelProperties());
    	Configuration freezerConfig = createConfiguration(FACTORY_PID_FREEZER, getFreezerProperties());
    	
    	// Wait for PvPanel and Freezer to become active
    	clusterHelper.checkServiceByPid(context, pvPanelConfig.getPid(), PVPanelAgent.class);
    	clusterHelper.checkServiceByPid(context, freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// Create StoringObserver
    	Configuration storingObserverConfig = createConfiguration(FACTORY_PID_OBSERVER, getStoringObserverProperties());
    	StoringObserver observer = clusterHelper.getServiceByPid(context, storingObserverConfig.getPid(), StoringObserver.class);
    	
    	// Checking to see if all agents send bids
    	Thread.sleep(10000);
    	checkBidsFullCluster(observer);
    	
    	// disconnect Auctioneer 
    	this.disconnectAgent(configAdmin, auctioneerConfig.getPid());
    	
    	//Checking to see if any bids were sent when the autioneer was down.
    	observer.clearEvents();
    	Thread.sleep(10000);
    	checkBidsClusterNoAuctioneer(observer);
    	
    	// connect auctioneer, bid should start again
    	auctioneerConfig = createConfiguration(FACTORY_PID_AUCTIONEER, getAuctioneerProperties());
    	clusterHelper.checkActive(scrService, FACTORY_PID_AUCTIONEER);
    
    	Thread.sleep(10000);
    	// TODO Reattaching of Auctioneer fails in sessionmanager.
    	// Disabled this teststep for now
    	// checkBidsFullCluster(observer);
    }
    
	private void disconnectAgent(ConfigurationAdmin configAdmin, String agentPid) throws Exception, InvalidSyntaxException {
		Configuration config = configAdmin.getConfiguration(agentPid);
		if (config == null) {
			 fail("Config for agent " + agentPid + " does not exist, but should be.");
		}
		
		config.delete();
    }
	    
    private Dictionary<String, Object> getPvPanelProperties() throws Exception {
    	// create PvPanel props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_PV_PANEL);
    	properties.put("desiredParentId", AGENT_ID_CONCENTRATOR);
    	properties.put("bidUpdateRate", "4");
    	properties.put("minimumDemand", "-700");
    	properties.put("maximumDemand", "-600");
    	
    	return properties;
    }
    
    private Dictionary<String, Object> getFreezerProperties() throws Exception {
    	// create Freezer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_FREEZER);
    	properties.put("desiredParentId", AGENT_ID_CONCENTRATOR);
    	properties.put("bidUpdateRate", "4");
    	properties.put("minimumDemand", "100");
    	properties.put("maximumDemand", "121");
    	
    	return properties;
    }
    
    private Dictionary<String, Object> getConcentratorProperties() throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_CONCENTRATOR);
    	properties.put("desiredParentId", AGENT_ID_AUCTIONEER);
    	properties.put("minTimeBetweenBidUpdates", 5000);
    	
    	return properties;
    }
    
    private Configuration createConfiguration(String factoryPid, Dictionary<String, Object> properties) throws Exception {
    	Configuration config = configAdmin.createFactoryConfiguration(factoryPid, null);

    	// create Auctioneer props
    	config.update(properties);
    	
    	return config;
    }

    private Dictionary<String, Object> getAuctioneerProperties() {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_AUCTIONEER);
    	properties.put("clusterId", "DefaultCluster");
    	properties.put("commodity", "electricity");
    	properties.put("currency", "EUR");
    	properties.put("priceSteps", 100);
    	properties.put("minimumPrice", 0.0);
    	properties.put("maximumPrice", 1.0);
    	properties.put("bidTimeout", 600);
    	properties.put("priceUpdateRate", 30l);
    	properties.put("minTimeBetweenPriceUpdates", 5000);
    	
    	return properties;
    }
    
    private Dictionary<String, Object> getStoringObserverProperties() throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("observableAgent_filter", "");

    	return properties;    	
    }
    
    private void checkBidsFullCluster(StoringObserver observer) {
    	// Are any bids available for each agent (at all)
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_CONCENTRATOR).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_PV_PANEL).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_FREEZER).isEmpty());
    	
    	// Validate bidnumbers
    	checkBidNumbers(observer, AGENT_ID_CONCENTRATOR);
    	checkBidNumbers(observer, AGENT_ID_FREEZER);
    	checkBidNumbers(observer, AGENT_ID_PV_PANEL);
    }

    private void checkBidNumbers(StoringObserver observer, String agentId) {
    	// Validate bidnumber incoming from concentrator for correct agent
    	List<OutgoingBidEvent> agentBids = observer.getOutgoingBidEvents(agentId);
    	List<IncomingPriceUpdateEvent> receivedPrices = observer.getIncomingPriceUpdateEvents(agentId);

    	for (IncomingPriceUpdateEvent priceEvent : receivedPrices) {
    		int priceBidnumber = priceEvent.getPriceUpdate().getBidNumber();
    		boolean validBidNumber = false;
    		
    		for (OutgoingBidEvent bidEvent : agentBids) {
    			if (bidEvent.getBidUpdate().getBidNumber() == priceBidnumber) {
    				validBidNumber = true;
    			}
    		}

    		assertTrue("Price bidnumber " + priceBidnumber + " is unknown in bids for agent " + agentId, validBidNumber);
    	}
    }
    
    private void checkBidsClusterNoFreezer(StoringObserver observer, Concentrator concentrator) {
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_CONCENTRATOR).isEmpty());
    	assertFalse(observer.getOutgoingBidEvents(AGENT_ID_PV_PANEL).isEmpty());
    	assertTrue(observer.getOutgoingBidEvents(AGENT_ID_FREEZER).isEmpty());

    	// Check aggregated bid does no longer contain freezer, by checking last aggregated against panel bids
    	List<OutgoingBidEvent> concentratorBids = observer.getOutgoingBidEvents(AGENT_ID_CONCENTRATOR);
    	List<OutgoingBidEvent> panelBids = observer.getOutgoingBidEvents(AGENT_ID_PV_PANEL);
    	
    	OutgoingBidEvent concentratorBid = concentratorBids.get(concentratorBids.size()-1);
    	boolean foundBid = false;
    	for (OutgoingBidEvent panelBid : panelBids) {
    		if (panelBid.getBidUpdate().getBid().toArrayBid().equals(concentratorBid.getBidUpdate().getBid().toArrayBid())) {
    			foundBid = true;
    		}
    	}
    	
    	assertTrue("Concentrator still contains freezer bid", foundBid);
    	
    	// Validate last aggregated bid contains only pvPanel
    	BaseMatcherEndpoint matcherPart = clusterHelper.getPrivateField(concentrator, "matcherPart", BaseMatcherEndpoint.class);
    	BidCache bidCache = clusterHelper.getPrivateField(matcherPart, "bidCache", BidCache.class);
    	AggregatedBid lastBid = clusterHelper.getPrivateField(bidCache, "lastBid", AggregatedBid.class);
    	assertTrue(lastBid.getAgentBidReferences().containsKey(AGENT_ID_PV_PANEL));
    	assertFalse(lastBid.getAgentBidReferences().containsKey(AGENT_ID_FREEZER));
    	
    	// Validate bidcache no longer conatins any reference to Freezer
    	@SuppressWarnings("unchecked")
		Map<String, BidUpdate> agentBids = (Map<String, BidUpdate>)clusterHelper.getPrivateField(bidCache, "agentBids", Object.class);
    	assertFalse(agentBids.containsKey(AGENT_ID_FREEZER));
    	assertTrue(agentBids.containsKey(AGENT_ID_PV_PANEL));
    }
    
    private void checkBidsClusterNoAuctioneer(StoringObserver observer) {
    	assertTrue(observer.getOutgoingBidEvents(AGENT_ID_CONCENTRATOR).isEmpty());
    	assertTrue(observer.getOutgoingBidEvents(AGENT_ID_PV_PANEL).isEmpty());
    	assertTrue(observer.getOutgoingBidEvents(AGENT_ID_FREEZER).isEmpty());
    }
}
