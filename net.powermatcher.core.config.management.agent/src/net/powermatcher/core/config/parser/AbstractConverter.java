package net.powermatcher.core.config.parser;


import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author IBM
 * @version 0.9.0
 * @since 0.7
 */
public abstract class AbstractConverter implements PropertyConverter {
	/**
	 * Convert with the specified property parameter and return the Object
	 * result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @return Results of the convert (<code>Object</code>) value.
	 * @see #convert(String,int)
	 */
	@Override
	public Object convert(final String property) {
		return convert(property, 0);
	}

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
	@Override
	public Object convert(final String property, final int cardinality) {
		if (cardinality == 0) {
			return doConversion(property);
		}
		if (cardinality < 0) {
			final StringTokenizer tokenizer = new StringTokenizer(property, ","); //$NON-NLS-1$
			final List<Object> list = new Vector<Object>(tokenizer.countTokens());
			while (tokenizer.hasMoreTokens()) {
				list.add(doConversion(tokenizer.nextToken()));
			}
			return list;
		}
		final StringTokenizer tokenizer = new StringTokenizer(property, ","); //$NON-NLS-1$
		return doPrimitiveArrayConversion(tokenizer);
	}

	/**
	 * Create array with the specified size parameter and return the Object[]
	 * result.
	 * 
	 * @param size
	 *            The size (<code>int</code>) parameter.
	 * @return Results of the create array (<code>Object[]</code>) value.
	 */
	protected abstract Object[] createArray(final int size);

	/**
	 * Do conversion with the specified property parameter and return the Object
	 * result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @return Results of the do conversion (<code>Object</code>) value.
	 * @see #doPrimitiveArrayConversion(StringTokenizer)
	 */
	protected abstract Object doConversion(final String property);

	/**
	 * Do primitive array conversion with the specified tokenizer parameter and
	 * return the Object result.
	 * 
	 * @param tokenizer
	 *            The tokenizer (<code>StringTokenizer</code>) parameter.
	 * @return Results of the do primitive array conversion (<code>Object</code>
	 *         ) value.
	 */
	protected abstract Object doPrimitiveArrayConversion(final StringTokenizer tokenizer);

}
