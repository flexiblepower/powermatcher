package net.powermatcher.core.configurable.service;



/**
 * @author IBM
 * @version 0.9.0
 * 
 * <p>
 * Defines the interface for objects for which a configuration
 * can be set.
 * </p>
 */
public interface ConfigurableService {
	/**
	 * Gets the configuration (ConfigurationService) value.
	 * 
	 * @return The configuration (<code>ConfigurationService</code>) value.
	 * @see #setConfiguration(Configurable)
	 */
	public Configurable getConfiguration();

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #getConfiguration()
	 */
	public void setConfiguration(final Configurable configuration);

}
