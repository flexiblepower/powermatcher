package net.powermatcher.core.test.osgi;

import java.util.Dictionary;

import net.powermatcher.core.test.util.CSVLogReader;

import org.apache.felix.scr.Component;
import org.osgi.service.cm.Configuration;

/**
 * Implements the resilience test cases for the Communication Framework testing
 * of the (Test)Agent. The test scenarios focus on adding, updating and removing
 * agent configurations.
 */
public class ResilienceTestCGF extends ResilienceTestOSGi {

	
	/**
	 * Add (Test)Agent to an empty cluster. 
	 * 
	 * Starts with an initial empty configuration (no agents) and
	 * then adds the configuration for a single test agent.
	 * 
	 * Expected behaviour: Test agent component is instantiated and
	 * activated.
	 * 
	 * @throws Exception
	 */
	public void test_CGF1_AddSingleAgent() throws Exception {
        // First load an empty configuration 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGF1_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is no TestAgent configuration present
    	assert(!containsFactoryConfiguration(TEST_AGENT_PID));
    	    	
    	// Load configuration file that adds an TestAgent configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGF1_add_agent.xml");
    	
    	// Verify that there is exactly one TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals(100.0));
    	}
    	
    	// Verify that the Test Agent is active
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, TEST_AGENT_PID, "testagent1", Component.STATE_ACTIVE));   
    	
    }
	
	/**
	 * Add (Test)Agent to an existing cluster.
	 * 
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator and
	 * CSVLoggingAgent. Then a configuration for a single TestAgent
	 * is added.
	 * 
	 * Expected behaviour: (1) Auctioneer, CSVLoggingAgent and Concentrator are
	 * active and exchanging price and bid information. (2)After creating the
	 * TestAgent configuration the TestAgent is active. (3) The TestAgent sends
	 * bids and receives price information. 
	 * 
	 * 
	 * @throws Exception
	 */
	public void test_CGF2_Add_TestAgent() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGF2_init.xml");

		// Check if the ConfigManager is present
    	assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is no Concentrator configuration present
    	// Verify that there is no TestAgent configuration present
    	assert(!containsFactoryConfiguration(TEST_AGENT_PID));

    	    	
    	// Load configuration that adds a Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGF2_add_testagent.xml");
    	
    	// Verify that there is exactly one TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals(100.0));
    	}
    	
    	
    	// Check that the TestAgent and other components are running
    	Component[] components = getComponents();
    	assert(checkComponentState(components, CONCENTRATOR_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, AUCTIONEER_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, TEST_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, CSV_LOGGING_AGENT_PID, Component.STATE_ACTIVE));
    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Read log files
    	String priceLogFile = getPriceLogFileName("CGF2_POST");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	
    	String bidLogFile = getBidLogFileName("CGF2_POST");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));

    }
	
	/**
	 * Update a (Test)Agent
	 * 
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then some configuration properties for
	 * the test agent are updated.
	 * 
	 * Expected behaviour: (1) Auctioneer, Concentrator, CSVLoggingAgent and TestAgent are
	 * active and exchange price and bid information. (2) After processing the
	 * configuration update the test agent properties are updated (3) The
	 * agents remain active after the update. 
	 * 
	 * @throws Exception
	 */
	public void test_CGF3_Update_TestAgent() throws Exception {
        // First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGF3_init.xml");

		// Check if the ConfigManager is present
    	assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is exactly one TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals(100.0));
    		assert(agentProps != null && agentProps.get("minimum.power") != null && agentProps.get("minimum.power").equals(0.0));
    	}
    	
    	// Verify that the Test Agent is active
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, TEST_AGENT_PID, "testagent1", Component.STATE_ACTIVE)); 
    	
    	// Load configuration that updates the Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGF3_update_testagent.xml");
    	
    	// Verify that there is exactly one TestAgent configuration present that
    	// contains the updated property values.
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals(200.0));
    		assert(agentProps != null && agentProps.get("minimum.power") != null && agentProps.get("minimum.power").equals(5.0));
    	}
    	
    	// Check that the TestAgent and other components are running
    	components = getComponents();
    	assert(checkComponentState(components, CONCENTRATOR_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, AUCTIONEER_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, TEST_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, CSV_LOGGING_AGENT_PID, Component.STATE_ACTIVE));
    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Read log files and verify the auctioneer and concentrator send price info
    	String priceLogFile = getPriceLogFileName("CGF3_POST");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	
    	// and that they log bid info
    	String bidLogFile = getBidLogFileName("CGF3_POST");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    }
	
	/**
	 * Remove a (Test)Agent
	 *  
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then some configuration properties for
	 * the test agent are updated.
	 * 
	 * Expected behaviour: (1) Auctioneer, Concentrator, CSVLoggingAgent and TestAgent are
	 * active and exchange price and bid information. (2) After processing the
	 * configuration update the test agent configuration is removed and the component
	 * is stopped (state is 'unsatisfied'). (3) The concentrator does not receive any
	 * bids and aggregated bid is repeated util TestAgent bid is expired. The price information
	 * is send by the auctioneer to the concentrator and both log the price info.
	 * 
	 * @throws Exception
	 */
	public void test_CGF4_Remove_TestAgent() throws Exception {
        // First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGF4_init.xml");

		// Check if the ConfigManager is present
    	assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is exactly one TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("maximum.power").equals(100.0));
    		assert(agentProps != null && agentProps.get("maximum.power") != null && agentProps.get("minimum.power").equals(0.0));
    	}
    	
    	// Verify that the Test Agent is active
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, TEST_AGENT_PID, "testagent1", Component.STATE_ACTIVE)); 
    	   	    	
    	// Load configuration that where the Auctioneer configuration is removed
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGF4_testagent_removed.xml");
    	
    	// Verify that there no TestAgent configuration present
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );
    	
    	// Verify that there is no TestAgent component running, (state is is not active but 'unsatisfied')
    	// and the other agent components remain active.
    	components = getComponents();
    	assert(checkComponentState(components, CONCENTRATOR_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, AUCTIONEER_AGENT_PID, Component.STATE_ACTIVE));
    	assert(checkComponentState(components, TEST_AGENT_PID, Component.STATE_UNSATISFIED));
    	assert(checkComponentState(components, CSV_LOGGING_AGENT_PID, Component.STATE_ACTIVE));
    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Read log files and verify the auctioneer and concentrator send price info
    	String priceLogFile = getPriceLogFileName("CGF4_POST");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	
    	// and that except by the test agent the others log bid info
    	String bidLogFile = getBidLogFileName("CGF4_POST");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	
    }
}
