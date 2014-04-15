package net.powermatcher.core.config.management.agent;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.SAXParserFactory;

import net.powermatcher.core.config.ConfigurationSpec;
import net.powermatcher.core.config.ConfigurationSpecUtil;
import net.powermatcher.core.config.parser.SystemConfigurationParser;
import net.powermatcher.core.config.parser.SystemConfigurationParserException;
import net.powermatcher.core.config.processor.ConfigurationProcessor;
import net.powermatcher.core.configurable.ConfigurableObject;
import net.powermatcher.core.configurable.service.ConfigurationService;

import org.apache.commons.codec.binary.Base64;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigManager extends ConfigurableObject {
	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class ConfigUpdater extends TimerTask {
		/**
		 * Run.
		 */
		@Override
		public void run() {
			try {
				doUpdate();
			} catch (final Throwable t) {
				ConfigManager.this.logger.error("Error during update processing", t);
			}
		}

	}

	/**
	 * Define the connection time out for retrieving the configuration data.
	 */
	private static final int CONNECTION_TIME_OUT = 30000;
	/**
	 * Define the read time out for reading the configuration data.
	 */
	private static final int READ_TIME_OUT = 30000;
	/**
	 * Define the https protocol constant string.
	 */
	private static final String URL_PROTOCOL_FILE = "file:";
	/**
	 * Define the encoding standard to be used in http requests.
	 */
	private static final String URL_ENCODING = "UTF-8";
	/**
	 * User name for the task account for retrieving the configuration data.
	 */
	private String configServerUserName;
	/**
	 * Password for the task account for retrieving the configuration data.
	 */
	private String configServerPassword;
	/**
	 * Identifier that identifies the the node that the ConfigManager is
	 * responsible for.
	 */
	private String nodeId;
	/**
	 * Define the logger (Logger) field.
	 */
	private Logger logger;
	/**
	 * Define the Configuration processor for handling configuration updates.
	 */
	private ConfigurationProcessor processor;
	/**
	 * Define the parser factory for the configuration xml file.
	 */
	private SAXParserFactory factory;
	/**
	 * Define the field for the configuration parser.
	 */
	private SystemConfigurationParser parser;
	/**
	 * Define the field for the configuration data URL.
	 */
	private String configurationDataURL;
	/**
	 * Define the field for the PID file instance.
	 */
	private File pidFile;
	/**
	 * Define the field for the OSGi ConfigurationAdmin service.
	 */
	private ConfigurationAdmin configurationAdmin;
	/**
	 * Define the update timer (Timer) field.
	 */
	private Timer updateTimer;
	/**
	 * Define the update task (ConnectTask) field.
	 */
	private ConfigUpdater updateTask;
	/**
	 * Define the update interval (int) field.
	 */
	private int updateInterval;

	/**
	 * Constructs an instance of this class from the specified configuration
	 * admin, factory, PID file and configuration parameters.
	 * 
	 * @param configurationAdmin
	 *            The configuration admin (<code>ConfigurationAdmin</code>)
	 *            parameter.
	 * @param factory
	 *            The factory (<code>SAXParserFactory</code>) parameter.
	 * @param pidFile
	 *            The PID file (<code>File</code>) parameter.
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public ConfigManager(final ConfigurationAdmin configurationAdmin, final SAXParserFactory factory, final File pidFile,
			final ConfigurationService configuration) {
		super(configuration);
		this.logger = LoggerFactory.getLogger(getClass().getName());
		this.configurationAdmin = configurationAdmin;
		this.factory = factory;
		this.pidFile = pidFile;
	}

	/**
	 * Get urlinput stream with the specified URL string, encoding, username and
	 * password parameters and return the InputStream result.
	 * 
	 * @param urlString
	 *            The URL string (<code>String</code>) parameter.
	 * @param encoding
	 *            The encoding (<code>String</code>) parameter.
	 * @param username
	 *            The username (<code>String</code>) parameter.
	 * @param password
	 *            The password (<code>String</code>) parameter.
	 * @return Results of the get urlinput stream (<code>InputStream</code>)
	 *         value.
	 * @throws IOException
	 *             IOException.
	 */
	private InputStream getURLInputStream(final String urlString, final String encoding, final String username,
			final String password) throws IOException {
		URL url = null;
		URLConnection conn = null;
		this.logger.info("Retrieving configuration '" + this.nodeId + "' from URL: " + urlString);
		/* Different handling for file and other protocols */
		if (urlString.trim().startsWith(URL_PROTOCOL_FILE)) {
			url = new URL(urlString);
			conn = url.openConnection();
		} else {
			/* Build query parameter string */
			String query = "?nodeid=" + URLEncoder.encode(this.nodeId, URL_ENCODING);
			/* Create the url combined with the parameter */
			url = new URL(urlString + query);
			/* Open the URL connection */
			conn = url.openConnection();
			conn.setConnectTimeout(CONNECTION_TIME_OUT);
			conn.setReadTimeout(READ_TIME_OUT);
			conn.setUseCaches(false);
			/* Make sure underlying connection is closed and not reused */
			conn.setRequestProperty("Connection", "close");
			/* User id and password for basic authentication */
			if (username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
				String credentials = username + ":" + password;
				String basicAuth = "Basic " + new String(new Base64().encode(credentials.getBytes()));
				conn.setRequestProperty("Authorization", basicAuth);
			}
		}
		/* Get the response */
		return conn.getInputStream();
	}

	/**
	 * Sets or updates the configuration of the configuration manager itself.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #getConfiguration()
	 */
	@Override
	public void setConfiguration(ConfigurationService configuration) {
		super.setConfiguration(configuration);
		init();
	}

	/**
	 * Init.
	 */
	private void init() {
		this.nodeId = getProperty(ConfigManagerConfiguration.CONFIGURATION_NODE_ID_PROPERTY,
				ConfigManagerConfiguration.CONFIGURATION_NODE_ID_DEFAULT);
		this.updateInterval = getProperty(ConfigManagerConfiguration.UPDATE_INTERVAL_PROPERTY,
				ConfigManagerConfiguration.UPDATE_INTERVAL_DEFAULT);
		this.configurationDataURL = getProperty(ConfigManagerConfiguration.CONFIGURATION_DATA_URL_PROPERTY,
				ConfigManagerConfiguration.CONFIGURATION_DATA_URL_DEFAULT);
		this.configServerUserName = getProperty(ConfigManagerConfiguration.CONFIGURATION_DATA_USERNAME_PROPERTY, (String) null);
		this.configServerPassword = getProperty(ConfigManagerConfiguration.CONFIGURATION_DATA_PASSWORD_PROPERTY, (String) null);
		this.parser = new SystemConfigurationParser(this.factory);
		this.updateTimer = new Timer("ConfigManager-Updater-" + this.nodeId, true);
	}

	/**
	 * Start the configuration manager.
	 */
	public void start() {
		if (this.updateTask == null) {
			this.updateTask = new ConfigManager.ConfigUpdater();
			this.updateTimer.schedule(this.updateTask, 0, this.updateInterval * 1000l);
		}
	}

	/**
	 * Stop the configuration manager.
	 */
	public void stop() {
		if (this.updateTask != null) {
			this.updateTask.cancel();
			this.updateTask = null;
		}
	}

	/**
	 * Restart the configuration manager.
	 */
	public void restart() {
		if (this.updateTask != null) {
			this.updateTask.cancel();
			this.updateTask = new ConfigManager.ConfigUpdater();
			long period = this.updateInterval * 1000l;
			this.updateTimer.schedule(this.updateTask, 0, period);
		}
	}

	/**
	 * Update OSGi runtime configuration.
	 */
	public synchronized void doUpdate() {
		Set<ConfigurationSpec> configurations;
		InputStream is = null;

		this.logger.info("Updating runtime configuration.");

		try {
			is = getURLInputStream(this.configurationDataURL, URL_ENCODING, this.configServerUserName,
					this.configServerPassword);
			configurations = this.parser.parse(is);
			this.processor = new ConfigurationProcessor(this.configurationAdmin, this.pidFile);
			this.processor.processUpdate(ConfigurationSpecUtil.configurationSet(configurations));
		} catch (SystemConfigurationParserException e) {
			this.logger.error("Parsing exception occured while updating configuration.", e);
		} catch (IOException e) {
			this.logger.error("IO exception occured while updating configuration.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					this.logger.error("Error closing input stream.", e);
				}
			}
		}
	}

}
