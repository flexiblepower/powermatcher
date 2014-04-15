package net.powermatcher.core.config.parser;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.powermatcher.core.config.ConfigurationSpec;


/**
 * @author IBM
 * @version 0.9.0
 */
public class SystemConfigurationParser {
	/**
	 * Define the sax parser factory (SAXParserFactory) field.
	 */
	private SAXParserFactory saxParserFactory;

	/**
	 * Constructs an instance of this class from the specified factory
	 * parameter.
	 * 
	 * @param factory
	 *            The factory (<code>SAXParserFactory</code>) parameter.
	 */
	public SystemConfigurationParser(final SAXParserFactory factory) {
		super();
		this.saxParserFactory = factory;
	}

	/**
	 * Gets the parser (SAXParser) value.
	 * 
	 * @return The parser (<code>SAXParser</code>) value.
	 * @throws Exception
	 *             Exception.
	 * @see #SystemConfigurationParser(SAXParserFactory)
	 */
	private SAXParser getParser() throws Exception {
		SAXParser saxParser;
		if (this.saxParserFactory == null) {
			final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			saxParserFactory.setNamespaceAware(false);
			try {
				saxParserFactory.setValidating(false);
				saxParser = saxParserFactory.newSAXParser();
			} catch (final ParserConfigurationException e) {
				saxParserFactory.setValidating(false);
				saxParser = saxParserFactory.newSAXParser();
			}
		} else {
			saxParser = this.saxParserFactory.newSAXParser();
		}
		return saxParser;
	}

	/**
	 * Parse with the specified input parameter and return the
	 * Set<ConfigurationSpec> result.
	 * 
	 * @param input
	 *            The input (<code>InputStream</code>) parameter.
	 * @return TODO
	 * @throws SystemConfigurationParserException
	 * @see #parse(String)
	 */
	public Set<ConfigurationSpec> parse(final InputStream input) throws SystemConfigurationParserException {
		SAXParser saxParser = null;
		Set<ConfigurationSpec> configurations = null;
		try {
			saxParser = getParser();
			configurations = SystemConfigurationContentHandler.parse(saxParser, input);
		} catch (Exception e) {
			throw new SystemConfigurationParserException("Exception occurred while parsing the system configuration resource.",
					e);
		}
		return configurations;
	}

	/**
	 * Parse with the specified filename parameter and return the
	 * Set<ConfigurationSpec> result.
	 * 
	 * @param filename
	 *            The filename (<code>String</code>) parameter.
	 * @return TODO
	 * @throws SystemConfigurationParserException
	 * @see #parse(InputStream)
	 */
	public Set<ConfigurationSpec> parse(final String filename) throws SystemConfigurationParserException {
		SAXParser saxParser = null;
		Reader in = null;
		Set<ConfigurationSpec> configurations = null;
		try {
			saxParser = getParser();
			in = new FileReader(filename);
			configurations = SystemConfigurationContentHandler.parse(saxParser, in);
		} catch (final FileNotFoundException e) {
			throw new SystemConfigurationParserException("Configuration resource not found.", e);
		} catch (final Exception e) {
			throw new SystemConfigurationParserException("Exception occurred while parsing the system configuration resource.",
					e);
		}
		return configurations;
	}

}
