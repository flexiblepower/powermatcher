package net.powermatcher.telemetry.model.data;


/**
 * @author IBM
 * @version 0.9.0
 */
public class RequestResponseProperty {
	/**
	 * Define the name (String) field.
	 */
	private String name;
	/**
	 * Define the value (String) field.
	 */
	private String value;
	/**
	 * Define the logging (Boolean) field.
	 */
	private Boolean logging;

	/**
	 * Constructs an instance of this class from the specified name and value
	 * parameters.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @see #RequestResponseProperty(String,String,Boolean)
	 */
	public RequestResponseProperty(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructs an instance of this class from the specified name, value and
	 * logging parameters.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @param logging
	 *            The logging (<code>Boolean</code>) parameter.
	 * @see #RequestResponseProperty(String,String)
	 */
	public RequestResponseProperty(final String name, final String value, final Boolean logging) {
		this(name, value);
		this.logging = logging;
	}

	/**
	 * Gets the logging (Boolean) value.
	 * 
	 * @return The logging (<code>Boolean</code>) value.
	 */
	public Boolean getLogging() {
		return this.logging;
	}

	/**
	 * Gets the name (String) value.
	 * 
	 * @return The name (<code>String</code>) value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the value (String) value.
	 * 
	 * @return The value (<code>String</code>) value.
	 */
	public String getValue() {
		return this.value;
	}

}
