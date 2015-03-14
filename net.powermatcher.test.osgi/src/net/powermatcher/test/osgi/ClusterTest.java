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
    
    public void testAddAgentsForCluster() throws Exception {
	    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);

	    // Create Auctioneer
    	String auctioneerFactoryPid = FACTORY_PID_AUCTIONEER;
    	Configuration auctioneerConfig = configAdmin.createFactoryConfiguration(auctioneerFactoryPid, null);
    	setAuctioneerProperties(auctioneerConfig);

    	// Wait for Auctioneer to become active
    	Auctioneer auctioneer = getServiceByPid(auctioneerConfig.getPid(), Auctioneer.class);
    	
    	assertNotNull(auctioneer);
    	Thread.sleep(1000);
    	
    	// Create Concentrator
    	String concentratorFactoryPid = FACTORY_PID_CONCENTRATOR;
    	Configuration concentratorConfig = configAdmin.createFactoryConfiguration(concentratorFactoryPid, null);
    	setConcentratorProperties(concentratorConfig);
    	
    	// Wait for Concentrator to become active
    	Concentrator concentrator = getServiceByPid(concentratorConfig.getPid(), Concentrator.class);
    	
    	assertNotNull(concentrator);
    	Thread.sleep(1000);
    	
    	// Create PvPanel
    	String pvPanelFactoryPid = FACTORY_PID_PV_PANEL;
    	Configuration pvPanelConfig = configAdmin.createFactoryConfiguration(pvPanelFactoryPid, null);
    	setPvPanelProperties(pvPanelConfig);
    	
    	// Wait for PvPanel to become active
    	PVPanelAgent pvPanel = getServiceByPid(pvPanelConfig.getPid(), PVPanelAgent.class);
    	
    	assertNotNull(pvPanel);
    	Thread.sleep(1000);

    	// Create Freezer
    	String freezerFactoryPid = FACTORY_PID_FREEZER;
    	Configuration freezerConfig = configAdmin.createFactoryConfiguration(freezerFactoryPid, null);
    	setFreezerProperties(freezerConfig);
    	
    	// Wait for Freezer to become active
    	Freezer freezer = getServiceByPid(freezerConfig.getPid(), Freezer.class);
    	
    	assertNotNull(freezer);
    	Thread.sleep(1000);
    	
    	// check Auctioneer alive
    	assertEquals(true, checkActive(FACTORY_PID_AUCTIONEER));
    	// check Concentrator alive
    	assertEquals(true, checkActive(FACTORY_PID_CONCENTRATOR));
    	// check PvPanel alive
    	assertEquals(true, checkActive(FACTORY_PID_PV_PANEL));
    	// check Freezer alive
    	assertEquals(true, checkActive(FACTORY_PID_FREEZER));
    	
    	//Create StoringObserver
    	String storingObserverFactoryPid = FACTORY_PID_OBSERVER;
    	Configuration storingObserverConfig = configAdmin.createFactoryConfiguration(storingObserverFactoryPid, null);
    	
    	setStoringObserverProperties(storingObserverConfig);
    	
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

        assertNotNull(result);

		return result;
    }

    private void setPvPanelProperties(Configuration pvPanelConfig) throws Exception {
    	// create PvPanel props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_PV_PANEL);
    	properties.put("desiredParentId", AGENT_ID_CONCENTRATOR);
    	properties.put("bidUpdateRate", "30");
    	properties.put("minimumDemand", "-700");
    	properties.put("maximumDemand", "-600");
    	pvPanelConfig.update(properties);
    }
    
    private void setFreezerProperties(Configuration freezerConfig) throws Exception {
    	// create Freezer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_FREEZER);
    	properties.put("desiredParentId", AGENT_ID_CONCENTRATOR);
    	properties.put("bidUpdateRate", "30");
    	properties.put("minimumDemand", "100");
    	properties.put("maximumDemand", "121");
    	freezerConfig.update(properties);
    }
    
    private void setConcentratorProperties(Configuration concentratorConfig) throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", AGENT_ID_CONCENTRATOR);
    	properties.put("desiredParentId", AGENT_ID_AUCTIONEER);
    	properties.put("minTimeBetweenBidUpdates", "1000");
    	
    	concentratorConfig.update(properties);
    }
    
    private void setAuctioneerProperties(Configuration auctioneerConfig) throws Exception {
    	// create Auctioneer props
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
    	auctioneerConfig.update(properties);
    }
    
    private void setStoringObserverProperties(Configuration storingObserverConfig) throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("observableAgent_filter", "");
    	storingObserverConfig.update(properties);
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
