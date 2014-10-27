package net.powermatcher.core.launcher.main;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.powermatcher.core.adapter.service.AdapterFactoryService;
import net.powermatcher.core.adapter.service.Adaptable;
import net.powermatcher.core.adapter.service.Connectable;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.ConfigurableObject;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.core.configurable.service.ConfigurableService;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Starts a PowerMatcher demo application.
 * <p>
 * This class defines a Java main application that starts a PowerMatcher
 * demo application. The application expects a configuration file named
 * agent_config.properties in the current directory.
 * <p>
 * Running this application requires an active broker. 
 * 
 * @author IBM
 * @version 0.9.0
 */
public class Main {

	public static final String ID_PROPERTY_SUFFIX = Configurable.SEPARATOR + "id";
	public static final String CLASS_PROPERTY_SUFFIX = Configurable.SEPARATOR + "class";
	public static final String AGENT_PROPERTY_PREFIX = "agent" + Configurable.SEPARATOR;
	public static final String ADAPTER_FACTORY_PROPERTY_PREFIX = "adapter.factory" + Configurable.SEPARATOR;

	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * Main with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	public static void main(final String[] args) {
		new Main().run(args);
	}

	/**
	 * Define the agent configuration file (String) field.
	 */
	protected String agentConfigFile = "agent_config.properties";
	/**
	 * Define the broker configuration file (String) field.
	 */
	protected String brokerConfigFile = "broker_config.properties";
	/**
	 * Define the agent properties (Properties) field.
	 */
	private Properties agentProperties;

	/**
	 * Define the adapter manager (AdapterManager) field.
	 */
	private AdapterManager adapterManager = null;

	private Map<String, AdapterFactoryService<Connectable>> adapterFactories;

	private ConnectorRegistry connectorRegistry;

	/**
	 * Constructs an instance of this class.
	 */
	public Main() {
	}

	/**
	 * Configure agent with the specified adapters, agent ID and agent
	 * parameters.
	 * 
	 * @param adapters
	 *            The adapters (<code>Set<AdapterService></code>) parameter.
	 * @param id
	 *            The agent ID (<code>String</code>) parameter.
	 * @param configurable
	 *            The agent (<code>Agent</code>) parameter.
	 * @throws ClassNotFoundException 
	 * @throws InstantiationException 
	 * @throws Exception 
	 */
	protected void configureAgent(final Set<Adaptable> adapters, final String id, final ConfigurableService configurable) throws Exception {
		Configurable configuration = createAgentConfiguration(id);
		configurable.setConfiguration(configuration);

		if (configurable instanceof Connectable) {
			createAdapters(adapters, configuration, (Connectable)configurable);
		}

	}

	/**
	 * Recursively create adapters for connectable object.
	 * @param adapters
	 * @param configuration
	 * @param connector
	 * @throws Exception
	 */
	private void createAdapters(final Set<Adaptable> adapters, Configurable configuration,
			Connectable connector) throws Exception {
		Class<? extends Connectable> connectorTypes[] = connector.getConnectorTypes();
		for (int i = 0; i < connectorTypes.length; i++) {
			String[] adapterFactoryIds = connector.getAdapterFactory(connectorTypes[i]);
			for (int adapterIndex = 0; adapterIndex < adapterFactoryIds.length; adapterIndex++) {
				String adapterFactoryId = adapterFactoryIds[adapterIndex];
				if (adapterFactoryId.length() > 0) {
					AdapterFactoryService<Connectable> adapterFactory = this.adapterFactories.get(adapterFactoryId);
					if (adapterFactory == null) {
						throw new Exception("Undefined adapter factory '" + adapterFactoryId + "' for " + connectorTypes[i].getSimpleName() + " of " + connector.getConnectorId());
					}
					Configurable factoryConfiguration = createAdapterConfiguration(adapterFactoryId, configuration);
					Adaptable adapter = adapterFactory.createAdapter(factoryConfiguration, connector, this.connectorRegistry, adapterIndex);
					adapters.add(adapter);
					if (adapter instanceof Connectable) {
						createAdapters(adapters, factoryConfiguration, (Connectable)adapter);
					}
				}
			}
		}
	}

