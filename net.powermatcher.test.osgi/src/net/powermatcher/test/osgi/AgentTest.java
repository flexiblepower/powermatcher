package net.powermatcher.test.osgi;

import junit.framework.TestCase;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;

public class AgentTest extends TestCase {

    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

    private ConfigurationAdmin configAdmin;
    private ServiceReference configAdminReference;
    
	public void testExample() throws Exception {
		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
		
//		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
//    	if (configAdminReference != null) {
//    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
//    	}
    	
    	
    	assertNotNull(context);
    }
}
