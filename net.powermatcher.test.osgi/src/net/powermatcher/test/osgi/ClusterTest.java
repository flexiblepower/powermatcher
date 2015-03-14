package net.powermatcher.test.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.Freezer;
import net.powermatcher.examples.PVPanelAgent;
import net.powermatcher.examples.StoringObserver;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

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
    
    public void testAddAgentsForCluster() throws Exception {
    	configAdmin = getService(ConfigurationAdmin.class);
    	
	    // Create Auctioneer
    	Configuration auctioneerConfig = createConfifuration(FACTORY_PID_AUCTIONEER, getAuctioneerProperties());

    	// Wait for Auctioneer to become active
    	checkServiceByPid(auctioneerConfig.getPid(), Auctioneer.class);
    	
    	// Create Concentrator
    	Configuration concentratorConfig = createConfifuration(FACTORY_PID_CONCENTRATOR, getConcentratorProperties());
    	
    	// Wait for Concentrator to become active
    	checkServiceByPid(concentratorConfig.getPid(), Concentrator.class);
    	
    	// Create PvPanel
    	Configuration pvPanelConfig = createConfifuration(FACTORY_PID_PV_PANEL, getPvPanelProperties());
    	
    	// Wait for PvPanel to become active
    	checkServiceByPid(pvPanelConfig.getPid(), PVPanelAgent.class);

    	// Create Freezer
    	Configuration freezerConfig = createConfifuration(FACTORY_PID_FREEZER, getFreezerProperties());
    	
    	// Wait for Freezer to become active
    	checkServiceByPid(freezerConfig.getPid(), Freezer.class);

    	// Wait a little time for all components to become satisfied / active
    	Thread.sleep(2000);
    	
    	// check Auctioneer alive
    	assertEquals(true, checkActive(FACTORY_PID_AUCTIONEER));
    	// check Concentrator alive
    	assertEquals(true, checkActive(FACTORY_PID_CONCENTRATOR));
    	// check PvPanel alive
    	assertEquals(true, checkActive(FACTORY_PID_PV_PANEL));
    	// check Freezer alive
    	assertEquals(true, checkActive(FACTORY_PID_FREEZER));
    	
    	//Create StoringObserver
    	Configuration storingObserverConfig = createConfifuration(FACTORY_PID_OBSERVER, getStoringObserverProperties());
    	
    	// Wait for StoringObserver to become active
    	StoringObserver observer = getServiceByPid(storingObserverConfig.getPid(), StoringObserver.class);
    	
    	//Checking to see if all agents send bids
    	Thread.sleep(30000);
    	checkBidsFullCluster(observer);
    	
    	// disconnect Freezer
    	this.disconnectAgent(configAdmin, freezerConfig.getPid());
    	
    	//Checking to see if the Freezer stopped sending bid
    	observer.clearEvents();
    	Thread.sleep(30000);
    	checkBidsClusterNoFreezer(observer);

    	// disconnect Auctioneer 
    	this.disconnectAgent(configAdmin, auctioneerConfig.getPid());
    	
    	//Checking to see if any bids were sent when the autioneer was down.
    	observer.clearEvents();
    	Thread.sleep(30000);
    	checkBidsClusterNoAuctioneer(observer);
    }

	private void disconnectAgent(ConfigurationAdmin configAdmin, String agentPid) throws Exception, InvalidSyntaxException {
		Configuration config = configAdmin.getConfiguration(agentPid);
		if (config == null) {
			 fail("Config for agent " + agentPid + " does not exist, but should be.");
		}
		
		config.delete();
    }
	
	private boolean checkActive(final String factoryPid) throws Exception {
		Component[] components = scrService.getComponents(factoryPid);
		boolean activeAgent = false;
	
		for (Component comp : components) {
			if (comp.getConfigurationPid().equals(factoryPid)) {
				if (comp.getState() == Component.STATE_ACTIVE) {
					activeAgent = true;
				}
			}
		}
		return activeAgent;
	}
    
    private <T> T getService(Class<T> type) throws InterruptedException {
        ServiceTracker<T, T> serviceTracker = 
                new ServiceTracker<T, T>(context, type, null);
        serviceTracker.open();
        T result = (T)serviceTracker.waitForService(10000);

        assertNotNull(result);
        
        return result;
    }

    private <T> void checkServiceByPid(String pid, Class<T> type) throws InterruptedException {
    	T service = getServiceByPid(pid, type);
        assertNotNull(service);
    }

    
    private <T> T getServiceByPid(String pid, Class<T> type) throws InterruptedException {
    	String filter = "(" + Constants.SERVICE_PID + "=" + pid + ")";
    	
        ServiceTracker<T, T> serviceTracker;
        T result = null;
		try {
			serviceTracker = new ServiceTracker<T, T>(context, FrameworkUtil.createFilter(filter), null);
		
	        serviceTracker.open();
	        result = type.cast(serviceTracker.waitForService(10000));
		} catch (InvalidSyntaxException e) {
			fail(e.getMessage());
		}

		return result;
    }

    private Dictionary<String, Object> getPvPanelProperties() throws Exception {
    	// create PvPanel props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_PV_PANEL);
    	properties.put("desiredParentId", AGENT_ID_CONCENTRATOR);
    	properties.put("bidUpdateRate", "30");
    	properties.put("minimumDemand", "-700");
    	properties.put("maximumDemand", "-600");
    	
    	return properties;
    }
    
    private Dictionary<String, Object> getFreezerProperties() throws Exception {
    	// create Freezer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_FREEZER);
    	properties.put("desiredParentId", AGENT_ID_CONCENTRATOR);
    	properties.put("bidUpdateRate", "30");
    	properties.put("minimumDemand", "100");
    	properties.put("maximumDemand", "121");
    	
    	return properties;
    }
    
    private Dictionary<String, Object> getConcentratorProperties() throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_CONCENTRATOR);
    	properties.put("desiredParentId", AGENT_ID_AUCTIONEER);
    	properties.put("minTimeBetweenBidUpdates", "1000");
    	
    	return properties;
    }
    
    private Configuration createConfifuration(String factoryPid, Dictionary<String, Object> properties) throws Exception {
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
    	properties.put("minTimeBetweenPriceUpdates", 1000);
    	
    	return properties;
    }
    
    private Dictionary<String, Object> getStoringObserverProperties() throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("observableAgent_filter", "");

    	return properties;    	
    }
    
    private void checkBidsFullCluster(StoringObserver observer) {
    	assertTrue(!observer.getEvents().isEmpty());
    	assertTrue(observer.getEvents().containsKey(AGENT_ID_CONCENTRATOR));
    	assertTrue(observer.getEvents().containsKey(AGENT_ID_PV_PANEL));
    	assertTrue(observer.getEvents().containsKey(AGENT_ID_FREEZER));
    }
    
    private void checkBidsClusterNoFreezer(StoringObserver observer) {
    	assertTrue(!observer.getEvents().isEmpty());
    	assertTrue(observer.getEvents().containsKey(AGENT_ID_CONCENTRATOR));
    	assertFalse(observer.getEvents().containsKey(AGENT_ID_FREEZER));
    }
    
    private void checkBidsClusterNoAuctioneer(StoringObserver observer) {
    	assert(observer.getEvents().isEmpty());
    }
    
}