	/**
	 * Create agent configuration with the specified agentId parameter and return the
	 * ConfigurationService result.
	 * 
	 * @param id
	 *            The agent ID (<code>String</code>) parameter.
	 * @return Results of the create configuration (
	 *         <code>ConfigurationService</code>) value.
	 */
	protected Configurable createAgentConfiguration(final String id) {
		String prefix = AGENT_PROPERTY_PREFIX + id;
		return new PrefixedConfiguration(this.agentProperties, prefix);
	}

	/**
	 * Create adapter configuration with the specified agentId and parent parameters
	 * and return the ConfigurationService result.
	 * The adapter receives the configuration of the connectable object, with the factory
	 * configuration specifying the property defaults.
	 * 
	 * @param adapterFactoryId
	 *            The adapter factory ID (<code>String</code>) parameter.
	 * @param connectableConfiguration
	 *            The configuration of the parent agent or adapter (<code>ConfigurationService</code>).
	 * 
	 * @return Results of the create configuration (
	 *         <code>ConfigurationService</code>) value.
	 */
	protected Configurable createAdapterConfiguration(final String adapterFactoryId, final Configurable connectableConfiguration) {
		String prefix = ADAPTER_FACTORY_PROPERTY_PREFIX + adapterFactoryId;
		Configurable adapterConfiguration = new PrefixedConfiguration(connectableConfiguration, this.agentProperties, prefix);
		/*
		 * If the connectable object explicitly specifies connector.id, the adapter must be
		 * create with id = connector.id of connectable. This allows shared adapters (like for
		 * example for messaging connections) to be created by specifying the same connector.id
		 * in different connecables.
		 */
		String connectorId = (String)connectableConfiguration.getProperty(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY, (String)null);
		if (connectorId != null) {
			Map<String, Object> adapterProperties = new HashMap<String, Object>();
			adapterProperties.put(IdentifiableObjectConfiguration.ID_PROPERTY, connectorId);
			adapterProperties.put(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY, connectableConfiguration.getOptionalProperty(ConnectableObjectConfiguration.CONNECTOR_ID_PROPERTY));
			adapterConfiguration = new BaseConfiguration(adapterConfiguration, adapterProperties);
		}
		return adapterConfiguration;
	}

	/**
	 * Gets the adapters (Set<AdapterService>) value.
	 * 
	 * @return The adapters (<code>Set<AdapterService></code>) value.
	 * @throws Exception
	 */
	protected Set<Adaptable> getAdapters() throws Exception {
		Set<Adaptable> adapters = new HashSet<Adaptable>();
		for (String propertyName : this.agentProperties.stringPropertyNames()) {
			if (propertyName.startsWith(AGENT_PROPERTY_PREFIX) && propertyName.endsWith(CLASS_PROPERTY_SUFFIX)) {
				String idPropertyName = propertyName.substring(0, propertyName.lastIndexOf(Configurable.SEPARATOR)) + ID_PROPERTY_SUFFIX;
				String id = this.agentProperties.getProperty(idPropertyName);
				Class<?> cls = Class.forName(this.agentProperties.getProperty(propertyName));
				ConfigurableObject configurable = (ConfigurableObject) cls.newInstance();
				configureAgent(adapters, id, configurable);
				if (configurable instanceof Connectable) {
					this.connectorRegistry.add((Connectable)configurable);
				}
			}
		}
		return adapters;
	}

