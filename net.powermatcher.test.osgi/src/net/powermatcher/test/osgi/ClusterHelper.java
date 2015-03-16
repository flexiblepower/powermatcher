package net.powermatcher.test.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ClusterHelper {

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
    
}
