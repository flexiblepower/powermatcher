package net.powermatcher.expeditor.broker.manager;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.ConfigurableObject;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.expeditor.broker.manager.config.BrokerManagerConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.micro.admin.AdminException;
import com.ibm.micro.admin.Broker;
import com.ibm.micro.admin.BrokerDefinition;
import com.ibm.micro.admin.BrokerFactory;
import com.ibm.micro.admin.LocalBroker;
import com.ibm.micro.admin.PersistenceDefinition;
import com.ibm.micro.admin.QueuingDefinition;

/**
 * @author IBM
 * @version 0.9.0
 */
public class BrokerManager extends ConfigurableObject {
	/**
	 * Define the wait loop polling delay (long) constant.
	 */
	private static final long WAIT_LOOP_POLLING_DELAY = 100;
	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = LoggerFactory.getLogger(BrokerManager.class);
	/**
	 * Define the broker (LocalBroker) field.
	 */
	private LocalBroker broker;
	/**
	 * Define the queue size (int) field.
	 */
	private int queueSize;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #BrokerManager(ConfigurationService)
	 */
	public BrokerManager() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #BrokerManager()
	 */
	public BrokerManager(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Create broker with the specified broker definition parameter and return
	 * the LocalBroker result.
	 * 
	 * @param brokerDefinition
	 *            The broker definition (<code>BrokerDefinition</code>)
	 *            parameter.
	 * @return Results of the create broker (<code>LocalBroker</code>) value.
	 * @throws AdminException
	 *             Admin Exception.
	 * @see #getBroker()
	 * @see #startBroker()
	 * @see #startBroker(boolean)
	 * @see #stopBroker()
	 */
	private LocalBroker createBroker(final BrokerDefinition brokerDefinition) throws AdminException {
		BrokerFactory factory = this.getFactory();
		LocalBroker broker = null;
		if ((brokerDefinition != null) && (factory != null)) {
			broker = factory.create(brokerDefinition);
		}
		return (broker);
	}

	/**
	 * Create broker definition with the specified directory and name parameters
	 * and return the BrokerDefinition result.
	 * 
	 * @param directory
	 *            The directory (<code>String</code>) parameter.
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the create broker definition (
	 *         <code>BrokerDefinition</code>) value.
	 */
	private BrokerDefinition createBrokerDefinition(final String directory, final String name) {
		BrokerFactory factory = getFactory();
		/* Create BrokerDefinition */
		BrokerDefinition brokerDefinition = factory.createBrokerDefinition(name);
		/* Set data directory */
		brokerDefinition.setDataDirectory(directory);
		/* Set maximum message size */
		brokerDefinition.setMaximumMessageSize(this.getMaxMessageSize());
		/* Set persistence definition */
		PersistenceDefinition persistenceDefinition = createPersistenceDefinition(brokerDefinition);
		brokerDefinition.setPersistenceDefinition(persistenceDefinition);
		/* Maximum number of clients */
		int maxNumberOfClients = this.getMaxNumberOfClients();
		// must check for default setting of -1, Microbroker defaults to -1 if
		// not set, but disallows explicitly setting to default of -1, so only
		// set if non-default
		if (maxNumberOfClients != BrokerManagerConfiguration.MAX_NUMBER_OF_CLIENTS_DEFAULT) {
			brokerDefinition.setMaxNumberOfClients(maxNumberOfClients);
		}
		/* Remote port */
		brokerDefinition.setPort(this.getRemotePort());
		/* Pass by reference */
		brokerDefinition.setLocalOptimizationEnabled(true);
		/* Disable auto-start */
		brokerDefinition.setAutoStartEnabled(false);
		this.queueSize = getQueueSize();
		if (this.queueSize > 0) {
			QueuingDefinition qd = brokerDefinition.getQueuingDefinition();
			qd.setMaximumDepthDefault(this.queueSize);
			// brokerDefinition.setQueuingDefinition(qd); is this necessary?
		}
		brokerDefinition.setSecurityEnabled(getSecurityEnabled());
		return brokerDefinition;
	}

	/**
	 * Create persistence definition with the specified broker definition
	 * parameter and return the PersistenceDefinition result.
	 * 
	 * @param brokerDefinition
	 *            The broker definition (<code>BrokerDefinition</code>)
	 *            parameter.
	 * @return Results of the create persistence definition (
	 *         <code>PersistenceDefinition</code>) value.
	 */
	private PersistenceDefinition createPersistenceDefinition(final BrokerDefinition brokerDefinition) {
		PersistenceDefinition persistenceDefinition = brokerDefinition.getPersistenceDefinition();
		persistenceDefinition.setPersistenceType(this.getPersistence());
		return persistenceDefinition;
	}

	/**
	 * Delete broker with the specified directory and name parameters and return
	 * the boolean result.
	 * 
	 * @param directory
	 *            The directory (<code>String</code>) parameter.
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the delete broker (<code>boolean</code>) value.
	 * @see #getBroker()
	 * @see #startBroker()
	 * @see #startBroker(boolean)
	 * @see #stopBroker()
	 */
	private boolean deleteBroker(final String directory, final String name) {
		boolean deleted = false;
		try {
			this.getFactory().delete(name);
			deleted = true;
		} catch (AdminException e) {
			logger.error("Error deleting broker", e);
			/*
			 * In case there is a problem deleting the broker, 
			 * remove it by deleting its definition file.
			 */
			File file = new File(directory, name);
			deleted = deleteFile(file);
		}
		return deleted;
	}

	/**
	 * Delete file with the specified file parameter and return the boolean
	 * result.
	 * 
	 * @param file
	 *            The file (<code>File</code>) parameter.
	 * @return Results of the delete file (<code>boolean</code>) value.
	 */
	private boolean deleteFile(final File file) {
		boolean result = true;
		boolean valid = file.exists();
		if (valid == true) {
			boolean isDirectory = file.isDirectory();
			if (isDirectory == true) {
				String[] filenames = file.list();
				int count = filenames.length;
				int i = 0;
				while (result == true && i < count) {
					String filename = filenames[i];
					File child = new File(file, filename);
					result = deleteFile(child);
					i++;
				}
			}
			if (result == true) {
				result = file.delete();
			}
		}
		return result;
	}

	/**
	 * Gets the broker value.
	 * 
	 * @return The broker (<code>Broker</code>) value.
	 * @see #startBroker()
	 * @see #startBroker(boolean)
	 * @see #stopBroker()
	 */
	public Broker getBroker() {
		return this.broker;
	}

	/**
	 * Gets the broker wait timeout (long) value.
	 * 
	 * @return The broker wait timeout (<code>long</code>) value.
	 */
	private long getBrokerWaitTimeout() {
		return getProperty(BrokerManagerConfiguration.BROKER_WAIT_TIMEOUT_PROPERTY,
				BrokerManagerConfiguration.BROKER_WAIT_TIMEOUT_DEFAULT);
	}

	/**
	 * Gets the dir (String) value.
	 * 
	 * @return The dir (<code>String</code>) value.
	 */
	private String getDir() {
		return getProperty(BrokerManagerConfiguration.BROKER_DIR_PROPERTY, BrokerManagerConfiguration.DEFAULT_DIR);
	}

	/**
	 * Gets the factory (BrokerFactory) value.
	 * 
	 * @return The factory (<code>BrokerFactory</code>) value.
	 */
	protected BrokerFactory getFactory() {
		return BrokerFactory.INSTANCE;
	}

	/**
	 * Gets the max message size (int) value.
	 * 
	 * @return The max message size (<code>int</code>) value.
	 */
	public int getMaxMessageSize() {
		return getProperty(BrokerManagerConfiguration.MAX_MESSAGE_SIZE_PROPERTY,
				BrokerManagerConfiguration.MAX_MESSAGE_SIZE_DEFAULT);
	}

	/**
	 * Gets the max number of clients (int) value.
	 * 
	 * @return The max number of clients (<code>int</code>) value.
	 */
	private int getMaxNumberOfClients() {
		return getProperty(BrokerManagerConfiguration.MAX_NUMBER_OF_CLIENTS_PROPERTY,
				BrokerManagerConfiguration.MAX_NUMBER_OF_CLIENTS_DEFAULT);
	}

	/**
	 * Gets the name (String) value.
	 * 
	 * @return The name (<code>String</code>) value.
	 */
	public String getName() {
		return getProperty(BrokerManagerConfiguration.BROKER_NAME_PROPERTY, BrokerManagerConfiguration.BROKER_NAME_DEFAULT);
	}

	/**
	 * Gets the persistence (int) value.
	 * 
	 * @return The persistence (<code>int</code>) value.
	 */
	private int getPersistence() {
		return getProperty(BrokerManagerConfiguration.PERSISTENCE_PROPERTY, BrokerManagerConfiguration.PERSISTENCE_DEFAULT);
	}

	/**
	 * Gets the queue size (int) value.
	 * 
	 * @return The queue size (<code>int</code>) value.
	 */
	private int getQueueSize() {
		return getProperty(BrokerManagerConfiguration.QUEUE_SIZE_PROPERTY, BrokerManagerConfiguration.QUEUE_SIZE_DEFAULT);
	}

	/**
	 * Gets the remote port (int) value.
	 * 
	 * @return The remote port (<code>int</code>) value.
	 */
	private int getRemotePort() {
		return getProperty(BrokerManagerConfiguration.PORT_PROPERTY, BrokerManagerConfiguration.PORT_DEFAULT);
	}

	/**
	 * Gets the security enabled (boolean) value.
	 * 
	 * @return The security enabled (<code>boolean</code>) value.
	 */
	private boolean getSecurityEnabled() {
		return getProperty(BrokerManagerConfiguration.SECURITY_ENABLED_PROPERTY,
				BrokerManagerConfiguration.SECURITY_ENABLED_DEFAULT);
	}

	/**
	 * Start broker.
	 * 
	 * @throws AdminException
	 *             Admin Exception.
	 * @see #getBroker()
	 * @see #startBroker(boolean)
	 * @see #stopBroker()
	 */
	public void startBroker() throws AdminException {
		BrokerDefinition brokerDefinition = this.createBrokerDefinition(this.getDir(), this.getName());
		startBroker(true, brokerDefinition);
	}

	/**
	 * Start broker with the specified b stop if started parameter.
	 * 
	 * @param bStopIfStarted
	 *            The b stop if started (<code>boolean</code>) parameter.
	 * @throws AdminException
	 *             Admin Exception.
	 * @see #getBroker()
	 * @see #startBroker()
	 * @see #stopBroker()
	 */
	public void startBroker(final boolean bStopIfStarted) throws AdminException {
		BrokerDefinition brokerDefinition = this.createBrokerDefinition(this.getDir(), this.getName());
		startBroker(bStopIfStarted, brokerDefinition);
	}

	/**
	 * ** Start the broker.
	 * 
	 * @param bStopIfStarted
	 *            The b stop if started (<code>boolean</code>) parameter.
	 * @param brokerDefinition
	 *            The broker definition (<code>BrokerDefinition</code>)
	 *            parameter.
	 * @throws AdminException
	 * @see #getBroker()
	 * @see #startBroker()
	 * @see #startBroker(boolean)
	 * @see #stopBroker()
	 */
	private void startBroker(final boolean bStopIfStarted, final BrokerDefinition brokerDefinition) throws AdminException {
		BrokerFactory factory = getFactory();
		try {
			boolean exists = factory.exists(this.getName());
			if (exists == true) {
				LocalBroker broker = factory.getByName(this.getName());
				if (broker != null) {
					boolean bRunning = broker.isRunning();
					if (bRunning) {
						if (bStopIfStarted) {
							/*
							 * Broker startup must be completed before stopping it again.
							 */
							this.waitForBrokerToStart(this.getName(), this.getBrokerWaitTimeout());
							broker.stop(false);
							this.waitForBrokerToStop(this.getName(), this.getBrokerWaitTimeout());
						} else {
							logger.error("Broker is already started: " + this.getDir() + File.separatorChar + this.getName());
							return;
						}
					}
				}
				boolean deleted = deleteBroker(this.getDir(), this.getName());
				if (deleted == false) {
					return; // Early return.
				}
				this.broker = null;
			}
			this.broker = createBroker(brokerDefinition);
			if (this.broker != null) {
				this.broker.start();
				this.waitForBrokerToStart(this.getName(), this.getBrokerWaitTimeout());
			}
		} catch (AdminException exception) {
			logger.error("Failed to start broker: " + this.getDir() + File.separatorChar + this.getName(), exception);
			throw exception;
		}
	}

	/**
	 * Stop broker.
	 * 
	 * @see #getBroker()
	 * @see #startBroker()
	 * @see #startBroker(boolean)
	 */
	public void stopBroker() {
		LocalBroker broker = (LocalBroker) getBroker();
		if (broker != null) {
			try {
				broker.getBridge().stopAllPipes();
			} catch (final AdminException exception) {
				logger.warn(exception.getMessage());
			}
			try {
				broker.stop(true);
			} catch (final AdminException exception) {
				logger.warn(exception.getMessage());
				try {
					BrokerDefinition brokerDefinition = broker.getDefinition();
					String directory = brokerDefinition.getDataDirectory();
					String name = brokerDefinition.getName();
					Object value = directory + File.separatorChar + name;
					logger.warn("Failed to stop broker: " + value);
				} catch (final AdminException adminException) {
					logger.warn(adminException.getMessage(), exception);
				}
			}
		}
	}

	/**
	 * Wait for broker to start with the specified str broker name and l timeout
	 * parameters.
	 * 
	 * @param strBrokerName
	 *            The str broker name (<code>String</code>) parameter.
	 * @param lTimeout
	 *            The l timeout (<code>long</code>) parameter.
	 */
	public void waitForBrokerToStart(final String strBrokerName, final long lTimeout) {
		long lStartTime = System.currentTimeMillis();
		try {
			while (this.broker.isRunning() == false) {
				long lElapsedTime = System.currentTimeMillis() - lStartTime;
				if (lElapsedTime > lTimeout) {
					break;
				}
				try {
					Thread.sleep(WAIT_LOOP_POLLING_DELAY);
				} catch (final InterruptedException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		} catch (final AdminException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Wait for broker to stop with the specified str broker name and l timeout
	 * parameters.
	 * 
	 * @param strBrokerName
	 *            The str broker name (<code>String</code>) parameter.
	 * @param lTimeout
	 *            The l timeout (<code>long</code>) parameter.
	 */
	public void waitForBrokerToStop(final String strBrokerName, final long lTimeout) {
		long lStartTime = System.currentTimeMillis();
		try {
			while (this.broker.isRunning() == true) {
				long lElapsedTime = System.currentTimeMillis() - lStartTime;
				if (lElapsedTime > lTimeout) {
					break;
				}
				try {
					Thread.sleep(WAIT_LOOP_POLLING_DELAY);
				} catch (final InterruptedException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		} catch (final AdminException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Main with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	public static void main(final String[] args) {
		try {
			String brokerConfigFile = null;
			for (int index = 0; index < args.length; ++index) {
				if (args[index].equalsIgnoreCase("-brokerConfig") && index + 1 < args.length) {
					brokerConfigFile = args[++index];
				} else {
					logger.error("Usage: BrokerManager [-brokerConfig <file>]");
					System.exit(-1);
				}
			}
			new BrokerManager().startBrokerManager(brokerConfigFile);
		} catch (Exception e) {
			System.exit(-1);
		}
	}

	/**
	 * Run with the specified arguments parameter.
	 * 
	 */
	/**
	 * Start broker manager.
	 * 
	 * @param brokerConfigFile TODO.
	 * @throws Exception 
	 */
	private void startBrokerManager(final String brokerConfigFile) throws Exception {
		Properties brokerProperties = loadProperties(brokerConfigFile);
		setConfiguration(new BaseConfiguration(brokerProperties));
		startBroker();
	}

	/**
	 * Load properties with the specified file name parameter.
	 * 
	 * @param fileName
	 *            The file name (<code>String</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	private Properties loadProperties(final String fileName) throws IOException {
		Properties brokerProperties = new Properties();
		if (fileName != null) {
			logger.info("Loading the broker properties");
			try {
				brokerProperties.load(new FileInputStream(fileName));
			} catch (final IOException e) {
				logger.error("Could not open properties file: " + fileName, e);
				throw e;
			}
		}
		return brokerProperties;
	}

}
