package net.powermatcher.test.osgi;

import junit.framework.TestCase;

import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Base case for OSGI testcases 
 *
 * @author FAN
 * @version 2.0
 */
public abstract class OsgiTestCase extends TestCase {
	protected final BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();
    protected ServiceReference<?> scrServiceReference = context.getServiceReference( ScrService.class.getName());
    protected ScrService scrService = (ScrService) context.getService(scrServiceReference);
    protected ConfigurationAdmin configAdmin;
    
    protected ClusterHelper clusterHelper;
 
    /**
     * Setup tests, which cleans existing OSGI servers and gets reference to configuration admin. 
     */
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
}
