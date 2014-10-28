package net.powermatcher.core.configurable;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * @author IBM
 * @version 0.9.0
 * 
 * <p>
 * A BaseConfiguration instance implements the ConfigurationService interface.
 * </p>
 * <p>
 * Configuration data of the type Properties or Map<String, Object> is assigned
 * at creation time.
 * </p>
 * <p>
 * If the instance has a parent configuration the property value in the child
 * class will override the property with the same name in the parent.
 * </p>
 * <p>
 * Property values can also contain properties that refer to other properties
 * in the configuration object. For example, suppose there are three properties;
 * propA, propB and propC all of type String where the latter refers to the others:
 * <br>
 * <br> propA = "Hello"
 * <br> propB = "World"
 * <br> propC = "${propA} ${propB}"
 * <br>
 * <br>
 * Then the method getStringProperty("propC") will return "Hello World". The "${property}"
 * is regarded as a reference to another and will be replaced by the value of the property.
 * If the property does not exist the value remains as defined.
 *
 * 
 * </p>
 */
public class BaseConfiguration implements ConfigurationService {
	/**
	 * Define the parent (ConfigurationService) field.
	 */
	private ConfigurationService parent;
	/**
	 * Define the properties (Map) field.
	 */
	private Map<String, Object> properties;

	/**
	 * Constructs an instance of this class from the specified parent and
	 * properties parameters.
	 * 
	 * @param parent
	 *            The parent (<code>ConfigurationService</code>) parameter.
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 * @see #BaseConfiguration(Map)
	 */
	public BaseConfiguration(final ConfigurationService parent, final Map<String, Object> properties) {
		this.properties = properties;
		this.parent = parent;
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Map<String,Object></code>) parameter.
	 * @see #BaseConfiguration(ConfigurationService,Map)
	 */
	public BaseConfiguration(final Map<String, Object> properties) {
		this(null, properties);
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param properties
	 *            The properties (<code>Properties</code>) parameter.
	 * @see #BaseConfiguration(ConfigurationService,Map)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public BaseConfiguration(final Properties properties) {
		this(null, (Map) properties);
	}

