package net.powermatcher.core.config.management.agent.test;

import java.io.IOException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;
import net.powermatcher.core.config.management.agent.component.ConfigManagerComponent;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ConfigManagerComponentTest extends TestCase {

	private final static long PROCESSING_DELAY = 1000;
    private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    private ServiceReference configAdminReference;
    ConfigurationAdmin configAdmin;
    
    
    @Override
	protected void setUp() throws Exception {
		super.setUp();
		
		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
    	if (configAdminReference != null) {
    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
    	}
    	assert(configAdmin != null);
	}

    
	public void test1_CreateConfiguration() throws Exception {
        
		createOrUpdateConfigManagerConfiguration("test_configuration1.xml");
    	
    	// Wait for the processing by the ConfigManager
    	Thread.sleep(PROCESSING_DELAY);
    	    			
    	
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=PowerMatcherTestAgent)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		Configuration agentConfig = configurations[0];
    		Dictionary<String, String> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals("30"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals("1000"));
    	}

    }

	public void test2_UpdateConfiguration() throws Exception {
        
		createOrUpdateConfigManagerConfiguration("test_configuration2.xml");
    	
    	// Wait for the processing by the ConfigManager
    	Thread.sleep(PROCESSING_DELAY);
    	    			
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=PowerMatcherTestAgent)");
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		Configuration agentConfig = configurations[0];
    		Dictionary<String, String> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals("39"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals("2000"));
    	}
    	

    }
	
	public void test3_DeleteConfiguration() throws Exception {
    	
		createOrUpdateConfigManagerConfiguration("test_configuration3.xml");
    	
    	// Wait for the processing by the ConfigManager
    	Thread.sleep(PROCESSING_DELAY);
    	
    	// Check that the PowerMatcherTestAgent config has been deleted.
    	Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=PowerMatcherTestAgent)");
    	assert(configurations == null || configurations.length == 0 );

    }
	
	public void test4_CreateTestConfigurations()  throws Exception {
		createOrUpdateConfigManagerConfiguration("test_configuration4.xml");
		
    	// Wait for the processing by the ConfigManager
    	Thread.sleep(PROCESSING_DELAY);
    	
    	// Check that the PowerMatcherTestAgent config has been deleted.
    	Configuration[] configurations = configAdmin.listConfigurations(null);
    	assert(configurations == null || configurations.length > 2 );
	}
	
	public void test5_DeleteTestConfigurations() throws Exception {
    	
		createOrUpdateConfigManagerConfiguration("test_configuration5.xml");
    	
    	// Wait for the processing by the ConfigManager
    	Thread.sleep(PROCESSING_DELAY);
    	
    	// Check that the PowerMatcherTestAgent config has been deleted.
    	Configuration[] configurations = configAdmin.listConfigurations(null);
    	assert(configurations != null && configurations.length == 1 && configurations[0].getFactoryPid().equals("PowerMatcherTestAgent"));

    }
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
    	context.ungetService(configAdminReference);
	}
	
	private Configuration createOrUpdateConfigManagerConfiguration(final String configFile) throws IOException {
		String pid = ConfigManagerComponent.COMPONENT_NAME;    	
    	URL url = context.getBundle().getResource(configFile);
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();
    	properties.put("configuration.data.url", url.toExternalForm());
    	
		Configuration config = this.configAdmin.getConfiguration(pid, null);
		if (config != null) {
			config.update(properties);
		}
		return config;
	}
}
