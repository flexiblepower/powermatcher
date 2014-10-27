package net.powermatcher.core.config.management.agent.component;


import java.io.File;
import java.util.Collections;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import net.powermatcher.core.config.management.agent.ConfigManager;
import net.powermatcher.core.config.management.agent.ConfigManagerConfiguration;
import net.powermatcher.core.config.management.agent.adapter.ConfigUpdateMessagingAdapter;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.service.MessagingConnectorService;

import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;


/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = ConfigManagerComponent.COMPONENT_NAME, designate = ConfigManagerComponentConfiguration.class, configurationPolicy = ConfigurationPolicy.optional, immediate = true)
public class ConfigManagerComponent extends ConfigUpdateMessagingAdapter implements MessagingConnectorService {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.config.management.agent.ConfigManager";
	/**
	 * Define the file name constant of the file that stores the current
	 * configuration PIDs.
	 */
	public final static String PID_PERSISTENCY_FILE = "pid.persistency";
	/**
	 * Define the field for the OSGi Configuration Admin service dependency.
	 */
	private ConfigurationAdmin configurationAdmin;
	/**
	 * Define the field for the parser (SAXParserFactory) factory dependency.
	 */
	private SAXParserFactory factory;
	/**
	 * Define the field for the configuration manager (ConfigManager).
	 */
	private ConfigManager configManager;

	/**
	 * Activate with the specified properties and context parameters.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 * @param context
	 *            The context (<code>ComponentContext</code>) parameter.
	 */
	@Activate
	void activate(final Map<String, Object> properties, final ComponentContext context) {
		logInfo("Starting Configuration Manager");
		Configurable configuration = createConfiguration(properties);
		setConfiguration(configuration);
		File pidFile = getPidFile(context, PID_PERSISTENCY_FILE);
		this.configManager = new ConfigManager(this.configurationAdmin, this.factory, pidFile, configuration);
		this.configManager.start();
		if (isEnabled()) {
			try {
				bind();
			} catch (final Exception e) {
				logError("Failed to bind Configuration Manager Messaging Adapter", e);
			}
		}
	}

	/**
	 * Create the configuration from the combination of the component properties and the system properties.
	 * @param properties The component properties.
	 * @return The combination of the component properties.
	 */
	private Configurable createConfiguration(final Map<String, Object> properties) {
		Configurable contextConfiguration = new PrefixedConfiguration(System.getProperties(),
				ConfigManagerConfiguration.PROPERTY_PREFIX);
		return new BaseConfiguration(contextConfiguration, properties);
	}

	/**
	 * Modified with the specified properties.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 */
	@Modified
	void modified(final ComponentContext context, final Map<String, Object> properties) {
		logInfo("Updating Configuration Manager");
		Configurable configuration = createConfiguration(properties);
		if (isEnabled()) {
			unbind();
		}
		setConfiguration(configuration);
		this.configManager.setConfiguration(configuration);
		this.configManager.restart();
		if (isEnabled()) {
			try {
				bind();
			} catch (final Exception e) {
				logError("Failed to bind Configuration Manager Messaging Adapter", e);
			}
		}
	}

	/**
	 * Deactivate.
	 */
	@Deactivate
	void deactivate() {
		logInfo("Stopping Configuration Manager");
		if (isEnabled()) {
			unbind();
		}
		this.configManager.stop();
		this.configManager = null;
	}

		
	/* (non-Javadoc)
	 * @see net.powermatcher.core.messaging.framework.MessagingAdapter#bind()
	 */
	@Override
	public void bind() throws Exception {
		setConfigManager(this.configManager);
		super.bind();
	}

	/**
	 * Get or create the PID file instance from the bundle context.
	 * 
	 * @param context
	 *            The context (<code>ComponentContext</code>) parameter.
	 * @param filename
	 *            The filename (<code>String</code>) parameter.
	 * @return Results of the get PID file (<code>File</code>) value.
	 */
	private File getPidFile(final ComponentContext context, final String filename) {
		BundleContext bundleContext = context.getBundleContext();
		File pidFile = bundleContext.getDataFile(filename);
		return pidFile;
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		String id = configuration.getProperty(net.powermatcher.core.object.config.ConnectableObjectConfiguration.ID_PROPERTY, (String) null);
		if (id == null) {
			String nodeId = configuration.getProperty(ConfigManagerConfiguration.CONFIGURATION_NODE_ID_PROPERTY,
					ConfigManagerConfiguration.CONFIGURATION_NODE_ID_DEFAULT);
			Map<String, Object> defaultProps = Collections.singletonMap(
					net.powermatcher.core.object.config.ConnectableObjectConfiguration.ID_PROPERTY, (Object) nodeId);
			super.setConfiguration(new BaseConfiguration(configuration, defaultProps));
		} else {
			super.setConfiguration(configuration);
		}
	}

	/**
	 * Sets the configuration admin value.
	 * 
	 * @param configurationAdmin
	 *            The configuration admin (<code>ConfigurationAdmin</code>)
	 *            parameter.
	 */
	@Reference
	public void setConfigurationAdmin(final ConfigurationAdmin configurationAdmin) {
		this.configurationAdmin = configurationAdmin;
	}

	/**
	 * Sets the factory value.
	 * 
	 * @param factory
	 *            The factory (<code>SAXParserFactory</code>) parameter.
	 */
	@Reference
	public void setFactory(final SAXParserFactory factory) {
		this.factory = factory;
	}

}