	/**
	 * Get boolean property with the specified name parameter and return the
	 * boolean result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get boolean property (<code>boolean</code>) value.
	 */
	@Override
	public boolean getBooleanProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		if (value instanceof String) {
			return Boolean.valueOf(substituteVariables((String) value)).booleanValue();
		}
		throw new IllegalArgumentException("Property must be of type String or Boolean: " + name);
	}

	/**
	 * Get byte property with the specified name parameter and return the byte
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get byte property (<code>byte</code>) value.
	 */
	@Override
	public byte getByteProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Byte) {
			return ((Byte) value).byteValue();
		}
		if (value instanceof String) {
			try {
				return Byte.parseByte(substituteVariables((String) value));
			} catch (final NumberFormatException exception) {
				throw new IllegalArgumentException("Property must be parsable as a byte: " + name);
			}
		}
		throw new IllegalArgumentException("Property must be of type Byte: " + name);
	}

	/**
	 * Get double property with the specified name parameter and return the
	 * double result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get double property (<code>double</code>) value.
	 */
	@Override
	public double getDoubleProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Double) {
			return ((Double) value).doubleValue();
		}
		if (value instanceof String) {
			return Double.parseDouble(substituteVariables((String) value));
		}
		throw new IllegalArgumentException("Property must be of type String or Double: " + name);
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
	@Override
	public String[] getEnumeratedStringProperties(final String name) {
		return getEnumeratedStringProperties(name, ",");
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
	@Override
	public String[] getEnumeratedStringProperties(final String name, final String delim) {
		String value = getStringProperty(name);
		StringTokenizer tokenizer = new StringTokenizer(value, delim);
		String[] result = new String[tokenizer.countTokens()];
		for (int i = 0; tokenizer.hasMoreTokens(); i++) {
			result[i] = tokenizer.nextToken();
		}
		return result;
	}

	/**
	 * Get float property with the specified name parameter and return the float
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get float property (<code>float</code>) value.
	 */
	@Override
	public float getFloatProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Float) {
			return ((Float) value).floatValue();
		}
		if (value instanceof String) {
			return Float.parseFloat(substituteVariables((String) value));
		}
		throw new IllegalArgumentException("Property must be of type String or Float: " + name);
	}

	/**
	 * Get integer property with the specified name parameter and return the int
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get integer property (<code>int</code>) value.
	 */
	@Override
	public int getIntegerProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Integer) {
			return ((Integer) value).intValue();
		}
		if (value instanceof String) {
			try {
				return Integer.parseInt(substituteVariables((String) value));
			} catch (final NumberFormatException exception) {
				throw new IllegalArgumentException("Property must be parsable as an int: " + name);
			}
		}
		throw new IllegalArgumentException("Property must be of type String or Integer: " + name);
	}

	/**
	 * Get long property with the specified name parameter and return the long
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get long property (<code>long</code>) value.
	 */
	@Override
	public long getLongProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Long) {
			return ((Long) value).longValue();
		}
		if (value instanceof String) {
			try {
				return Long.parseLong(substituteVariables((String) value));
			} catch (final NumberFormatException exception) {
				throw new IllegalArgumentException("Property must be parsable as a long: " + name);
			}
		}
		throw new IllegalArgumentException("Property must be of type String or Long: " + name);
	}

	/**
	 * Get optional property with the specified name parameter and return the
	 * Object result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get optional property (<code>Object</code>) value.
	 */
	@Override
	public Object getOptionalProperty(final String name) {
		try {
			return getProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return null;
	}

	/**
	 * Gets the parent (ConfigurationService) value.
	 * 
	 * @return The parent (<code>ConfigurationService</code>) value.
	 */
	@Override
	public ConfigurationService getParent() {
		return this.parent;
	}

	/**
	 * Get a map with all configuration properties.
	 * This does include the configuration properties of the parent configuration, if defined.
	 * @return A map with all configuration properties, possibly inherited from the parent configuration.
	 */
	@Override
	public Map<String, Object> getProperties() {
		if (this.parent == null) {
			return this.properties;
		}
		Map<String, Object> properties = new HashMap<String, Object>(this.parent.getProperties());
		properties.putAll(this.properties);
		return properties;
	}

	/**
	 * Get property with the specified name parameter and return the String
	 * result. If the property is not found there will be a lookup in the properties
	 * of the parent.  
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public Object getProperty(final String name) {
		Object value = this.properties.get(name);
		if (value == null) {
			if (this.parent == null) {
				throw new IllegalArgumentException("Missing property: " + name);
			}
			value = this.parent.getProperty(name);
		}
		return value;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public boolean getProperty(final String name, final boolean defaultValue) {
		try {
			return getBooleanProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public byte getProperty(final String name, final byte defaultValue) {
		try {
			return getByteProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public double getProperty(final String name, final double defaultValue) {
		try {
			return getDoubleProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public float getProperty(final String name, final float defaultValue) {
		try {
			return getFloatProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public int getProperty(final String name, final int defaultValue) {
		try {
			return getIntegerProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public long getProperty(final String name, final long defaultValue) {
		try {
			return getLongProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public short getProperty(final String name, final short defaultValue) {
		try {
			return getShortProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return defaultValue;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public String getProperty(final String name, final String defaultValue) {
		try {
			return getStringProperty(name);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		return substituteVariables(defaultValue);
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
	 * @see #getProperty(String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public String[] getProperty(final String name, final String delim, final String[] defaultValue) {
		try {
			return getEnumeratedStringProperties(name, delim);
		} catch (final IllegalArgumentException exception) {
			/* ignore exception */
		}
		if (defaultValue == null) {
			return null;
		}
		String result[] = new String[defaultValue.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = substituteVariables(defaultValue[i]);
		}
		return result;
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
	 * @see #getProperty(String,String,String[])
	 * @see #getProperty(String,double)
	 * @see #getProperty(String,float)
	 * @see #getProperty(String,int)
	 * @see #getProperty(String,long)
	 * @see #getProperty(String,short)
	 * @see #getProperty(String,String)
	 * @see #getProperty(String)
	 * @see #getShortProperty(String)
	 * @see #getStringProperty(String)
	 */
	@Override
	public String[] getProperty(final String name, final String[] defaultValue) {
		return getProperty(name, ",", defaultValue);
	}

	/**
	 * Get short property with the specified name parameter and return the short
	 * result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get short property (<code>short</code>) value.
	 */
	@Override
	public short getShortProperty(final String name) {
		Object value = getProperty(name);
		if (value instanceof Short) {
			return ((Short) value).shortValue();
		}
		if (value instanceof String) {
			try {
				return Short.parseShort((String) value);
			} catch (final NumberFormatException exception) {
				throw new IllegalArgumentException("Property must be parsable as a short: " + name);
			}
		}
		throw new IllegalArgumentException("Property must be of type String or Short: " + name);
	}

	/**
	 * Get string property with the specified name parameter and return the
	 * String result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get string property (<code>String</code>) value.
	 */
	@Override
	public String getStringProperty(final String name) {
		Object obj = getProperty(name);
		if (!(obj instanceof String) || "".equals(obj)) {
			throw new IllegalArgumentException("Property must be of type String: " + name);
		}
		return substituteVariables((String) obj);
	}

	/**
	 * Substitute ${variable} occurrences in the string value.
	 * Undefined variables are not substituted.
	 * @param value The string value containing zero or more variables.
	 * @return The string with substitutions.
	 */
	protected String substituteVariables(final String value) {
		String result = value;
		if (value != null) {
			int start = 0;
			while ( (start = result.indexOf("${", start)) != -1) {
				int end = result.indexOf('}', start);
				if (end != -1) {
					String varName = result.substring(start + 2 , end);
					Object obj = getProperty(varName);
					if (obj != null) {
						String subtitute = String.valueOf(obj);
						result = result.substring(0, start) + subtitute + result.substring(end + 1);
						start = start + subtitute.length();
					} else {
						start = end + 1;
					}
				} else {
					start = start + 2;
				}
			}
		}
		return result;
	}

}
