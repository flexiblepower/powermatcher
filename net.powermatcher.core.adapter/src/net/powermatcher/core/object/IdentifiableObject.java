package net.powermatcher.core.object;


import net.powermatcher.core.configurable.ConfigurableObject;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * <p>
 * The purpose of the IdentifiableObject class is that of a parent class that can be
 * used to build systems of configurable and identifiable objects with a uniform
 * logging facility. 
 * </p>
 * <p>
 * The IdentifiableObject is an abstract class that extends the ConfigurableObject
 * by providing a method for defining a configurable object's identity.
 * The identification is not unique within the system, only within the class of object,
 * like for example a PowerMatcher agent, or an adapter of a specific type.
 * </p>
 * <p>
 * The properties can be set using a configuration object that implements the
 * ConfigurationService interface. The property names in the configuration object
 * are defined by the constants in the IdentifiableObjectConfiguration class. 
 * </p>
 * <p> 
 * ConnectableObject provides convenience methods for logging that reference the SLF4J logging facade.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * s
 * @see Configurable
 * @see IdentifiableObjectConfiguration
 */
public abstract class IdentifiableObject extends ConfigurableObject {

	/**
	 * Define the logger (Logger) field.
	 */
	private Logger logger;

	/**
	 * Define the cluster ID (String) field.
	 */
	private String clusterId;

	/**
	 * Define the ID (String) field.
	 */
	private String id;

	/**
	 * Constructs an instance of this class.
	 */
	protected IdentifiableObject() {
		/* Set a temporary logger until setConfiguration */
		this.logger = LoggerFactory.getLogger(getClass());
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter. All property settings like id, cluster id, logger and
	 * connector id should be set through the configuration parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	protected IdentifiableObject(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return The Cluster ID (<code>String</code>) value.
	 */
	public String getClusterId() {
		return this.clusterId;
	}

	/**
	 * Gets the ID (String) value.
	 * 
	 * @return Results of the get ID (<code>String</code>) value.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the logger value.
	 * 
	 * @return The logger (<code>Logger</code>) value.
	 */
	public Logger getLogger() {
		return this.logger;
	}

	/**
	 * Gets the name (String) value.
	 * 
	 * @return Results of the get name (<code>String</code>) value.
	 */
	public String getName() {
		return getClass().getSimpleName();
	}

	/**
	 * Gets the debug enabled (boolean) value.
	 * 
	 * @return The debug enabled (<code>boolean</code>) value.
	 */
	public boolean isDebugEnabled() {
		return this.logger.isDebugEnabled();
	}

	/**
	 * Is enabled.
	 * 
	 * @return The is enabled (<code>boolean</code>) value.
	 */
	public boolean isEnabled() {
		return getProperty(IdentifiableObjectConfiguration.ENABLED_PROPERTY, IdentifiableObjectConfiguration.ENABLED_DEFAULT);
	}

	/**
	 * Gets the info enabled (boolean) value.
	 * 
	 * @return The info enabled (<code>boolean</code>) value.
	 */
	public boolean isInfoEnabled() {
		return this.logger.isInfoEnabled();
	}

	/**
	 * Log debug with the specified message parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 */
	public void logDebug(final String message) {
		this.logger.debug(message);
	}

	/**
	 * Log debug with the specified message and t parameters.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @param t
	 *            The t (<code>Throwable</code>) parameter.
	 */
	public void logDebug(final String message, final Throwable t) {
		this.logger.debug(message, t);
	}

	/**
	 * Log error with the specified message parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
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
	 */
	public void logError(final String message, final Throwable t) {
		this.logger.error(message, t);
	}

	/**
	 * Log info with the specified message parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
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
	 */
	public void logInfo(final String message, final Throwable t) {
		this.logger.info(message, t);
	}

	/**
	 * Log warning with the specified message parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 */
	public void logWarning(final String message) {
		this.logger.warn(message);
	}

	/**
	 * Log warning with the specified message and t parameters.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @param t
	 *            The t (<code>Throwable</code>) parameter.
	 */
	public void logWarning(final String message, final Throwable t) {
		this.logger.warn(message, t);
	}

	/**
	 * Sets the configuration value. The id, clusterId, connectorId and
	 * logger property values are retrieved from this configuration value.
	 * The property names are defined by the constants in class IdentifiableObjectConfiguration.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 *            
	 * @see IdentifiableObjectConfiguration
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		this.clusterId = getProperty(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, IdentifiableObjectConfiguration.CLUSTER_ID_DEFAULT);
		this.id = getStringProperty(IdentifiableObjectConfiguration.ID_PROPERTY);
		this.logger = LoggerFactory.getLogger(getClass().getName() + '-' + getClusterId() + '.' + getId());
	}

}
