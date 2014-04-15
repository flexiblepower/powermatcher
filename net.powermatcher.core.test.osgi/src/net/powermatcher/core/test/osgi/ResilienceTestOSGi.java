package net.powermatcher.core.test.osgi;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public abstract class ResilienceTestOSGi extends TestCase {

	private final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
    
	public final static String AUCTIONEER_AGENT_PID = "net.powermatcher.core.agent.auctioneer.Auctioneer";
	public final static String CONCENTRATOR_AGENT_PID = "net.powermatcher.core.agent.concentrator.Concentrator";
	public final static String TEST_AGENT_PID = "net.powermatcher.core.agent.test.TestAgent";
	public final static String CSV_LOGGING_AGENT_PID = "net.powermatcher.core.agent.logging.CSVLoggingAgent";
	
	
	private ServiceReference configAdminReference;
    private ServiceReference scrServiceReference;
    
    protected ConfigurationAdmin configAdmin;
    protected ScrService scrService;
    
    private String currentDate;
    
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		configAdminReference = context.getServiceReference(ConfigurationAdmin.class.getName());
    	if (configAdminReference != null) {
    		configAdmin = (ConfigurationAdmin) context.getService(configAdminReference);
    	}
    	assert(configAdmin != null);
    	
    	// Wait for the processing to initialize
	    Thread.sleep(5000);
    	
    	scrServiceReference = context.getServiceReference( ScrService.class.getName() );
    	if (scrServiceReference != null) {
    		scrService = (ScrService) context.getService(scrServiceReference);
    	}
    	assert(scrService != null);
    	
    	this.currentDate = getCurrentDateString();
	}
	
	
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
    	context.ungetService(configAdminReference);
    	context.ungetService(scrServiceReference);
	}
	
	/**
	 * Verifies if a valid config manager component is running.
	 * 
	 * @return Returns true if there is exactly one config manager component running, otherwise false;
	 */
	protected boolean isConfigManagerComponentPresent() {
		// Check if the ConfigManager is present
    	Component[] components = scrService.getComponents("net.powermatcher.core.config.management.agent.ConfigManager");
    	if (components != null && components.length == 1 && components[0].getState() == Component.STATE_ACTIVE) {
    		return true;
    	}
    	return false;
	}
	
	/**
	 * Verifies if the factory configuration with the specified pid is
	 * currently defined. 
	 * 
	 * @param pid	The Persistent Identifier (PID).
	 * @return Returns true if there is at least one 
	 * 			configuration with the specified pid, otherwise false;
	 * @throws InvalidSyntaxException 
	 * @throws IOException 
	 */
	protected boolean containsFactoryConfiguration(String pid) throws IOException, InvalidSyntaxException {
		// Verify that there is no Auctioneer configuration present
		Configuration[] configurations = listFactoryConfigurations(pid);
		if (configurations != null && configurations.length > 0 ) {
			return true;
		}		
    	return false;
	}

	/**
	 * Retun factory configurations with the specified pid.
	 * 
	 * @param pid	The Persistent Identifier (PID).
	 * @return The configurations that match the specified PID.
	 * @throws IOException
	 * @throws InvalidSyntaxException
	 */
	protected Configuration[] listFactoryConfigurations(String pid) throws IOException, InvalidSyntaxException { 
		return configAdmin.listConfigurations("(service.factoryPid=" + pid + ")");
	}
	
	
	/**
	 * Return the list of currently registered components.
	 * 
	 * @return The list of components
	 */
	protected Component[] getComponents() { 
		return scrService.getComponents();
	}
	
	/**
	 * Return the list of components with a PID as specified..
	 * 
	 * @param pid	The Persistent Identifier (PID).
	 * @return The compnents that match the specified PID.
	 */
	protected Component[] getComponents(String pid) { 
		return scrService.getComponents(pid);
	}
	
	/**
	 * Return the component that matches the PID and id.
	 * 
	 * @param pid	The Persistent Identifier (PID).
	 * @param id	The components id property.
	 * @return The component that matches the specified PID and id property.
	 */
	protected Component getComponent(String pid, String id) { 
		Component[] components = scrService.getComponents(pid);
		Component cmpnt = null; 
		
		if (components != null ) {
			for (Component c : components) {
				if (c.getName().equals(pid) 
						&& c.getProperties() != null && c.getProperties().get("id") != null 
						&& c.getProperties().get("id").equals(id)) {
					cmpnt = c;
				}
			}
		}
		return cmpnt;
	}
	
	protected boolean checkComponentState(Component[] components, String pid, int state) {
		if (components != null ) {
			for (Component c : components) {
				if (c.getName().equals(pid) && c.getState() == state) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean checkComponentState(Component[] components, String pid, String id,  int state) {
		if (components != null ) {
			for (Component c : components) {
				if (c.getName().equals(pid) && c.getState() == state
						&& c.getProperties() != null && c.getProperties().get("id") != null 
						&& c.getProperties().get("id").equals(id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean checkComponentNotEqualsState(Component[] components, String pid, String id,  int state) {
		if (components != null ) {
			for (Component c : components) {
				if (c.getName().equals(pid) && c.getState() != state
						&& c.getProperties() != null && c.getProperties().get("id") != null 
						&& c.getProperties().get("id").equals(id)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected Configuration findConfiguration(Configuration[] configurations, String pid, String id) {
		for (Configuration conf : configurations) {
			if (conf.getFactoryPid() != null && conf.getFactoryPid().equals(pid)  
					&& conf.getProperties() != null && conf.getProperties().get("id") != null 
					&& conf.getProperties().get("id").equals(id)) {
				return conf;
			}
			
		}
		return null;
	}
	
	protected Configuration createOrUpdateConfigManagerConfiguration(final String configFile) throws IOException, InterruptedException {
		String pid = "net.powermatcher.core.config.management.agent.ConfigManager";    	
    	Dictionary<String, Object> properties = new Hashtable<String, Object>();

    	properties.put("configuration.data.url", configFile);
    	properties.put("update.interval", 5);
    	
		Configuration config = this.configAdmin.getConfiguration(pid, null);
		if (config != null) {
			config.update(properties);
			
			// Wait for the processing by the ConfigManager
	    	Thread.sleep(10000);
	    
	    	properties.put("update.interval", 300);
	    	config.update(properties);
		}
    	
		return config;
	}
	
	protected String getCurrentDateString() {
		Date now = new Date( System.currentTimeMillis());
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String nowAsString = df.format(now);
		
		return nowAsString;
	}
	
	protected String getBidLogFileName(String prefix) {
		return prefix + "_pwm_bid_log_" + currentDate + ".csv";
	}
	
	protected String getPriceLogFileName(String prefix) {
		return prefix + "_pwm_price_log_" + currentDate + ".csv";
	}
}
