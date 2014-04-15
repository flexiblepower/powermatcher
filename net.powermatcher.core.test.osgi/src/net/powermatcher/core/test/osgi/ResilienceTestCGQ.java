package net.powermatcher.core.test.osgi;

import java.util.Dictionary;

import net.powermatcher.core.test.util.CSVLogReader;

import org.apache.felix.scr.Component;
import org.osgi.service.cm.Configuration;

/**
 * Resilience test CGQ containing scenarios  for Adding, Updating or Removing 
 * Test Agents. Tests are part of the Quality tests for the within the 
 * Communication Framework Testing catgegory. 
 *
 */
public class ResilienceTestCGQ extends ResilienceTestOSGi {
   
     	
	/**
	 * Add a test agent to a cluster with a test agent with the same identifier already present.
	 * <p>
	 * Test starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then an identical configuration xml that 
	 * includes a second test agent with the same id is loaded.
	 * <p>
	 * Expected behaviour: (1) Adding second test agent fails. (2) The
	 * second test agent is not active and cluster remains working correctly.
	 *
	 * @throws Exception
	 */
	public void test_CGQ1_Add_Duplicate_Agent() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ1_init.xml");

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
    	}
    	
    	// Verify that the Test Agent is active
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, TEST_AGENT_PID, "testagent1", Component.STATE_ACTIVE));   
    	
    	    	
    	// Load configuration that adds a duplicate TestAgent configuration 
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ1_add_duplicate_agent.xml");
    	
    	// Verify that there are two TestAgent configurations present
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 2 );

    	// Verify that not both TestAgent are active.
    	components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 2 );
    	for (Component c : components) {
    		// Verify the test agents have the same id
			assert( c.getProperties() != null && c.getProperties().get("id") != null 
					&& c.getProperties().get("id").equals("testagent1"));
		}
    	assert(components[0].getState() != Component.STATE_ACTIVE || components[1].getState() != Component.STATE_ACTIVE);
       	
    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(30000);
    }
	
	/**
	 * Updates a test agent in a cluster with invalid property values.
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then an identical configuration xml where 
	 * some properties of the test agent are changed with invalid data.
	 * <p>
	 * Expected behaviour: (1) The updated values can bee seen in the configuration of the test agent.
	 * (2) the test agent does not use the invalid data and falls back to its default values. 
	 *
	 * @throws Exception
	 */
	public void test_CCQ2_Invalid_Update_Agent() throws Exception {
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ2_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
    	// Verify that there is an TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    	}
    	    	
    	// Load configuration that updates the TestAgent with invalid property values
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ2_invalid_update_agent.xml");
    	
    	// Verify that there is exactly one TestAgent configuration present
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    		assert(agentProps != null && agentProps.get("update.interval") != null && agentProps.get("update.interval").equals(-30));
    		assert(agentProps != null && agentProps.get("maximum.price") != null && agentProps.get("maximum.price").equals(-120));
    		assert(agentProps != null && agentProps.get("steps") != null && agentProps.get("steps").equals(-21));    		
    	}

    	// Verify that the TestAgent is active
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, TEST_AGENT_PID, "testagent1", Component.STATE_ACTIVE));
    	
    	// Verify the component properties
    	Component concentrator = getComponent(TEST_AGENT_PID, "testagent1");
    	assert(concentrator != null);
    	Dictionary<?, ?> props = concentrator.getProperties();
    	assert(props != null && props.get("update.interval") != null && !props.get("update.interval").equals(-30));
		assert(props != null && props.get("maximum.price") != null && !props.get("maximum.price").equals(-120));    	
		assert(props != null && props.get("steps") != null && !props.get("steps").equals(-21));
    }
	
	
	/**
	 * Ungracefully remove an agent from a cluster
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then the TestAgent is 'ungracefully' removed
	 * by removing the configuration. 
	 * <p>
	 * Expected behaviour: (1) The cluster is operational and functions without any
	 * problems. (2) After the update the test agent configuration is removed and
	 * TestAgent component is not active. (3) The concentrator is not receiving bids and
	 * the auctioneer and concentrator are logging last aggregated bid and price info.
	 *
	 * @throws Exception
	 */
	public void test_CCQ3_Ungracefully_Remove_Agent() throws Exception {

		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ3_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
		// Verify that there is an TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    	}
    	    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);   	
    	
    	// Load configuration that adds an Concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ3_ungracefully_remove_agent.xml");
    	
    	// Verify that there is no concentrator configuration present
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );

    	// Verify that the TestAgent component is not active (unsatisfied).
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(components[0].getState() == Component.STATE_UNSATISFIED);
    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Check log files. Both auctioneer and concentrator are logging price info.
    	String priceLogFile = getPriceLogFileName("CGQ3_POST");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	
    	// After the update the test agent will not log any published bids.
    	String bidLogFile = getBidLogFileName("CGQ3_POST");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	
    }
	
	/**
	 * Ungracefully remove an agent from a cluster, and after a set time, 
	 * re-add an agent to the cluster
	 * <p>
	 * Starts with an initial configuration that defines an Auctioneer, Concentrator,
	 * CSVLoggingAgent and a TestAgent. Then the test agent is 'ungracefully' removed
	 * by removing the configuration. A third version of the configuration file will
	 * restore the test agent configuration. 
	 * <p>
	 * Expected behaviour: (1) The cluster is operational and functions without any
	 * problems. (2) After the update the test agent configuration is removed and
	 * TestAgent component is not active. (3) The concentrator is not receiving bids and
	 * the auctioneer and concentrator are logging last aggregated bid and price info. 
	 * (4) The second update restores the situation and the cluster works fine again. 
	 *
	 * @throws Exception
	 */
	public void test_CAQ4_ReAddAgentAfterUngracefulRemoval() throws Exception {
		
		// First initial configuration: Auctioneer, Concentrator, CSVLoggingAgent and TestAgent. 
		createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ4_init.xml");

		// Check if the ConfigManager is present
		assert(isConfigManagerComponentPresent());
    			
		// Verify that there is an TestAgent configuration present
    	Configuration[] configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations == null || configurations.length == 1 );
    	if (configurations.length == 1) {
    		// Verify the properties
    		Configuration agentConfig = configurations[0];
    		Dictionary<?, ?> agentProps = agentConfig.getProperties();
    		assert(agentProps != null && agentProps.get("id") != null && agentProps.get("id").equals("testagent1"));
    	}
    	    	
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Load configuration that removes the concentrator configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ4_ungracefully_remove_agent.xml");
    	
    	// Verify that there is no TestAgent configuration present
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations == null || configurations.length == 0 );

    	// Verify that the concentrator component is unsatisfied
    	Component[] components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(components[0].getState() == Component.STATE_UNSATISFIED);
    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	
    	// Check log files. Both auctioneer and concentrator are logging price info.
    	String priceLogFile =  getPriceLogFileName("CGQ4_POST");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	
    	// After the update the test agent will not log any published bids.
    	String bidLogFile = getBidLogFileName("CGQ4_POST");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(!CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));

    	// Load configuration that (re)adds a TestAgent configuration
    	createOrUpdateConfigManagerConfiguration("file:resources/config_CGQ4_re_add_testagent.xml");
    	
    	// Verify that there is exactly one test agent configuration present
    	configurations = listFactoryConfigurations(TEST_AGENT_PID);
    	assert(configurations != null && configurations.length == 1 );

    	// Verify that the test agent is active
    	components = getComponents(TEST_AGENT_PID);
    	assert(components != null && components.length == 1 );
    	assert(checkComponentState(components, TEST_AGENT_PID, "testagent1", Component.STATE_ACTIVE));    	
		
    	// Let the agents exchange some bids and price info
    	Thread.sleep(60000);
    	

    	// Check log files. Both auctioneer and concentrator are logging price info.
    	priceLogFile = getPriceLogFileName("CGQ4_POST2");
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(priceLogFile, "auctioneer"));
    	
    	// After the update the test agent will again log published bids.
    	bidLogFile = getBidLogFileName("CGQ4_POST2");
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "concentrator"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "auctioneer"));
    	assert(CSVLogReader.containsLogLinesForToken(bidLogFile, "testagent1"));
    	
    }
	
	// TODO: CGQ5
}
