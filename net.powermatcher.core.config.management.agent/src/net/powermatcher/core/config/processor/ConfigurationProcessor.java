package net.powermatcher.core.config.processor;


import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import net.powermatcher.core.config.ConfigurationSpec;
import net.powermatcher.core.config.ConfigurationSpec.ConfigurationType;
import net.powermatcher.core.config.osgi.admin.util.AdminServiceUtil;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigurationProcessor {
	/**
	 * Define the logger (Logger) field.
	 */
	private Logger logger;
	/**
	 * Define the admin service util (AdminServiceUtil) field.
	 */
	private AdminServiceUtil adminServiceUtil;
	/**
	 * Define the configuration PID file (File) field.
	 */
	private File configurationPidFile;
	/**
	 * Define the PID set (Set<String>) field.
	 */
	private Set<String> pidSet;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * admin and PID file parameters.
	 * 
	 * @param configurationAdmin
	 *            The configuration admin (<code>ConfigurationAdmin</code>)
	 *            parameter.
	 * @param pidFile
	 *            The PID file (<code>File</code>) parameter.
	 */
	public ConfigurationProcessor(final ConfigurationAdmin configurationAdmin, final File pidFile) {
		super();
		this.adminServiceUtil = new AdminServiceUtil(configurationAdmin);
		this.configurationPidFile = pidFile;
		this.logger = LoggerFactory.getLogger(getClass());
	}

	/**
	 * Create configuration with the specified configuration parameter and
	 * return the String result.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationSpec</code>) parameter.
	 * @return Results of the create configuration (<code>String</code>) value.
	 */
	private String createConfiguration(final ConfigurationSpec configuration) {
		logInfo("Creating configuration pid: " + configuration.getPid() + " type: " + configuration.getType() + " id:"
				+ configuration.getId() + " cluster:" + configuration.getClusterId());
		String pid = null;
		try {
			Dictionary<String, Object> properties = toDictionary(configuration.getProperties());
			properties.put(ConfigPropertyConstants.CONF_PROP_CLUSTER_ID, configuration.getClusterId());
			properties.put(ConfigPropertyConstants.CONF_PROP_ID, configuration.getId());
			Configuration osgiConfig = null;
			if (configuration.getType().equals(ConfigurationType.factory)) {
				osgiConfig = this.adminServiceUtil.createFactoryConfiguration(configuration.getPid(), null, properties);
			} else {
				osgiConfig = this.adminServiceUtil.createOrUpdateConfiguration(configuration.getPid(), null, properties);
			}
			if (osgiConfig != null) {
				pid = osgiConfig.getPid();
			} else {
				logInfo("Creating configuration failed. ");
			}
		} catch (final IOException e) {
			logError("IO Exception occurred while creating configuration: " + configuration + ". Reason: " + e.getMessage(), e);
		}
		return pid;
	}

	/**
	 * Get the current OSGi configuration objects using the pids persisted in
	 * the PID-file.
	 * 
	 * @return The set of current OSGi configurations.
	 */
	private Set<Configuration> currentOSGiConfigurations() {
		logInfo("Retrieving current configurations. ");
		Set<Configuration> configSet = new HashSet<Configuration>();
		Configuration config = null;
		for (String pid : this.pidSet) {
			try {
				config = this.adminServiceUtil.getConfigurationByPid(pid);
				/* Add the PwmConfig to the set */
				if (config != null) {
					configSet.add(config);
				}
			} catch (IOException e) {
				this.logger.error("I/O exception occured while reading PID list from file.", e);
			} catch (InvalidSyntaxException e) {
				this.logger.error("Invalid filter for searching the PID file", e);
			}
		}
		return configSet;
	}

	/**
	 * Delete configuration with the specified configuration parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>Configuration</code>) parameter.
	 */
	private void deleteConfiguration(final Configuration configuration) {
		String filter = this.adminServiceUtil.createFilter(null, configuration.getPid(), null);
		try {
			this.adminServiceUtil.deleteConfigurations(filter);
		} catch (final IOException e) {
			logError("IO Exception occurred while deleting configuration " + configuration + ". Reason: " + e.getMessage(), e);
		} catch (final InvalidSyntaxException e) {
			logError(
					"Invalid Syntax Exception occurred while deleting configuration " + configuration + ". Reason: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Deserialize pids with the specified file parameter and return the
	 * Set<String> result.
	 * 
	 * @param file
	 *            The file (<code>File</code>) parameter.
	 * @return Results of the deserialize pids (<code>Set<String></code>) value.
	 */
	private Set<String> deserializePids(final File file) {
		logInfo("Reading current PIDs from persistency.");
		ObjectInputStream ois = null;
		Set<String> pids = new HashSet<String>();
		try {
			/* Construct the ObjectInputStream object */
			ois = new ObjectInputStream(new FileInputStream(file));
			Object obj = null;
			while ((obj = ois.readObject()) != null) {
				if (obj instanceof String) {
					pids.add((String) obj);
				}
			}
		} catch (EOFException ex) {
			/* This exception will be caught when EOF is reached */
			/* Ignore exception and continue */
		} catch (ClassNotFoundException ex) {
			logError("Error when deserializing objects from file. Class not found. ", ex);
		} catch (FileNotFoundException ex) {
			logInfo("PID persistency file not found. File will be created, processing continues.");
		} catch (IOException ex) {
			logError("Error when deserializing objects from file. IO Exception:  " + ex.getMessage(), ex);
		} finally {
			/* Close the ObjectInputStream */
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException ex) {
				logError("Error closing the PID file input stream. IO Exception:  " + ex.getMessage(), ex);
				ex.printStackTrace();
			}
		}
		return pids;
	}

	/**
	 * Log error with the specified message parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @see #logError(String,Throwable)
	 */
	public void logError(final String message) {
		this.logger.error(message);
	}

	/**
	 * Log error with the specified message and t parameters.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @param t
	 *            The t (<code>Throwable</code>) parameter.
	 * @see #logError(String)
	 */
	public void logError(final String message, final Throwable t) {
		this.logger.error(message, t);
	}

	/**
	 * Log info with the specified message parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @see #logInfo(String,Throwable)
	 */
	public void logInfo(final String message) {
		this.logger.info(message);
	}

	/**
	 * Log info with the specified message and t parameters.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @param t
	 *            The t (<code>Throwable</code>) parameter.
	 * @see #logInfo(String)
	 */
	public void logInfo(final String message, final Throwable t) {
		this.logger.info(message, t);
	}

	/**
	 * Find the PwmConfiguration in the configuration set that matches the OSGi
	 * configuration. If the osgi configuration instance is a factory instance
	 * there is a match when the the id and the factory pid are equal (and pid
	 * is ignored). For singleton configurations we compare the pid and the id.
	 * 
	 * @param configurationSet
	 *            The configuration set (<code>Set<ConfigurationSpec></code>)
	 *            parameter.
	 * @param configuration
	 *            The configuration (<code>Configuration</code>) parameter.
	 * @return The matching PwmConfiguration
	 */
	private ConfigurationSpec matchConfiguration(final Set<ConfigurationSpec> configurationSet,
			final Configuration configuration) {
		String clusterId = (String) configuration.getProperties().get(ConfigPropertyConstants.CONF_PROP_CLUSTER_ID);
		String id = (String) configuration.getProperties().get(ConfigPropertyConstants.CONF_PROP_ID);
		String pid = configuration.getPid();
		String fpid = configuration.getFactoryPid();
		if (clusterId != null && id != null) {
			/* Determine the OSGi configuration type */
			ConfigurationType type = (fpid == null) ? ConfigurationSpec.ConfigurationType.singleton
					: ConfigurationSpec.ConfigurationType.factory;
			for (ConfigurationSpec configurationSpec : configurationSet) {
				String configSpecId = (String) configurationSpec.getId();
				if (clusterId.equals(configurationSpec.getClusterId()) &&
						(
								(ConfigurationType.factory.equals(type) && id.equals(configSpecId) && configurationSpec.getPid().equals(fpid))
								|| (ConfigurationType.singleton.equals(type) && configurationSpec.getPid().equals(pid))
						)
					) {
					return configurationSpec;
				}
			}
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("No matching configuration spec for found for id:" + id + " pid:" + pid + " fpid:" + fpid
						+ " type:" + type.toString());
			}
		}
		return null;
	}

	/**
	 * Process update with the specified configuration specs parameter.
	 * 
	 * @param configSpecs
	 *            The configuration specs (<code>Set<ConfigurationSpec></code>)
	 *            parameter.
	 */
	public void processUpdate(final Set<ConfigurationSpec> configSpecs) {
		/* New pid set */
		Set<String> newPidSet = new HashSet<String>();
		/* Read the pids of the current configuration from file */
		this.pidSet = deserializePids(this.configurationPidFile);
		/* First get current configuration */
		Set<Configuration> currentConfigs = currentOSGiConfigurations();
		/* Create a working copy of the new configuration definition */
		Set<ConfigurationSpec> workingSet = new HashSet<ConfigurationSpec>(configSpecs);
		/* Process the changes */
		ConfigurationSpec configSpec = null;
		for (Configuration configuration : currentConfigs) {
			/* Find the config spec that matches the configuration */
			if ((configSpec = matchConfiguration(workingSet, configuration)) != null) {
				/* If configuration specification has been */
				/* changed update the configuration */
				if (!configSpec.equals(configuration)) {
					updateConfiguration(configuration, configSpec);
				}
				workingSet.remove(configSpec);
				newPidSet.add(configuration.getPid());
			} else {
				/* Configuration should be deleted */
				deleteConfiguration(configuration);
			}
		}
		/* For the remaining configurations in the working set create the */
		/* configuration */
		for (ConfigurationSpec configurationSpec : workingSet) {
			String pid = createConfiguration(configurationSpec);
			newPidSet.add(pid);
		}
		/* Persist the new pids */
		this.pidSet = newPidSet;
		serializePids(this.configurationPidFile, this.pidSet);
	}

	/**
	 * Serialize pids with the specified file and pids parameters.
	 * 
	 * @param file
	 *            The file (<code>File</code>) parameter.
	 * @param pids
	 *            The pids (<code>Set<String></code>) parameter.
	 */
	private void serializePids(final File file, final Set<String> pids) {
		logInfo("Persisting configuration PIDs to a file.");

		OutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(file);
			out = new ObjectOutputStream(fos);

			for (String pid : pids) {
				out.writeObject(pid);
			}
		} catch (FileNotFoundException e) {
			logError("Configuration Manager Agent could not persist the PIDs. Reason: " + e.getMessage(), e);
		} catch (IOException e) {
			logError("Configuration Manager Agent could not persist the PIDs. Reason: " + e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logError("I/O Error while closing file resource. Reason: " + e.getMessage(), e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logError("I/O Error while closing file resource. Reason: " + e.getMessage(), e);
				}
			}

		}
	}

	/**
	 * To dictionary with the specified map parameter and return the
	 * Dictionary<String,Object> result.
	 * 
	 * @param map
	 *            The map (<code>Map<String,Object></code>) parameter.
	 * @return Results of the to dictionary (
	 *         <code>Dictionary<String,Object></code>) value.
	 */
	private Dictionary<String, Object> toDictionary(final Map<String, Object> map) {
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		ht.putAll(map);
		return ht;
	}

	/**
	 * Update configuration with the specified configuration and configuration
	 * spec parameters and return the String result.
	 * 
	 * @param configuration
	 *            The configuration (<code>Configuration</code>) parameter.
	 * @param configSpec
	 *            The configuration spec (<code>ConfigurationSpec</code>)
	 *            parameter.
	 * @return Results of the update configuration (<code>String</code>) value.
	 */
	private String updateConfiguration(final Configuration configuration, final ConfigurationSpec configSpec) {
		logInfo("Updating configuration pid: " + configuration.getPid() + " type: " + configSpec.getType() + " id:"
				+ configuration.getProperties().get("id") + " cluster:" + configuration.getProperties().get("cluster.id"));
		Dictionary<String, Object> properties = new Hashtable<String, Object>(configSpec.getProperties());
		try {
			this.adminServiceUtil.createOrUpdateConfiguration(configuration.getPid(), configuration.getBundleLocation(),
					properties);
		} catch (final IOException e) {
			logError("IO Exception occurred while updating configuration: " + configuration + ". Reason: " + e.getMessage(), e);
		}
		return configuration.getPid();
	}

}
