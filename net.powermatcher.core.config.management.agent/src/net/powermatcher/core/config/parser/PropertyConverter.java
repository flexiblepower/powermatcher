package net.powermatcher.core.config.parser;


/**
 * @author IBM
 * @version 0.9.0
 * @since 0.7
 */
public interface PropertyConverter {
	/**
	 * Convert with the specified property parameter and return the Object
	 * result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @return Results of the convert (<code>Object</code>) value.
	 * @see #convert(String,int)
	 */
	public Object convert(final String property);

	/**
	 * Convert with the specified property and cardinality parameters and return
	 * the Object result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @param cardinality
	 *            The cardinality (<code>int</code>) parameter.
	 * @return Results of the convert (<code>Object</code>) value.
	 * @see #convert(String)
	 */
	public Object convert(final String property, final int cardinality);

}
