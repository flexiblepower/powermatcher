package net.powermatcher.test.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;
import net.powermatcher.core.auctioneer.Auctioneer;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.apache.felix.scr.Component;

public class OsgiAgentTest extends TestCase {

    private final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
    
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
	
	        assertNotNull(result);
		} catch (InvalidSyntaxException e) {
			fail(e.getMessage());
		}
		
		return result;
    }
    
	public void testAuctioneer() throws Exception {
	    ConfigurationAdmin configAdmin = getService(ConfigurationAdmin.class);

	    // Create Auctioneer
    	String auctioneerFactoryPid = "net.powermatcher.core.auctioneer.Auctioneer";
    	Configuration auctioneerConfig = configAdmin.createFactoryConfiguration(auctioneerFactoryPid, null);

    	// create Auctioneer props
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("agentId", "auctioneer");
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

    	// Wait for Auctioneer to become active
    	Auctioneer auctioneer = getServiceByPid(auctioneerConfig.getPid(), Auctioneer.class);
    	
//    	ServiceReference<?> scrServiceReference = context.getServiceReference(ScrService.class.getName());
//    	ScrService scrService = (ScrService) context.getService(scrServiceReference);
//    	Component[] components = scrService.getComponents(auctioneerConfig.getFactoryPid());
//    	
//    	for (Component comp : components) {
//			if (comp.getConfigurationPid().equals("net.powermatcher.core.auctioneer.Auctioneer")) {
//				int state = comp.getState();
//				System.out.println(state);
//				assertEquals(Component.STATE_ACTIVE, comp.getState());
//			}
//		}
    	
    	/* this does not work, since it seems to rely on time...
        // Verify there is exactly one Auctioneer configuration present
	    ScrService scrService = getService(ScrService.class);
	    Component[] components = scrService.getComponents(auctioneerConfig.getFactoryPid());
    	assertEquals(1, components.length);
    	Component auctioneer = components[0];
    	assertEquals(Component.STATE_ACTIVE, auctioneer.getState());
    	*/    	
    }	
}
