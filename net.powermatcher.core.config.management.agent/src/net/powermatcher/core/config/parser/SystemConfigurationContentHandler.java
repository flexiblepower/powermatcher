package net.powermatcher.core.config.parser;


import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;

import net.powermatcher.core.config.ConfigurationSpec;
import net.powermatcher.core.config.ConfigurationSpecXmlService;
import net.powermatcher.core.config.ConfigurationSpec.ConfigurationType;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author IBM
 * @version 0.9.0
 * @since 0.7
 */
public class SystemConfigurationContentHandler extends DefaultHandler {
	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	class ConfigurationState extends SystemConfigurationContentHandler.State {
		/**
		 * Define the parent (ConfigurationSpec) field.
		 */
		private ConfigurationSpec parent;

		/**
		 * Constructs an instance of this class from the specified parent state
		 * and properties parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param parent
		 *            The parent (<code>ConfigurationSpec</code>) parameter.
		 */
		public ConfigurationState(final SystemConfigurationContentHandler.State parentState, final ConfigurationSpec parent) {
			super(parentState);
			this.parent = parent;
		}

		/**
		 * End element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #startElement(String,Attributes)
		 */
		@Override
		void endElement(final String localName) throws SAXParseException {
			if (ConfigurationSpecXmlService.ELMNT_CONFIGURATION.equals(localName)) {
				returnToParent();
			} else {
				unexpectedEndElement(localName);
			}
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #endElement(String)
		 */
		@Override
		void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (ConfigurationSpecXmlService.ELMNT_CONFIGURATION.equals(localName)) {
				final ConfigurationType type = ConfigurationType.valueOf(super.trim(atts
						.getValue(ConfigurationSpecXmlService.ATTR_TYPE)));
				final String id = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_ID));
				String clusterId = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_CLUSTER_ID));
				if (clusterId == null && this.parent != null) {
					clusterId = this.parent.getClusterId();
				}
				final String pid = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_PID));
				final String isTemplateString = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_TEMPLATE));
				Boolean isTemplate = false; // default
				if (isTemplateString != null) {
					isTemplate = (Boolean) PropertyConverterUtility.convert(isTemplateString, "boolean");
				}
				ConfigurationSpec config = new ConfigurationSpec(type, id, clusterId, pid, isTemplate, this.parent);
				Map<String, Object> properties = new HashMap<String, Object>();
				config.setProperties(properties);
				this.parent.addChild(config);
				SystemConfigurationContentHandler.this.state = new ConfigurationState(this, config);
			} else if (ConfigurationSpecXmlService.ELMNT_PROPERTY.equals(localName)) {
				final String key = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_NAME));
				final String value = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_VALUE));
				final String type = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_TYPE));
				try {
					this.parent.addProperty(key, PropertyConverterUtility.convert(value, type));
				} catch (final NumberFormatException e) {
					unexpectedAttributeValue(ConfigurationSpecXmlService.ATTR_VALUE, value, e.getLocalizedMessage());
				}
				SystemConfigurationContentHandler.this.state = new PropertyState(this);
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	class EndDocumentState extends SystemConfigurationContentHandler.State {
		/**
		 * Constructs an instance of this class.
		 */
		public EndDocumentState() {
			super(null);
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	class PropertyState extends SystemConfigurationContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * parameter.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 */
		public PropertyState(final SystemConfigurationContentHandler.State parentState) {
			super(parentState);
		}

		/**
		 * End element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #startElement(String,Attributes)
		 */
		@Override
		void endElement(final String localName) throws SAXParseException {
			if (ConfigurationSpecXmlService.ELMNT_PROPERTY.equals(localName)) {
				returnToParent();
			} else {
				unexpectedEndElement(localName);
			}
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #endElement(String)
		 */
		@Override
		void startElement(final String localName, final Attributes atts) throws SAXParseException {
			unexpectedStartElement(localName);
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	class StartDocumentState extends SystemConfigurationContentHandler.State {
		/**
		 * Constructs an instance of this class.
		 */
		public StartDocumentState() {
			super(null);
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (ConfigurationSpecXmlService.ELMNT_NODE_CONFIG.equals(localName)) {
				SystemConfigurationContentHandler.this.state = new SystemConfigurationContentHandler.SystemConfigurationState(
						this);
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	abstract class State {
		/**
		 * Define the parent state (State) field.
		 */
		protected State parentState;

		/**
		 * Constructs an instance of this class from the specified parent state
		 * parameter.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 */
		protected State(final SystemConfigurationContentHandler.State parentState) {
			this.parentState = parentState;
		}

		/**
		 * Characters with the specified ch, start and length parameters.
		 * 
		 * @param ch
		 *            The ch (<code>char[]</code>) parameter.
		 * @param start
		 *            The start (<code>int</code>) parameter.
		 * @param length
		 *            The length (<code>int</code>) parameter.
		 */
		public void characters(final char[] ch, final int start, final int length) {
			/* do nothing */
		}

		/**
		 * End document.
		 * 
		 * @see #startDocument()
		 */
		void endDocument() {
			SystemConfigurationContentHandler.this.state = new SystemConfigurationContentHandler.EndDocumentState();
		}

		/**
		 * End element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #startElement(String,Attributes)
		 * @see #unexpectedEndElement(String)
		 * @see #unexpectedStartElement(String)
		 */
		void endElement(final String localName) throws SAXParseException {
			/* do nothing */
		}

		/**
		 * Return to parent.
		 */
		protected void returnToParent() {
			SystemConfigurationContentHandler.this.state = this.parentState;
		}

		/**
		 * Start document.
		 * 
		 * @see #endDocument()
		 */
		void startDocument() {
			SystemConfigurationContentHandler.this.state = new SystemConfigurationContentHandler.StartDocumentState();
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #endElement(String)
		 * @see #unexpectedEndElement(String)
		 * @see #unexpectedStartElement(String)
		 */
		void startElement(final String localName, final Attributes atts) throws SAXParseException {
			/* do nothing */
		}

		/**
		 * Trim with the specified to trim parameter and return the String
		 * result.
		 * 
		 * @param toTrim
		 *            The to trim (<code>String</code>) parameter.
		 * @return Results of the trim (<code>String</code>) value.
		 */
		String trim(final String toTrim) {
			if (toTrim != null) {
				return toTrim.trim();
			}
			return null;
		}

		/**
		 * Unexpected attribute value with the specified attr, value and message
		 * parameters.
		 * 
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param value
		 *            The value (<code>String</code>) parameter.
		 * @param message
		 *            The message (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		void unexpectedAttributeValue(final String attr, final String value, final String message) throws SAXParseException {
			throw new SAXParseException("Unexpected attribute value '" + attr + "'=" + value, null, null,
					SystemConfigurationContentHandler.this.lineNumber, 0);
		}

		/**
		 * Unexpected end element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		void unexpectedEndElement(final String localName) throws SAXParseException {
			throw new SAXParseException("Unexpected end element '" + localName + "'", null, null,
					SystemConfigurationContentHandler.this.lineNumber, 0);
		}

		/**
		 * Unexpected start element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		void unexpectedStartElement(final String localName) throws SAXParseException {
			throw new SAXParseException("Unexpected start element '" + localName + "'", null, null,
					SystemConfigurationContentHandler.this.lineNumber, 0);
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	class SystemConfigurationState extends SystemConfigurationContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * parameter.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 */
		public SystemConfigurationState(final SystemConfigurationContentHandler.State parentState) {
			super(parentState);
		}

		/**
		 * End element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #startElement(String,Attributes)
		 */
		@Override
		void endElement(final String localName) throws SAXParseException {
			if (ConfigurationSpecXmlService.ELMNT_NODE_CONFIG.equals(localName)) {
				returnToParent();
			} else {
				unexpectedEndElement(localName);
			}
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #endElement(String)
		 */
		@Override
		void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (ConfigurationSpecXmlService.ELMNT_CONFIGURATION.equals(localName)) {
				final ConfigurationType type = ConfigurationType.valueOf(super.trim(atts
						.getValue(ConfigurationSpecXmlService.ATTR_TYPE)));
				final String id = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_ID));
				String clusterId = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_CLUSTER_ID));
				if (clusterId == null) {
					clusterId = ConfigurationSpecXmlService.DEFAULT_CLUSTER_ID;
				}
				final String pid = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_PID));
				final String isTemplateString = super.trim(atts.getValue(ConfigurationSpecXmlService.ATTR_TEMPLATE));
				Boolean isTemplate = false; // default
				if (isTemplateString != null) {
					isTemplate = (Boolean) PropertyConverterUtility.convert(isTemplateString, "boolean");
				}
				ConfigurationSpec config = new ConfigurationSpec(type, id, clusterId, pid, isTemplate, null);
				Map<String, Object> properties = new HashMap<String, Object>();
				config.setProperties(properties);
				SystemConfigurationContentHandler.this.configurations.add(config);
				SystemConfigurationContentHandler.this.state = new ConfigurationState(this, config);
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * Define the configuration (SystemConfiguration) field.
	 */
	Set<ConfigurationSpec> configurations;
	/**
	 * Define the state (State) field.
	 */
	State state;
	/**
	 * Define the lineNumber (int) field.
	 */
	int lineNumber;
	/**
	 * Define the jaxp schema language (String) constant.
	 */
	private final static String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; //$NON-NLS-1$
	/**
	 * Define the w3c xml schema (String) constant.
	 */
	private final static String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	/**
	 * Define the jaxp schema source (String) constant.
	 */
	private final static String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource"; //$NON-NLS-1$
	/**
	 * Define the schema source (String) constant.
	 */
	private final static String SCHEMA_SOURCE = "/configuration.xsd"; //$NON-NLS-1$

	/**
	 * Parse with the specified sax parser factory and in parameters and return
	 * the SystemConfiguration result.
	 * 
	 * @param saxParser
	 *            The sax parser (<code>SAXParser</code>) parameter.
	 * @param is
	 *            The is (<code>InputStream</code>) parameter.
	 * @return Results of the parse (<code>TestScript</code>) value.
	 * @throws Exception
	 *             Exception.
	 */
	public static Set<ConfigurationSpec> parse(final SAXParser saxParser, final InputStream is) throws Exception {
		if (saxParser.isValidating()) {
			try {
				saxParser.setProperty(SystemConfigurationContentHandler.JAXP_SCHEMA_LANGUAGE,
						SystemConfigurationContentHandler.W3C_XML_SCHEMA);
				final InputStream schema = SystemConfigurationContentHandler.class
						.getResourceAsStream(SystemConfigurationContentHandler.SCHEMA_SOURCE);
				saxParser.setProperty(SystemConfigurationContentHandler.JAXP_SCHEMA_SOURCE, schema);
			} catch (final SAXNotRecognizedException e) {
				/* ignore exception */
			}
		}
		final SystemConfigurationContentHandler systemConfigurationContentHandler = new SystemConfigurationContentHandler();
		saxParser.parse(is, systemConfigurationContentHandler);
		return systemConfigurationContentHandler.getConfigurations();
	}

	/**
	 * Parse with the specified sax parser factory and in parameters and return
	 * the SystemConfiguration result.
	 * 
	 * @param saxParser
	 *            The sax parser (<code>SAXParser</code>) parameter.
	 * @param in
	 *            The in (<code>Reader</code>) parameter.
	 * @return Results of the parse (<code>TestScript</code>) value.
	 * @throws Exception
	 *             Exception.
	 */
	public static Set<ConfigurationSpec> parse(final SAXParser saxParser, final Reader in) throws Exception {
		if (saxParser.isValidating()) {
			try {
				saxParser.setProperty(SystemConfigurationContentHandler.JAXP_SCHEMA_LANGUAGE,
						SystemConfigurationContentHandler.W3C_XML_SCHEMA);
				final InputStream schema = SystemConfigurationContentHandler.class
						.getResourceAsStream(SystemConfigurationContentHandler.SCHEMA_SOURCE);
				saxParser.setProperty(SystemConfigurationContentHandler.JAXP_SCHEMA_SOURCE, schema);
			} catch (final SAXNotRecognizedException e) {
				/* ignore exception */
			}
		}
		final InputSource inputSource = new InputSource(in);
		final SystemConfigurationContentHandler systemConfigurationContentHandler = new SystemConfigurationContentHandler();
		saxParser.parse(inputSource, systemConfigurationContentHandler);
		return systemConfigurationContentHandler.getConfigurations();
	}

	/**
	 * Constructs an instance of this class.
	 */
	public SystemConfigurationContentHandler() {
		super();
		this.configurations = new HashSet<ConfigurationSpec>();
		this.state = new SystemConfigurationContentHandler.EndDocumentState();
		this.lineNumber = 1;
	}

	/**
	 * Characters with the specified ch, start and length parameters.
	 * 
	 * @param ch
	 *            The ch (<code>char[]</code>) parameter.
	 * @param start
	 *            The start (<code>int</code>) parameter.
	 * @param length
	 *            The length (<code>int</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 * @see #characters(char[],int,int)
	 */
	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		this.state.characters(ch, start, length);
		countLines(ch, start, length);
	}

	/**
	 * Count lines with the specified ch, start and length parameters.
	 * 
	 * @param ch
	 *            The ch (<code>char[]</code>) parameter.
	 * @param start
	 *            The start (<code>int</code>) parameter.
	 * @param length
	 *            The length (<code>int</code>) parameter.
	 */
	private void countLines(final char[] ch, final int start, final int length) {
		int i = start;
		final int end = start + length;
		while (i < end) {
			if (ch[i++] == '\n') {
				this.lineNumber += 1;
			}
		}
	}

	/**
	 * End document.
	 */
	@Override
	public void endDocument() {
		this.state.endDocument();
	}

	/**
	 * End element with the specified namespace uri, local name and q name
	 * parameters.
	 * 
	 * @param namespaceURI
	 *            The namespace uri (<code>String</code>) parameter.
	 * @param localName
	 *            The local name (<code>String</code>) parameter.
	 * @param qName
	 *            The q name (<code>String</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 * @see SystemConfigurationContentHandler.State#endElement(String)
	 * @see #startElement(String, String, String, Attributes)
	 */
	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
		this.state.endElement(qName);
	}

	/**
	 * Gets the test script value.
	 * 
	 * @return The system configuration (<code>SystemConfiguration</code>)
	 *         value.
	 */
	private Set<ConfigurationSpec> getConfigurations() {
		return this.configurations;
	}

	/**
	 * Ignorable whitespace with the specified ch, start and length parameters.
	 * 
	 * @param ch
	 *            The ch (<code>char[]</code>) parameter.
	 * @param start
	 *            The start (<code>int</code>) parameter.
	 * @param length
	 *            The length (<code>int</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 */
	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		countLines(ch, start, length);
	}

	/**
	 * Start document.
	 */
	@Override
	public void startDocument() {
		this.state.startDocument();
	}

	/**
	 * Start element with the specified namespace uri, local name, q name and
	 * atts parameters.
	 * 
	 * @param namespaceURI
	 *            The namespace uri (<code>String</code>) parameter.
	 * @param localName
	 *            The local name (<code>String</code>) parameter.
	 * @param qName
	 *            The q name (<code>String</code>) parameter.
	 * @param atts
	 *            The atts (<code>Attributes</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 * @see #endElement(String, String, String)
	 * @see SystemConfigurationContentHandler.State#startElement(String,Attributes)
	 */
	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts)
			throws SAXException {
		this.state.startElement(qName, atts);
	}

}
