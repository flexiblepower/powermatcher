package net.powermatcher.core.configurable;


import net.powermatcher.core.configurable.service.ConfigurableService;
import net.powermatcher.core.configurable.service.Configurable;

/**
 * @author IBM
 * @version 0.9.0
 * 
 * <p>
 * An object of class ConfigurableObject can store configuration data.
 * </p>
 * <p>
 * This class provides an implementation of the ConfigurableService interface and
 * and is a wrapper class for an object that implements the ConfigurationService
 * interface. If offers convenience methods for retrieving configuration data.
 * </p>
 * <p>
 * The purpose of the class is to serve as a parent class for objects that need
 * to store a configuration and retrieve easily configuration properties.
 * </p>
 * @see ConfigurableService
 * @see Configurable
 */
public class ConfigurableObject implements ConfigurableService {
	/**
	 * Define the configuration (ConfigurationService) field.
	 */
	private Configurable configuration;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #ConfigurableObject(Configurable)
	 */
	public ConfigurableObject() {
		super();
	}

	/**
	 * Constructs a ConfigurableObject using the the configuration
	 * (<code>ConfigurationService</code>) from the specified parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #ConfigurableObject()
	 */
	public ConfigurableObject(final Configurable configuration) {
		setConfiguration(configuration);
	}

	/**
	 * Get boolean property with the specified name parameter and return the
	 * boolean result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get boolean property (<code>boolean</code>) value.
	 */
	protected boolean getBooleanProperty(final String name) {
		return this.configuration.getBooleanProperty(name);
	}

	/**
	 * Get byte property with the specified name parameter and return the byte
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get byte property (<code>byte</code>) value.
	 */
	protected byte getByteProperty(final String name) {
		return this.configuration.getByteProperty(name);
	}

	/**
	 * Gets the configuration (ConfigurationService) value.
	 * 
	 * @return The configuration (<code>ConfigurationService</code>) value.
	 * @see #setConfiguration(Configurable)
	 */
	@Override
	public Configurable getConfiguration() {
		return this.configuration;
	}

	/**
	 * Get double property with the specified name parameter and return the
	 * double result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get double property (<code>double</code>) value.
	 */
	protected double getDoubleProperty(final String name) {
		return this.configuration.getDoubleProperty(name);
	}

	/**
	 * Get enumerated string properties with the specified name parameter and
	 * return the String[] result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get enumerated string properties (
	 *         <code>String[]</code>) value.
	 * @see #getEnumeratedStringProperties(String,String)
	 */
	protected String[] getEnumeratedStringProperties(final String name) {
		return this.configuration.getEnumeratedStringProperties(name);
	}

	/**
	 * Get enumerated string properties with the specified name and token
	 * parameters and return the String[] result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param delim
	 *            The delim (<code>String</code>) parameter.
	 * @return Results of the get enumerated string properties (
	 *         <code>String[]</code>) value.
	 * @see #getEnumeratedStringProperties(String)
	 */
	protected String[] getEnumeratedStringProperties(final String name, final String delim) {
		return this.configuration.getEnumeratedStringProperties(name, delim);
	}

	/**
	 * Get float property with the specified name parameter and return the float
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get float property (<code>float</code>) value.
	 */
	protected float getFloatProperty(final String name) {
		return this.configuration.getFloatProperty(name);
	}

	/**
	 * Get integer property with the specified name parameter and return the int
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get integer property (<code>int</code>) value.
	 */
	protected int getIntegerProperty(final String name) {
		return this.configuration.getIntegerProperty(name);
	}

	/**
	 * Get long property with the specified name parameter and return the long
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get long property (<code>long</code>) value.
	 */
	protected long getLongProperty(final String name) {
		return this.configuration.getLongProperty(name);
	}

	/**
	 * Get optional property with the specified name parameter and return the
	 * Object result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get optional property (<code>Object</code>) value.
	 */
	protected Object getOptionalProperty(final String name) {
		return this.configuration.getOptionalProperty(name);
	}

	/**
	 * Get property with the specified name parameter and return the String
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get property (<code>String</code>) value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected Object getProperty(final String name) {
		return this.configuration.getProperty(name);
	}

	/**
	 * Get optional boolean property with the specified name and default value
	 * parameters and return the boolean result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>boolean</code>) parameter.
	 * @return Results of the get optional boolean property (
	 *         <code>boolean</code>) value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected boolean getProperty(final String name, final boolean defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional byte property with the specified name and default value
	 * parameters and return the byte result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>byte</code>) parameter.
	 * @return Results of the get optional byte property (<code>byte</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected byte getProperty(final String name, final byte defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional double property with the specified name and default value
	 * parameters and return the double result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>double</code>) parameter.
	 * @return Results of the get optional double property (<code>double</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected double getProperty(final String name, final double defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional float property with the specified name and default value
	 * parameters and return the float result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>float</code>) parameter.
	 * @return Results of the get optional float property (<code>float</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected float getProperty(final String name, final float defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional integer property with the specified name and default value
	 * parameters and return the int result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>int</code>) parameter.
	 * @return Results of the get optional integer property (<code>int</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected int getProperty(final String name, final int defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional long property with the specified name and default value
	 * parameters and return the long result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>long</code>) parameter.
	 * @return Results of the get optional long property (<code>long</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected long getProperty(final String name, final long defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional short property with the specified name and default value
	 * parameters and return the short result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>short</code>) parameter.
	 * @return Results of the get optional short property (<code>short</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected short getProperty(final String name, final short defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get optional string property with the specified name and default value
	 * parameters and return the String result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>String</code>) parameter.
	 * @return Results of the get optional string property (<code>String</code>)
	 *         value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	protected String getProperty(final String name, final String defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get property with the specified name, token and default value parameters
	 * and return the String[] result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param delim
	 *            The delim (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>String[]</code>) parameter.
	 * @return Results of the get property (<code>String[]</code>) value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public String[] getProperty(final String name, final String delim, final String[] defaultValue) {
		return this.configuration.getProperty(name, delim, defaultValue);
	}

	/**
	 * Get property with the specified name and default value parameters and
	 * return the String[] result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>String[]</code>) parameter.
	 * @return Results of the get property (<code>String[]</code>) value.
	 * @see #getBooleanProperty(String)
	 * @see #getByteProperty(String)
	 * @see #getDoubleProperty(String)
	 * @see #getFloatProperty(String)
	 * @see #getIntegerProperty(String)
	 * @see #getLongProperty(String)
	 * @see #getOptionalProperty(String)
	 * @see #getProperty(String,boolean)
	 * @see #getProperty(String,byte)
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getProperty(String,String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public String[] getProperty(final String name, final String[] defaultValue) {
		return this.configuration.getProperty(name, defaultValue);
	}

	/**
	 * Get short property with the specified name parameter and return the short
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get short property (<code>short</code>) value.
	 */
	protected short getShortProperty(final String name) {
		return this.configuration.getShortProperty(name);
	}

	/**
	 * Get string property with the specified name parameter and return the
	 * String result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get string property (<code>String</code>) value.
	 */
	protected String getStringProperty(final String name) {
		return this.configuration.getStringProperty(name);
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #getConfiguration()
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		this.configuration = configuration;
	}

}
