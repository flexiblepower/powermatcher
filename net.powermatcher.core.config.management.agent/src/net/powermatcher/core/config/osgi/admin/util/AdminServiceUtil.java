package net.powermatcher.core.config.osgi.admin.util;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author IBM
 * @version 0.9.0
 */
public class AdminServiceUtil {
	/**
	 * Define the configuration admin (ConfigurationAdmin) field.
	 */
	private ConfigurationAdmin configurationAdmin;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * admin parameter.
	 * 
	 * @param configurationAdmin
	 *            The configuration admin (<code>ConfigurationAdmin</code>)
	 *            parameter.
	 */
	public AdminServiceUtil(final ConfigurationAdmin configurationAdmin) {
		super();
		this.configurationAdmin = configurationAdmin;
	}

	/**
	 * Create factory configuration with the specified fpid, bundle location and
	 * properties parameters and return the Configuration result.
	 * 
	 * @param fpid
	 *            The fpid (<code>String</code>) parameter.
	 * @param bundleLocation
	 *            The bundle location (<code>String</code>) parameter.
	 * @param properties
	 *            The properties (<code>Dictionary</code>) parameter.
	 * @return Results of the create factory configuration (
	 *         <code>Configuration</code>) value.
	 * @throws IOException
	 */
	public Configuration createFactoryConfiguration(final String fpid, final String bundleLocation,
			@SuppressWarnings("rawtypes") final Dictionary properties) throws IOException {
		Configuration config = this.configurationAdmin.createFactoryConfiguration(fpid, bundleLocation);
		if (config != null) {
			config.update(properties);
		}
		return config;
	}

	/**
	 * Create filter with the specified fpid, PID and ID parameters and return
	 * the String result.
	 * 
	 * @param fpid
	 *            The fpid (<code>String</code>) parameter.
	 * @param pid
	 *            The PID (<code>String</code>) parameter.
	 * @param id
	 *            The ID (<code>String</code>) parameter.
	 * @return Results of the create filter (<code>String</code>) value.
	 */
	public String createFilter(final String fpid, final String pid, final String id) {
		String filter = null;
		List<String> elements = new ArrayList<String>();
		if (fpid == null && pid == null && id == null) {
			return null;
		}
		if (fpid != null) {
			elements.add("(service.factoryPid=" + fpid + ")");
		}
		if (pid != null) {
			elements.add("(service.pid=" + pid + ")");
		}
		if (id != null) {
			elements.add("(Id=" + id + ")");
		}
		int size = elements.size();
		if (size == 0) {
			filter = null;
		} else if (size == 1) {
			filter = elements.get(0);
		} else {
			filter = "(&";
			for (int i = 0; i < size; i++) {
				filter += elements.get(i);
			}
			filter += ")";
		}
		return filter;
	}

	/**
	 * Create or update configuration with the specified PID, bundle location
	 * and properties parameters and return the Configuration result.
	 * 
	 * @param pid
	 *            The PID (<code>String</code>) parameter.
	 * @param bundleLocation
	 *            The bundle location (<code>String</code>) parameter.
	 * @param properties
	 *            The properties (<code>Dictionary</code>) parameter.
	 * @return Results of the create or update configuration (
	 *         <code>Configuration</code>) value.
	 * @throws IOException
	 */
	public Configuration createOrUpdateConfiguration(final String pid, final String bundleLocation,
			@SuppressWarnings("rawtypes") final Dictionary properties) throws IOException {
		Configuration config = this.configurationAdmin.getConfiguration(pid, bundleLocation);
		if (config != null) {
			config.update(properties);
		}
		return config;
	}

	/**
	 * Delete configurations with the specified filter parameter.
	 * 
	 * @param filter
	 *            The filter (<code>String</code>) parameter.
	 * @throws IOException
	 * @throws InvalidSyntaxException
	 * @see #printConfigurations(Configuration[])
	 * @see #searchConfigurations(String)
	 */
	public void deleteConfigurations(final String filter) throws IOException, InvalidSyntaxException {
		Configuration[] configurations = this.configurationAdmin.listConfigurations(filter);
		if (configurations != null) {
			for (int i = 0; i < configurations.length; i++) {
				configurations[i].delete();
			}
		}
	}

	/**
	 * Get configuration by PID with the specified PID parameter and return the
	 * Configuration result.
	 * 
	 * @param pid
	 *            The PID (<code>String</code>) parameter.
	 * @return Results of the get configuration by PID (
	 *         <code>Configuration</code>) value.
	 * @throws IOException
	 * @throws InvalidSyntaxException
	 */
	public Configuration getConfigurationByPid(final String pid) throws IOException, InvalidSyntaxException {
		Configuration[] configurations = this.configurationAdmin.listConfigurations(createFilter(null, pid, null));
		if (configurations != null && configurations.length > 0) {
			return configurations[0];
		}
		return null;
	}

	/**
	 * Get configuration details with the specified configuration parameter and
	 * return the StringBuffer result.
	 * 
	 * @param config
	 *            The configuration (<code>Configuration</code>) parameter.
	 * @return Results of the get configuration details (
	 *         <code>StringBuffer</code>) value.
	 */
	public StringBuffer getConfigurationDetails(final Configuration config) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("--------------------------------------------\n");
		buffer.append("Factory PID : " + config.getFactoryPid() + "\n");
		buffer.append("PID         : " + config.getPid() + "\n");
		buffer.append("Bundle loc. : " + config.getBundleLocation() + "\n");

		@SuppressWarnings("unchecked")
		Dictionary<String, Object> props = config.getProperties();
		if (props != null && !props.isEmpty()) {
			List<String> nameList = Collections.list(props.keys());
			for (String name : nameList) {
				buffer.append(" property - " + name + "=" + props.get(name) + "\n");
			}
		} else {
			buffer.append("Configuration has no properties defined." + "\n");
		}
		return buffer;
	}

	/**
	 * Print configurations with the specified configs parameter.
	 * 
	 * @param configs
	 *            The configs (<code>Configuration[]</code>) parameter.
	 * @see #deleteConfigurations(String)
	 * @see #searchConfigurations(String)
	 */
	public void printConfigurations(final Configuration[] configs) {
		if (configs == null || configs.length == 0) {
			System.out.println("No configurations found ");
		} else if (configs.length == 1) {
			System.out.println(getConfigurationDetails(configs[0]));
		} else {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < configs.length; i++) {
				buffer = getConfigurationDetails(configs[i]);
			}
			System.out.println(buffer);
		}
	}

	/**
	 * Search configurations with the specified filter parameter and return the
	 * Configuration[] result.
	 * 
	 * @param filter
	 *            The filter (<code>String</code>) parameter.
	 * @return Results of the search configurations (
	 *         <code>Configuration[]</code>) value.
	 * @throws IOException
	 * @throws InvalidSyntaxException
	 * @see #deleteConfigurations(String)
	 * @see #printConfigurations(Configuration[])
	 */
	public Configuration[] searchConfigurations(final String filter) throws IOException, InvalidSyntaxException {
		return this.configurationAdmin.listConfigurations(filter);
	}

}
