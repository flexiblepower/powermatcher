package net.powermatcher.core.config.parser;


/**
 * @author IBM
 * @version 0.9.0
 * @since 0.7
 */
public class PropertyConverterFactory {
	/**
	 * Define the boolean type (String) constant.
	 */
	public static final String BOOLEAN_TYPE = "boolean"; //$NON-NLS-1$
	/**
	 * Define the byte type (String) constant.
	 */
	public static final String BYTE_TYPE = "byte"; //$NON-NLS-1$
	/**
	 * Define the character type (String) constant.
	 */
	public static final String CHARACTER_TYPE = "character"; //$NON-NLS-1$
	/**
	 * Define the double type (String) constant.
	 */
	public static final String DOUBLE_TYPE = "double"; //$NON-NLS-1$
	/**
	 * Define the float type (String) constant.
	 */
	public static final String FLOAT_TYPE = "float"; //$NON-NLS-1$
	/**
	 * Define the integer type (String) constant.
	 */
	public static final String INTEGER_TYPE = "integer"; //$NON-NLS-1$
	/**
	 * Define the long type (String) constant.
	 */
	public static final String LONG_TYPE = "long"; //$NON-NLS-1$
	/**
	 * Define the short type (String) constant.
	 */
	public static final String SHORT_TYPE = "short"; //$NON-NLS-1$
	/**
	 * Define the string type (String) constant.
	 */
	public static final String STRING_TYPE = "string"; //$NON-NLS-1$
	/**
	 * Define the boolean converter (BooleanConverter) constant.
	 */
	private static final BooleanConverter BOOLEAN_CONVERTER = new BooleanConverter();
	/**
	 * Define the byte converter (ByteConverter) constant.
	 */
	private static final ByteConverter BYTE_CONVERTER = new ByteConverter();
	/**
	 * Define the character converter (CharacterConverter) constant.
	 */
	private static final CharacterConverter CHARACTER_CONVERTER = new CharacterConverter();
	/**
	 * Define the double converter (DoubleConverter) constant.
	 */
	private static final DoubleConverter DOUBLE_CONVERTER = new DoubleConverter();
	/**
	 * Define the float converter (FloatConverter) constant.
	 */
	private static final FloatConverter FLOAT_CONVERTER = new FloatConverter();
	/**
	 * Define the integer converter (IntegerConverter) constant.
	 */
	private static final IntegerConverter INTEGER_CONVERTER = new IntegerConverter();
	/**
	 * Define the long converter (LongConverter) constant.
	 */
	private static final LongConverter LONG_CONVERTER = new LongConverter();
	/**
	 * Define the short converter (ShortConverter) constant.
	 */
	private static final ShortConverter SHORT_CONVERTER = new ShortConverter();
	/**
	 * Define the string converter (StringConverter) constant.
	 */
	private static final StringConverter STRING_CONVERTER = new StringConverter();

	/**
	 * Create with the specified type parameter and return the PropertyConverter
	 * result.
	 * 
	 * @param type
	 *            The type (<code>String</code>) parameter.
	 * @return Results of the create (<code>PropertyConverter</code>) value.
	 */
	public static PropertyConverter create(final String type) {
		if ((type == null) || (type.length() == 0) || type.toLowerCase().equals(PropertyConverterFactory.STRING_TYPE)) {
			return PropertyConverterFactory.STRING_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.BOOLEAN_TYPE)) {
			return PropertyConverterFactory.BOOLEAN_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.BYTE_TYPE)) {
			return PropertyConverterFactory.BYTE_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.CHARACTER_TYPE)) {
			return PropertyConverterFactory.CHARACTER_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.DOUBLE_TYPE)) {
			return PropertyConverterFactory.DOUBLE_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.FLOAT_TYPE)) {
			return PropertyConverterFactory.FLOAT_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.INTEGER_TYPE)) {
			return PropertyConverterFactory.INTEGER_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.LONG_TYPE)) {
			return PropertyConverterFactory.LONG_CONVERTER;
		} else if (type.toLowerCase().equals(PropertyConverterFactory.SHORT_TYPE)) {
			return PropertyConverterFactory.SHORT_CONVERTER;
		} else {
			return PropertyConverterFactory.STRING_CONVERTER;
		}
	}

}
