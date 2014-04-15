package net.powermatcher.core.config.parser;


/**
 * @author IBM
 * @version 0.9.0
 */
public class SystemConfigurationParserException extends Exception {
	/**
	 * Define the serial version UID (long) constant.
	 */
	private static final long serialVersionUID = -2210547345609551697L;

	/**
	 * Constructs an instance of this class from the specified message
	 * parameter.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @see #SystemConfigurationParserException(String,Throwable)
	 */
	public SystemConfigurationParserException(final String message) {
		super(message);
	}

	/**
	 * Constructs an instance of this class from the specified message and cause
	 * parameters.
	 * 
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @param cause
	 *            The cause (<code>Throwable</code>) parameter.
	 * @see #SystemConfigurationParserException(String)
	 */
	public SystemConfigurationParserException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
