package net.powermatcher.test.osgi;

import java.lang.reflect.Field;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class ClusterHelper {

	private final String FACTORY_PID_AUCTIONEER = "net.powermatcher.core.auctioneer.Auctioneer";
	private final String FACTORY_PID_CONCENTRATOR = "net.powermatcher.core.concentrator.Concentrator";
	private final String FACTORY_PID_PV_PANEL = "net.powermatcher.examples.PVPanelAgent";
	private final String FACTORY_PID_FREEZER = "net.powermatcher.examples.Freezer";
	private final String FACTORY_PID_OBSERVER = "net.powermatcher.examples.StoringObserver";
	
	private final String AGENT_ID_AUCTIONEER = "auctioneer";
	private final String AGENT_ID_CONCENTRATOR = "concentrator";
	private final String AGENT_ID_PV_PANEL = "pvPanel";
	private final String AGENT_ID_FREEZER = "freezer";

	public Configuration createConfiguration(ConfigurationAdmin configAdmin, String factoryPid, Dictionary<String, Object> properties) throws Exception {
    	Configuration config = configAdmin.createFactoryConfiguration(factoryPid, null);

    	// create Auctioneer props
    	config.update(properties);
    	
    	return config;
    }
	
	public Dictionary<String, Object> getAuctioneerProperties(String agentIdAuctioneer, long minTimeBetweenPriceUpdates) {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", agentIdAuctioneer);
    	properties.put("clusterId", "DefaultCluster");
    	properties.put("commodity", "electricity");
    	properties.put("currency", "EUR");
    	properties.put("priceSteps", 100);
    	properties.put("minimumPrice", 0.0);
    	properties.put("maximumPrice", 1.0);
    	properties.put("bidTimeout", 600);
    	properties.put("minTimeBetweenPriceUpdates", minTimeBetweenPriceUpdates);
    	
    	return properties;
    }
	
	public Dictionary<String, Object> getConcentratorProperties(String agentIdConcentrator, String agentIdAuctioneer, long minTimeBetweenBidUpdates) throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", agentIdConcentrator);
    	properties.put("desiredParentId", agentIdAuctioneer);
    	properties.put("minTimeBetweenBidUpdates", minTimeBetweenBidUpdates);
    	
    	return properties;
    }
	
	public Dictionary<String, Object> getPvPanelProperties(String agentIdPvPanel,String agentIdConcentrator, long bidUpdateRate) throws Exception {
    	// create PvPanel props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", agentIdPvPanel);
    	properties.put("desiredParentId", agentIdConcentrator);
    	properties.put("bidUpdateRate", bidUpdateRate);
    	properties.put("minimumDemand", "-700");
    	properties.put("maximumDemand", "-600");
    	
    	return properties;
    }
	
    public Dictionary<String, Object> getFreezerProperties(String agentIdFreezer,String agentIdConcentrator, long bidUpdateRate) throws Exception {
    	// create Freezer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", agentIdFreezer);
    	properties.put("desiredParentId", agentIdConcentrator);
    	properties.put("bidUpdateRate", bidUpdateRate);
    	properties.put("minimumDemand", "100");
    	properties.put("maximumDemand", "121");
    	
    	return properties;
    }

    public Dictionary<String, Object> getStoringObserverProperties() throws Exception {
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("observableAgent_filter", "");

    	return properties;    	
    }
 
    public boolean checkActive(ScrService scrService, final String factoryPid) throws Exception {
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
    
    public <T> void checkServiceByPid(BundleContext context, String pid, Class<T> type) throws InterruptedException {
    	T service = getServiceByPid(context, pid, type);
    	TestCase.assertNotNull(service);
    }
    
    public <T> T getService(BundleContext context, Class<T> type) throws InterruptedException {
        ServiceTracker<T, T> serviceTracker = 
                new ServiceTracker<T, T>(context, type, null);
        serviceTracker.open();
        T result = (T)serviceTracker.waitForService(10000);

        TestCase.assertNotNull(result);
        
        return result;
    }
    
    public <T> T getServiceByPid(BundleContext context, String pid, Class<T> type) throws InterruptedException {
    	String filter = "(" + Constants.SERVICE_PID + "=" + pid + ")";
    	
        ServiceTracker<T, T> serviceTracker;
        T result = null;
		try {
			serviceTracker = new ServiceTracker<T, T>(context, FrameworkUtil.createFilter(filter), null);
		
	        serviceTracker.open();
	        result = type.cast(serviceTracker.waitForService(10000));
		} catch (InvalidSyntaxException e) {
			TestCase.fail(e.getMessage());
		}

		return result;
    }
    
    public <T> T getPrivateField(Object agent, String field, Class<T> type) {
        T result = null;
        Field privateField = null;
        try {
            privateField = agent.getClass().getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            try {
                privateField = agent.getClass().getSuperclass().getDeclaredField(field);
            } catch (NoSuchFieldException e2) {
                TestCase.fail("Failed to get " + type.getSimpleName() + ", reason: " + e2);
            }
        }

        // Read value from field
        if (privateField != null) {
            try {
                privateField.setAccessible(true);
                result = type.cast(privateField.get(agent));
            } catch (IllegalArgumentException e) {
            	TestCase.fail("Failed to get " + type.getSimpleName() + ", reason: " + e);
            } catch (IllegalAccessException e) {
            	TestCase.fail("Failed to get " + type.getSimpleName() + ", reason: " + e);
            }
        }

        return result;
    }
    
	public void disconnectAgent(ConfigurationAdmin configAdmin, String agentPid) throws Exception, InvalidSyntaxException {
		Configuration config = configAdmin.getConfiguration(agentPid);
		if (config == null) {
			TestCase.fail("Config for agent " + agentPid + " does not exist, but should be.");
		}
		
		config.delete();
	}

	public String getFactoryPidAuctioneer() {
		return FACTORY_PID_AUCTIONEER;
	}
	
	public String getFactoryPidConcentrator() {
		return FACTORY_PID_CONCENTRATOR;
	}
	
	public String getAgentIdAuctioneer() {
		return AGENT_ID_AUCTIONEER;
	}
	
	public String getAgentIdConcentrator() {
		return AGENT_ID_CONCENTRATOR;
	}
	
	public String getFactoryPidPvPanel() {
		return FACTORY_PID_PV_PANEL;	
	}
	
	public String getAgentIdPvPanel() {
		return AGENT_ID_PV_PANEL;
	}
	
	public String getAgentIdFreezer() {
		return AGENT_ID_FREEZER;
	}
	
	public String getFactoryPidObserver() {
		return FACTORY_PID_OBSERVER;
	}
	
	public String getFactoryPidFreezer() {
		return FACTORY_PID_FREEZER;
	}
}
