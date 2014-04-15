package net.powermatcher.core.config.parser;


import java.util.StringTokenizer;

/**
 * @author IBM
 * @version 0.9.0
 * @since 0.7
 */
public class FloatConverter extends AbstractConverter {
	/**
	 * Create array with the specified size parameter and return the Object[]
	 * result.
	 * 
	 * @param size
	 *            The size (<code>int</code>) parameter.
	 * @return Results of the create array (<code>Object[]</code>) value.
	 */
	@Override
	protected Object[] createArray(final int size) {
		return new Float[size];
	}

	/**
	 * Do conversion with the specified property parameter and return the Object
	 * result.
	 * 
	 * @param property
	 *            The property (<code>String</code>) parameter.
	 * @return Results of the do conversion (<code>Object</code>) value.
	 * @see #doPrimitiveArrayConversion(StringTokenizer)
	 */
	@Override
	protected Object doConversion(final String property) {
		return Float.valueOf(property);
	}

	/**
	 * Do primitive array conversion with the specified tokenizer parameter and
	 * return the Object result.
	 * 
	 * @param tokenizer
	 *            The tokenizer (<code>StringTokenizer</code>) parameter.
	 * @return Results of the do primitive array conversion (<code>Object</code>
	 *         ) value.
	 */
	@Override
	protected Object doPrimitiveArrayConversion(final StringTokenizer tokenizer) {
		final float[] result = new float[tokenizer.countTokens()];
		for (int i = 0; i < result.length; i++) {
			result[i] = Float.parseFloat(tokenizer.nextToken());
		}
		return result;
	}

}