	/**
	 * Gets the adapter factories (Map<String, AdapterFactoryService<ConnectorService>) value.
	 * 
	 * @return The adapter factories (<code>Map<String, AdapterFactoryService<ConnectorService></code>) value.
	 * @throws Exception
	 */
	protected Map<String, AdapterFactoryService<Connectable>> getAdapterFactories() throws Exception {
		Map<String, AdapterFactoryService<Connectable>> adapterFactories = new HashMap<String, AdapterFactoryService<Connectable>>();
		for (String propertyName : this.agentProperties.stringPropertyNames()) {
			if (propertyName.startsWith(ADAPTER_FACTORY_PROPERTY_PREFIX) && propertyName.endsWith(CLASS_PROPERTY_SUFFIX)) {
				String factoryId = propertyName.substring(ADAPTER_FACTORY_PROPERTY_PREFIX.length(), propertyName.length() - CLASS_PROPERTY_SUFFIX.length());
				String adapterFactoryClass = this.agentProperties.getProperty(propertyName);
				if (adapterFactoryClass != null && adapterFactoryClass.length() > 0) {
					try {
						Class<?> cls = Class.forName(adapterFactoryClass);
						@SuppressWarnings("unchecked")
						AdapterFactoryService<Connectable> factory = (AdapterFactoryService<Connectable>) cls.newInstance();
						adapterFactories.put(factoryId, factory);
					} catch (Exception e) {
						throw new Exception("Could not create factory for " + propertyName + "=" + adapterFactoryClass, e);
					}
				}
			}
		}
		return adapterFactories;
	}

	/**
	 * Load properties with the specified file name parameter.
	 * 
	 * @param fileName
	 *            The file name (<code>String</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	protected Properties loadProperties(final String fileName, final boolean required) throws IOException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(fileName));
			logger.info("Loaded properties from: " + fileName);
		} catch (final IOException e) {
			if (required) {
				logger.error("Could not open properties file: " + fileName, e);
				throw e;
			} else {
				logger.info("Using property defaults for : " + fileName);
			}
		}
		return properties;
	}

	/**
	 * Read command line arguments with the specified arguments parameter and
	 * return the boolean result.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 * @return Results of the read command line arguments (<code>boolean</code>)
	 *         value.
	 */
	private boolean readCommandLineArgs(final String[] args) {
		for (int index = 0; index < args.length; ++index) {
			if (args[index].equalsIgnoreCase("-agentConfig") && index + 1 < args.length) {
				this.agentConfigFile = args[++index];
			} else if (args[index].equalsIgnoreCase("-brokerConfig") && index + 1 < args.length) {
				this.brokerConfigFile = args[++index];
			} else {
				logger.error("Usage: DemoApp [-agentConfig <file>]");
				return false;
			}
		}
		return true;
	}

	/**
	 * Run with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	protected void run(final String[] args) {
		boolean success = readCommandLineArgs(args);
		if (success) {
			try {
				startBrokerManager(loadProperties(this.brokerConfigFile, false));
				startAdapterManager(loadProperties(this.agentConfigFile, true));
			} catch (final Exception e) {
				success = false;
			}
		}
	}

	/**
	 * Start adapter manager.
	 * @param agentProperties 
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	protected void startAdapterManager(Properties agentProperties) throws Exception {
		try {
			logger.info("Creating and configuring the adapters");
			this.agentProperties = agentProperties;
			this.adapterManager = new AdapterManager();
			this.connectorRegistry = new ConnectorRegistry();
			this.adapterFactories = getAdapterFactories();
			Set<Adaptable> adapters = getAdapters();
			this.adapterManager.setAdapters(adapters);
		} catch (final Exception e) {
			logger.error("Could not create the adapters", e);
			throw e;
		}
		try {
			logger.info("Starting the adapter manager and adapters");
			this.adapterManager.start();
		} catch (final Exception e) {
			logger.error("Could not start the adapters", e);
			throw e;
		}
	}

	/**
	 * Start message broker manager.
	 * @param brokerProperties 
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	protected void startBrokerManager(Properties brokerProperties) throws Exception {
		logger.info("Starting application with external broker");
	}

}
