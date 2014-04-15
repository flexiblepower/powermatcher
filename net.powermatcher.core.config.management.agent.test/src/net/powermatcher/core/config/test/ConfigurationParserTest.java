package net.powermatcher.core.config.test;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.powermatcher.core.config.ConfigurationSpec;
import net.powermatcher.core.config.parser.SystemConfigurationContentHandler;

import org.junit.Before;
import org.junit.Test;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigurationParserTest {

	private SAXParserFactory saxParserFactory;
	private Reader in;

	/**
	 * @param in
	 * @return TODO
	 * @throws Exception
	 */
	public Set<ConfigurationSpec> parse(final Reader in) throws Exception {
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
		return SystemConfigurationContentHandler.parse(saxParser, in);
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			this.in = new FileReader("resources/simple_configuration.xml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("Could not create file reader", e);
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testParseSystemConfiguration() throws Exception {

		try {
			Set<ConfigurationSpec> configSet = this.parse(this.in);
			System.out.println(configSet);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Exception("Error parsing configuration.", e);
		}

	}
}
