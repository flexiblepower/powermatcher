package net.powermatcher.core.configurable.service;

import java.util.Map;


/**
 * @author IBM
 * @version 0.9.0
 * 
 * <p>
 * Defines the interface for an object that provides services for
 * retrieving configuration properties. 
 * </p>
 * <p>
 * The service allows the implementation of an hierarchy of configuration
 * objects.
 * </p>
 * @see Configurable#getParent()
 */
public interface Configurable {
	/**
	 * Define the separator (char) constant.
	 */
	public static final char SEPARATOR = '.';

	/**
	 * Get boolean property with the specified name parameter and return the
	 * boolean result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get boolean property (<code>boolean</code>) value.
	 */
	public boolean getBooleanProperty(final String name);

	/**
	 * Get byte property with the specified name parameter and return the byte
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get byte property (<code>byte</code>) value.
	 */
	public byte getByteProperty(final String name);

	/**
	 * Get double property with the specified name parameter and return the
	 * double result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get double property (<code>double</code>) value.
	 */
	public double getDoubleProperty(final String name);

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
	public String[] getEnumeratedStringProperties(final String name);

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
	public String[] getEnumeratedStringProperties(final String name, final String delim);

	/**
	 * Get float property with the specified name parameter and return the float
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get float property (<code>float</code>) value.
	 */
	public float getFloatProperty(final String name);

	/**
	 * Get integer property with the specified name parameter and return the int
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get integer property (<code>int</code>) value.
	 */
	public int getIntegerProperty(final String name);

	/**
	 * Get long property with the specified name parameter and return the long
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get long property (<code>long</code>) value.
	 */
	public long getLongProperty(final String name);

	/**
	 * Get optional property with the specified name parameter and return the
	 * Object result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get optional property (<code>Object</code>) value.
	 */
	public Object getOptionalProperty(final String name);

	/**
	 * Gets the parent (ConfigurationService) value.
	 * 
	 * @return The parent (<code>ConfigurationService</code>) value.
	 */
	public Configurable getParent();

	/**
	 * Get a map with all configuration properties.
	 * This does not include the configuration properties of the parent configuration.
	 * @return A map with all configuration properties.
	 */
	public Map<String, Object> getProperties();

	/**
	 * Get property with the specified name parameter and return the Object
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get property (<code>Object</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public Object getProperty(final String name);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the boolean result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>boolean</code>) parameter.
	 * @return Results of the get property (<code>boolean</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public boolean getProperty(final String name, final boolean defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the byte result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>byte</code>) parameter.
	 * @return Results of the get property (<code>byte</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public byte getProperty(final String name, final byte defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the double result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>double</code>) parameter.
	 * @return Results of the get property (<code>double</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public double getProperty(final String name, final double defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the float result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>float</code>) parameter.
	 * @return Results of the get property (<code>float</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public float getProperty(final String name, final float defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the int result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>int</code>) parameter.
	 * @return Results of the get property (<code>int</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public int getProperty(final String name, final int defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the long result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>long</code>) parameter.
	 * @return Results of the get property (<code>long</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public long getProperty(final String name, final long defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the short result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>short</code>) parameter.
	 * @return Results of the get property (<code>short</code>) value.
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public short getProperty(final String name, final short defaultValue);

	/**
	 * Get property with the specified name and default value parameters and
	 * return the String result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param defaultValue
	 *            The default value (<code>String</code>) parameter.
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
	 * @see #getProperty(String)
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	public String getProperty(final String name, final String defaultValue);

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
	public String[] getProperty(final String name, final String delim, final String[] defaultValue);

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
	public String[] getProperty(final String name, final String[] defaultValue);

	/**
	 * Get short property with the specified name parameter and return the short
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get short property (<code>short</code>) value.
	 */
	public short getShortProperty(final String name);

	/**
	 * Get string property with the specified name parameter and return the
	 * String result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get string property (<code>String</code>) value.
	 */
	public String getStringProperty(final String name);

}
