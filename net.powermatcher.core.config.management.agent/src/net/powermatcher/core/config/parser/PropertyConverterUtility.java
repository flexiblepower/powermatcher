package net.powermatcher.core.config.parser;


/**
 * @author IBM
 * @version 0.9.0
 * @since 0.7
 */
public class PropertyConverterUtility {
	/**
	 * Convert with the specified property and type parameters and return the
	 * Object result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @param type
	 *            The type (<code>String</code>) parameter.
	 * @return Results of the convert (<code>Object</code>) value.
	 * @see #convert(String,String,int)
	 */
	public static Object convert(final String property, final String type) {
		return convert(property, type, 0);
	}

	/**
	 * Convert with the specified property, type and cardinality parameters and
	 * return the Object result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @param type
	 *            The type (<code>String</code>) parameter.
	 * @param cardinality
	 *            The cardinality (<code>int</code>) parameter.
	 * @return Results of the convert (<code>Object</code>) value.
	 * @see #convert(String,String)
	 */
	public static Object convert(final String property, final String type, final int cardinality) {
		final PropertyConverter converter = PropertyConverterFactory.create(type);
		return converter.convert(property, cardinality);
	}

}
