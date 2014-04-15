package net.powermatcher.core.config.test;


import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import net.powermatcher.core.config.ConfigurationSpec;
import net.powermatcher.core.config.ConfigurationSpec.ConfigurationType;
import net.powermatcher.core.config.parser.PropertyConverterUtility;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigurationTest {

	ConfigurationSpec rootGroup;

	private void addProperty(final Map<String, Object> properties, final String key, final String value, final String type) {
		properties.put(key, PropertyConverterUtility.convert(value, type));
	}

	/**
	 * 
	 */
	@Test
	public void equalsTest() {

		// Create two identical configurations
		ConfigurationSpec config1 = new ConfigurationSpec(ConfigurationType.singleton, "agent1", "test_cluster", "TestAgent",
				false, this.rootGroup);
		addProperty(config1.getProperties(), "property1", "value1", "String");
		addProperty(config1.getProperties(), "property2", "value2", "String");
		addProperty(config1.getProperties(), "property3", "value3", "String");
		addProperty(config1.getProperties(), "property4", "100", "Integer");

		ConfigurationSpec config2 = new ConfigurationSpec(ConfigurationType.singleton, "agent1", "test_cluster", "TestAgent",
				false, this.rootGroup);
		addProperty(config2.getProperties(), "property1", "value1", "String");
		addProperty(config2.getProperties(), "property2", "value2", "String");
		addProperty(config2.getProperties(), "property3", "value3", "String");
		addProperty(config2.getProperties(), "property4", "100", "Integer");

		// They should be equal
		assertEquals(true, config1.equals(config2));

		// Change a property of config2, now they should be unequal
		addProperty(config2.getProperties(), "property1", "value1new", "String");
		assertEquals(false, config1.equals(config2));

	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.rootGroup = new ConfigurationSpec(ConfigurationType.group, "root", "test_cluster", null, false, null);
		// key, PropertyConverterUtility.convert(value, type));
		addProperty(this.rootGroup.getProperties(), "groupproperty1", "groupvalue1", "String");
		addProperty(this.rootGroup.getProperties(), "property3", "value3rootgroup", "String");

		ConfigurationSpec templateConfig = new ConfigurationSpec(ConfigurationType.singleton, "TestAgentTemplate",
				"test_cluster", "TestAgent", true, this.rootGroup);
		addProperty(templateConfig.getProperties(), "templateProp1", "templatevalue1", "String");
		this.rootGroup.addChild(templateConfig);

		ConfigurationSpec subGroup = new ConfigurationSpec(ConfigurationType.group, "subgroup", "test_cluster", null, false,
				this.rootGroup);
		addProperty(subGroup.getProperties(), "subproperty1", "subvalue1", "String");
		addProperty(subGroup.getProperties(), "property3", "value3subgroup", "String");
		this.rootGroup.addChild(subGroup);

		ConfigurationSpec config1 = new ConfigurationSpec(ConfigurationType.singleton, "agent1", "test_cluster", "TestAgent",
				false, subGroup);
		addProperty(config1.getProperties(), "property1", "value1", "String");
		addProperty(config1.getProperties(), "property2", "value2", "String");
		addProperty(config1.getProperties(), "property3", "value3", "String");
		subGroup.addChild(config1);
	}

	/**
	 * 
	 */
	@Test
	public void testFindConfiguration() {
		ConfigurationSpec config = this.rootGroup.findConfiguration("TestAgent", "agent1");

		assertEquals("TestAgent", config.getPid());
		assertEquals("agent1", config.getId());

	}

	/**
	 * 
	 */
	@Test
	public void testFullConfigurationSet() {
		Set<ConfigurationSpec> configSet = this.rootGroup.configurationSet();
		System.out.println(configSet);
	}

	/**
	 * 
	 */
	@Test
	public void testGetConfigurationProperties() {
		ConfigurationSpec config = this.rootGroup.findConfiguration("TestAgent", "agent1");

		System.out.println(config);

	}

	/**
	 * 
	 */
	@Test
	public void testGroupProperty() {
		Object rootprop = this.rootGroup.getProperties().get("property3");
		assertEquals("value3rootgroup", rootprop);
	}

	/**
	 * 
	 */
	@Test
	public void testPropertyInheritance() {
		ConfigurationSpec config = this.rootGroup.findConfiguration("TestAgent", "agent1");
		Object prop = config.getAllProperties().get("property3");
		assertEquals("value3", prop);
	}
}
